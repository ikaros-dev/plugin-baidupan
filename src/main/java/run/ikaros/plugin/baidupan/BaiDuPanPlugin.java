package run.ikaros.plugin.baidupan;

import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Component;
import run.ikaros.api.plugin.BasePlugin;

import lombok.extern.slf4j.Slf4j;

import static run.ikaros.plugin.baidupan.BaiDuPanConst.NAME;

@Slf4j
@Component
public class BaiDuPanPlugin extends BasePlugin {


    public BaiDuPanPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("plugin [{}] start success", NAME);
    }

    @Override
    public void stop() {
        log.info("plugin [{}] stop success", NAME);
    }

    @Override
    public void delete() {
        log.info("plugin [{}] delete success", NAME);
    }
}
