package run.ikaros.plugin.baidupan;

import org.pf4j.Extension;
import org.springframework.stereotype.Component;
import run.ikaros.api.core.file.RemoteFileChunk;
import run.ikaros.api.core.file.RemoteFileHandler;
import run.ikaros.plugin.baidupan.result.FileCreateResult;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Extension
public class BaiDuPanFileHandler implements RemoteFileHandler {
    private final BaiDuPanClient client;

    public BaiDuPanFileHandler(BaiDuPanClient client) {
        this.client = client;
    }

    @Override
    public String remote() {
        return BaiDuPanConst.REMOTE;
    }

    @Override
    public List<RemoteFileChunk> push(Path path) {
        File[] files = path.toFile().listFiles();
        if (files == null) {
            throw new RuntimeException("has not files for path: " + path);
        }
        List<RemoteFileChunk> remoteFileChunks = new ArrayList<>();
        for (File file : files) {
            FileCreateResult fileCreateResult = client.uploadFile(file.toPath());
            RemoteFileChunk remoteFileChunk = new RemoteFileChunk();
            remoteFileChunk.setFileId(String.valueOf(fileCreateResult.getFsId()));
            remoteFileChunk.setFileName(String.valueOf(fileCreateResult.getFilename()));
            remoteFileChunk.setCategory(fileCreateResult.getCategory());
            remoteFileChunk.setPath(fileCreateResult.getPath());
            remoteFileChunk.setMd5(fileCreateResult.getMd5());
            remoteFileChunk.setIsDir(fileCreateResult.getIsDir() == 1);
            remoteFileChunk.setSize(fileCreateResult.getSize());
            log.info("upload chunk file to remote[{}] for name: [{}].",
                BaiDuPanConst.REMOTE, file.getName());
            remoteFileChunks.add(remoteFileChunk);
        }
        return remoteFileChunks;
    }

    @Override
    public void pull(Path path, List<String> list) {

    }

    @Override
    public void delete(Path path) {

    }

    @Override
    public boolean ready() {
        return client.ready();
    }
}
