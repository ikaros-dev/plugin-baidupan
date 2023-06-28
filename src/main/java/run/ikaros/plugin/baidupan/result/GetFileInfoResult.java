package run.ikaros.plugin.baidupan.result;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import lombok.Data;

@Data
public class GetFileInfoResult {
    @JsonProperty("errmsg")
    private String errMsg;
    private Integer errno;
    @JsonProperty("request_id")
    private String requestId;
    private List<FileInfo> list;
}
