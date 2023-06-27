package run.ikaros.plugin.baidupan;

public class BaiDuPanException extends RuntimeException{
    public BaiDuPanException(String message) {
        super(message);
    }

    public BaiDuPanException(String message, Throwable cause) {
        super(message, cause);
    }
}
