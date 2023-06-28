package run.ikaros.plugin.baidupan.result;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import lombok.Data;

@Data
public class FileDeleteResult {
    private List<FileInfo> list;
    @JsonProperty("taskid")
    private Long taskId;
}
