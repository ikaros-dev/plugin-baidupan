package run.ikaros.plugin.baidupan;

import org.springframework.stereotype.Component;
import run.ikaros.api.core.file.RemoteFileChunk;
import run.ikaros.api.core.file.RemoteFileHandler;

import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
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
        return null;
    }

    @Override
    public void pull(Path path, List<String> list) {

    }

    @Override
    public void delete(Path path) {

    }
}
