package run.ikaros.plugin.baidupan;

import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import run.ikaros.api.core.setting.ConfigMap;
import run.ikaros.api.custom.ReactiveCustomClient;
import run.ikaros.api.infra.properties.IkarosProperties;
import run.ikaros.api.infra.utils.FileUtils;
import run.ikaros.api.plugin.event.PluginConfigMapUpdateEvent;
import run.ikaros.plugin.baidupan.result.FileCreateResult;
import run.ikaros.plugin.baidupan.result.FilePreCreateResult;
import run.ikaros.plugin.baidupan.result.RefreshTokenResult;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BaiDuPanClient {
    private final IkarosProperties ikarosProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ReactiveCustomClient reactiveCustomClient;
    private String appKey;
    private String secretKey;
    private String refreshToken;
    private String accessToken;

    public BaiDuPanClient(IkarosProperties ikarosProperties,
                          ReactiveCustomClient reactiveCustomClient) {
        this.ikarosProperties = ikarosProperties;
        this.reactiveCustomClient = reactiveCustomClient;
    }

    @EventListener(PluginConfigMapUpdateEvent.class)
    public void init(PluginConfigMapUpdateEvent event) {
        ConfigMap configMap = event.getConfigMap();
        if (!BaiDuPanConst.NAME.equals(configMap.getName())) {
            return;
        }

        Map<String, Object> dataMap = configMap.getData();
        appKey = String.valueOf(dataMap.get("appKey"));
        secretKey = String.valueOf(dataMap.get("secretKey"));
        refreshToken = String.valueOf(dataMap.get("refreshToken"));
        accessToken = String.valueOf(dataMap.get("accessToken"));

        log.debug("init appKey: [{}].", appKey);
        log.debug("init secretKey: [{}].", secretKey);
        log.debug("init accessToken: [{}].", accessToken);
    }

    public void refreshAccessToken() {
        Assert.hasText(appKey, "'appKey' must has text when request refresh token.");
        Assert.hasText(secretKey, "'secretKey' must has text when request refresh token.");
        Assert.hasText(refreshToken, "'refreshToken' must has text when request refresh token.");

        UriComponents uriComponents =
            UriComponentsBuilder.fromHttpUrl("https://openapi.baidu.com/oauth/2.0/token")
                .queryParam("grant_type", "refresh_token")
                .queryParam("refresh_token", refreshToken)
                .queryParam("client_id", appKey)
                .queryParam("client_secret", secretKey)
                .build();

        RefreshTokenResult refreshTokenResult = restTemplate
            .getForEntity(uriComponents.toUri(), RefreshTokenResult.class)
            .getBody();
        if (refreshTokenResult == null) {
            throw new BaiDuPanException("refresh token fail for client_id: " + appKey);
        }

        accessToken = refreshTokenResult.getAccessToken();
        log.info("refresh accessToken to: {}", accessToken);
        reactiveCustomClient.findOne(ConfigMap.class, BaiDuPanConst.NAME)
            .map(configMap -> configMap.putDataItem("refreshToken",
                refreshTokenResult.getRefreshToken()))
            .map(configMap -> configMap.putDataItem("accessToken",
                refreshTokenResult.getAccessToken()))
            .flatMap(reactiveCustomClient::update)
            .doOnSuccess(configMap -> log.info("update config map accessToken : {}", configMap))
            .subscribe();
    }

    public FileCreateResult uploadFile(Path path) {
        Assert.notNull(path, "'path' must not null.");

        File file = path.toFile();
        if (file.isDirectory()) {
            throw new BaiDuPanException("please appoint file path: " + path);
        }

        // 文件分片 按4MB进行分片
        Path chunkCacheDirPath = ikarosProperties.getWorkDir()
            .resolve("cache").resolve("plugin")
            .resolve(BaiDuPanConst.NAME)
            .resolve(FileUtils.formatDirName(file.getName()));
        if (!chunkCacheDirPath.toFile().exists()) {
            chunkCacheDirPath.toFile().mkdirs();
        }
        FileUtils.split(path, chunkCacheDirPath, 1024 * 4);
        // 计算分片文件的md5值
        List<String> blockList = new ArrayList<>();
        File[] files = chunkCacheDirPath.toFile().listFiles();
        if(files == null) {
            throw new BaiDuPanException("file split fail, chunk files is null");
        }
        List<File> sortedFiles = Arrays.stream(files)
            .sorted((o1, o2) -> (int) (Long.parseLong(o1.getName())
                - Long.parseLong(o2.getName()))).toList();

        final int length = files.length;
        int calculateFileHashIndex = 0;
        for (File listFile : sortedFiles) {
            try {
                blockList.add(
                    FileUtils.calculateFileHash(FileUtils.convertToDataBufferFlux(listFile)));
                calculateFileHashIndex++;
                log.info("current calculate file chunk: {}/{}", calculateFileHashIndex, length);
            } catch (NoSuchAlgorithmException | IOException e) {
                throw new BaiDuPanException("calculate chunk file hash fail.", e);
            }
        }

        // 远端路径
        String remotePath = "/apps/ikaros/" +UUID.randomUUID().toString().replace("-", "")
            + "-" + file.getName();

        // 预上传
        UriComponents uriComponents =
            UriComponentsBuilder.fromHttpUrl("http://pan.baidu.com/rest/2.0/xpan/file")
                .queryParam("method", "precreate")
                .queryParam("access_token", accessToken).build();
        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.put("path", List.of(remotePath));
        bodyMap.put("size", List.of(file.length()));
        bodyMap.put("isdir", List.of(0));
        bodyMap.put("block_list", Collections.singletonList(blockList));
        bodyMap.put("autoinit", List.of(1));
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(bodyMap);
        FilePreCreateResult filePreCreateResult =
            restTemplate.postForEntity(uriComponents.toUri(), httpEntity, FilePreCreateResult.class)
                .getBody();
        if (filePreCreateResult == null || filePreCreateResult.getErrno() != 0) {
            if (filePreCreateResult != null && 111 == filePreCreateResult.getErrno()) {
                refreshAccessToken();
            }
            throw new BaiDuPanException(
                "request pre create file fail, errno: " +
                    (filePreCreateResult == null ? null : filePreCreateResult.getErrno()));
        }
        String uploadId = filePreCreateResult.getUploadId();
        log.debug("update uploadId to {}.", uploadId);


        // 分片上传
        for (int i = 0; i < sortedFiles.size(); i++) {
            try {
                uploadChunkFile(sortedFiles.get(i), remotePath, uploadId, i);
                log.info("current upload chunk file: {}/{}", i, length);
            } catch (IOException e) {
                throw new BaiDuPanException("upload chunk file fail.", e);
            }
        }

        try {
            FileUtils.deleteDirByRecursion(chunkCacheDirPath.toFile().getAbsolutePath());
        } catch (IOException e) {
            throw new BaiDuPanException("delete chunk file fail.",e);
        }

        // 创建文件
        return createRemoteFile(remotePath, file.length(), blockList, uploadId);
    }

    private FileCreateResult createRemoteFile(String path, Long size, List<String> blockList,
                                              String uploadId) {
        UriComponents uriComponents =
            UriComponentsBuilder.fromHttpUrl("https://pan.baidu.com/rest/2.0/xpan/file")
                .queryParam("method", "create")
                .queryParam("access_token", accessToken).build();

        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.put("path", Collections.singletonList(path));
        bodyMap.put("size", Collections.singletonList(size));
        bodyMap.put("isdir", Collections.singletonList(0));
        bodyMap.put("block_list", Collections.singletonList(blockList));
        bodyMap.put("uploadid", Collections.singletonList(uploadId));

        FileCreateResult fileCreateResult =
            restTemplate.postForEntity(uriComponents.toUri(), bodyMap, FileCreateResult.class)
                .getBody();
        if (fileCreateResult == null || fileCreateResult.getErrno() != 0) {
            if (fileCreateResult != null && 111 == fileCreateResult.getErrno()) {
                refreshAccessToken();
            }
            throw new BaiDuPanException(
                "create remote file fail, errno: " +
                    (fileCreateResult == null ? null : fileCreateResult.getErrno()));
        }

        return fileCreateResult;
    }

    private void uploadChunkFile(File file, String path, String uploadId, int index) throws IOException {
        UriComponents uriComponents =
            UriComponentsBuilder.fromHttpUrl("https://d.pcs.baidu.com/rest/2.0/pcs/superfile2")
                .queryParam("method", "upload")
                .queryParam("access_token", accessToken)
                .queryParam("type", "tmpfile")
                .queryParam("path", URLEncoder.encode(path, StandardCharsets.UTF_8))
                .queryParam("uploadid", uploadId)
                .queryParam("partseq", index).build();

        FileSystemResource fileSystemResource = new FileSystemResource(file.toPath());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.put("file", List.of(fileSystemResource));
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(bodyMap, headers);
        String result =
            restTemplate.postForEntity(uriComponents.toUri(), httpEntity, String.class).getBody();
        log.debug("upload chunk file success for path: {} and result: {}.", file.getAbsolutePath(),
            result);
    }


}
