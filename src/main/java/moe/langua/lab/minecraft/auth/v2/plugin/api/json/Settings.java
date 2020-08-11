package moe.langua.lab.minecraft.auth.v2.plugin.api.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import moe.langua.lab.minecraft.auth.v2.plugin.api.Utils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Settings {
    @SerializedName("apiURL")
    @Expose
    private String apiURL = null;
    @SerializedName("webappURL")
    @Expose
    private String webappURL = null;
    @SerializedName("serverName")
    @Expose
    private String serverName = null;
    @SerializedName("secretKey")
    @Expose
    private String secretKey = null;
    @SerializedName("retryInterval")
    @Expose
    private Long retryInterval = null;

    public static Settings getDefault() throws MalformedURLException {
        Settings settings = new Settings();
        return Settings.check(settings);
    }

    public static Settings check(Settings settingsInstance) throws MalformedURLException {
        if (settingsInstance.apiURL == null) settingsInstance.apiURL = "http://127.0.0.1";
        if (settingsInstance.webappURL == null) settingsInstance.webappURL = "http://127.0.0.1";
        settingsInstance.apiURL = Utils.removeSlashAtTheEnd(settingsInstance.apiURL);
        settingsInstance.webappURL = Utils.removeSlashAtTheEnd(settingsInstance.webappURL);
        new URL(settingsInstance.apiURL);
        new URL(settingsInstance.webappURL);
        if (settingsInstance.serverName == null) settingsInstance.serverName = "Server_" + Utils.getRandomString(8);
        if (settingsInstance.secretKey == null) settingsInstance.secretKey = Utils.getRandomString(64);
        if (settingsInstance.retryInterval == null) settingsInstance.retryInterval = 1000L;
        return settingsInstance;
    }

    public static Settings load(File sourceFile) throws IOException {
        if (sourceFile.exists() && !sourceFile.isFile())
            throw new IllegalArgumentException(sourceFile.getAbsolutePath() + " should be a file, but found a directory.");
        Settings settings;
        if (sourceFile.createNewFile()) {
            settings = Settings.getDefault();
        } else {
            InputStreamReader sourceFileReader = new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.UTF_8);
            settings = Utils.prettyGSON.fromJson(sourceFileReader, Settings.class);
        }
        return settings;
    }

    public static void save(Settings settingsInstance, File targetFile) throws IOException {
        if (!targetFile.createNewFile() && !targetFile.isFile())
            throw new IllegalArgumentException(targetFile.getAbsolutePath() + " should be a file, but found a directory.");
        FileOutputStream targetFileWriterStream = new FileOutputStream(targetFile);
        targetFileWriterStream.write(Utils.prettyGSON.toJson(settingsInstance).getBytes(StandardCharsets.UTF_8));
    }

    public void reload(Settings newSettings) throws MalformedURLException {
        Settings.check(newSettings);
        this.apiURL = newSettings.apiURL;
        this.serverName = newSettings.serverName;
        this.secretKey = newSettings.secretKey;
        this.retryInterval = newSettings.retryInterval;
    }

    public URL getApiURL() {
        try {
            return new URL(apiURL);
        } catch (MalformedURLException ignore) {
            return null;
        }
    }

    public String getServerName() {
        return serverName;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public Long getRetryInterval() {
        return retryInterval;
    }

    public URL getWebappURL() {
        try {
            return new URL(webappURL);
        } catch (MalformedURLException ignore) {
            return null;
        }
    }
}
