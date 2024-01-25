package top.trumandu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

/**
 * @author Truman.P.Du
 * @date 2024/01/11
 */
@ConfigurationProperties(prefix = "hive")
public class HiveProperties {
    @NotNull
    private String url;
    private String user="hive";
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
