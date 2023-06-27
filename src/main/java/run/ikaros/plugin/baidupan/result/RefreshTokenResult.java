package run.ikaros.plugin.baidupan.result;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RefreshTokenResult {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private Integer expiresIn;
    @JsonProperty("refresh_token")
    private String refreshToken;
    private String scope;
}
