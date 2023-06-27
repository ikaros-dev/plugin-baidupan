package run.ikaros.plugin.baidupan.result;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FileCreateResult {
    private Integer errno;
    @JsonProperty("fs_id")
    private Long fsId;
    private String md5;
    @JsonProperty("server_filename")
    private String filename;
    /**
     * 分类类型, 1 视频 2 音频 3 图片 4 文档 5 应用 6 其他 7 种子.
     */
    private Integer category;
    private String path;
    private Long size;
    @JsonProperty("ctime")
    private Long createTime;
    @JsonProperty("mtime")
    private Long modificationTime;
    /**
     * 是否目录，0 文件、1 目录.
     */
    @JsonProperty("isdir")
    private Integer isDir;


}
