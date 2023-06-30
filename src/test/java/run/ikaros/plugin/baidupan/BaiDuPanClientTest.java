package run.ikaros.plugin.baidupan;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import run.ikaros.api.core.setting.ConfigMap;
import run.ikaros.api.infra.properties.IkarosProperties;
import run.ikaros.api.plugin.event.PluginConfigMapUpdateEvent;
import run.ikaros.plugin.baidupan.result.FileCreateResult;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BaiDuPanClientTest {

    @Test
    @Disabled
    void uploadFile() {
        IkarosProperties ikarosProperties = new IkarosProperties();
        ikarosProperties.setWorkDir(new File("C:\\Develop\\test\\ikaros-plugin").toPath());
        BaiDuPanClient baiDuPanClient = new BaiDuPanClient(ikarosProperties, null);

        ConfigMap configMap = new ConfigMap();
        configMap.setName(BaiDuPanConst.NAME);
        configMap.putDataItem("appKey", System.getenv("TEST_APP_KEY"));
        configMap.putDataItem("secretKey", System.getenv("TEST_SECRET_KEY"));
        configMap.putDataItem("refreshToken", System.getenv("TEST_REFRESH_TOKEN"));
        configMap.putDataItem("accessToken", System.getenv("TEST_ACCESS_TOKEN"));

        baiDuPanClient.init(configMap);

        File file = new File(
            "C:\\Develop\\test\\ikaros-plugin\\アイドル - YOASOBI.flac");

        FileCreateResult fileCreateResult = baiDuPanClient.uploadFile(file.toPath());

        System.out.println(fileCreateResult);
        System.out.println(fileCreateResult.getPath());

        assertThat(fileCreateResult).isNotNull();
        assertThat(fileCreateResult.getErrno()).isEqualTo(0);
    }

    @Test
    @Disabled
    void download() {
        IkarosProperties ikarosProperties = new IkarosProperties();
        ikarosProperties.setWorkDir(new File("C:\\Develop\\test\\ikaros-plugin").toPath());
        BaiDuPanClient baiDuPanClient = new BaiDuPanClient(ikarosProperties, null);

        ConfigMap configMap = new ConfigMap();
        configMap.setName(BaiDuPanConst.NAME);
        configMap.putDataItem("appKey", System.getenv("TEST_APP_KEY"));
        configMap.putDataItem("secretKey", System.getenv("TEST_SECRET_KEY"));
        configMap.putDataItem("accessToken", System.getenv("TEST_ACCESS_TOKEN"));
        configMap.putDataItem("refreshToken", System.getenv("TEST_REFRESH_TOKEN"));
        baiDuPanClient.init(configMap);

        baiDuPanClient.download("190341088047736",
            Path.of(new File("C:\\Develop\\test\\ikaros-plugin\\encrypt").toURI()));
    }

}