package run.ikaros.plugin.baidupan.result;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @see <a href="https://pan.baidu.com/union/doc/pksg0s9ns">获取用户信息</a>
 */
@Data
public class UserInfoResult {
    /**
     * 百度帐号.
     */
    @JsonProperty("baidu_name")
    private String baiduName;
    /**
     * 网盘帐号.
     */
    @JsonProperty("netdisk_name")
    private String netDiskName;
    /**
     * 头像地址.
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;
    /**
     * 会员类型，0普通用户、1普通会员、2超级会员.
     */
    @JsonProperty("vip_type")
    private Integer vipType;
    /**
     * 用户ID.
     */
    private Long uk;
}
