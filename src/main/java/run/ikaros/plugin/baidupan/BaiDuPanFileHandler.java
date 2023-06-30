package run.ikaros.plugin.baidupan;

import org.pf4j.Extension;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
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
    public RemoteFileChunk push(Path path) {
        Assert.notNull(path, "'path' must not null.");
        File file = path.toFile();
        Assert.notNull(file, "'path file' must not null.");
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
        return remoteFileChunk;
    }

    @Override
    public void pull(Path targetDirPath, String fsId) {
        client.download(fsId, targetDirPath);
    }

    @Override
    public void delete(String path) {
        client.delete(path, true);
    }

    @Override
    public boolean ready() {
        return client.ready();
    }
}
