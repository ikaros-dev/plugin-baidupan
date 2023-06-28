package run.ikaros.plugin.baidupan.result;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FileInfo {
    private Integer category;
    @JsonProperty("dlink")
    private String downloadLink;
    private String filename;
    @JsonProperty("fs_id")
    private Long fsId;
    @JsonProperty("isdir")
    private Integer isDir;
    private String md5;
    private String path;
    private Long size;
}
