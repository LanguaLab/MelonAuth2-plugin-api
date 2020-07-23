package moe.langua.lab.minecraft.auth.v2.client.api.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Config {
    public static Config instance;
    @SerializedName("apiURL")
    @Expose
    private String apiURL = null;
    @SerializedName("clientKey")
    @Expose
    private String clientKey = null;
    @SerializedName("retryDelayInMilliSecond")
    @Expose
    private Long retryDelayInMilliSeconds = null;
    @SerializedName("cacheLifeInMilliSecond")
    @Expose
    private Long cacheLifeInMilliSeconds = null;

    public static Config getDefault() {
        return new Config().check();
    }

    public String getApiURL() {
        return apiURL;
    }

    public String getClientKey() {
        return clientKey;
    }

    public Long getRetryDelayInMilliSeconds() {
        return retryDelayInMilliSeconds;
    }

    public Long getCacheLifeInMilliSeconds() {
        return cacheLifeInMilliSeconds;
    }

    public Config check() {
        if (apiURL == null) apiURL = "http://127.0.0.1";
        apiURL = removeSlashAtTheEnd(apiURL);
        if (clientKey == null) clientKey = "CLIENT_KEY";
        if (retryDelayInMilliSeconds == null) retryDelayInMilliSeconds = 10000L;
        if (cacheLifeInMilliSeconds == null) cacheLifeInMilliSeconds = 864000000L; //10 day by default
        return this;
    }

    private String removeSlashAtTheEnd(String target) {
        while (target.endsWith("/")) {
            target = target.substring(0, target.length() - 1);
        }
        return target;
    }

}
