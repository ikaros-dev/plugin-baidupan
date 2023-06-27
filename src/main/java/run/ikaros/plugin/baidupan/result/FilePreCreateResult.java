package run.ikaros.plugin.baidupan.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import lombok.Data;

@Data
public class FilePreCreateResult {
    private Integer errno;
    private String path;
    @JsonProperty("uploadid")
    private String uploadId;
    @JsonProperty("return_type")
    private Integer returnType;
    @JsonProperty("block_list")
    private List<String> blockList;
}
