package moe.langua.lab.minecraft.auth.v2.plugin.api.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This is a JSON serialization class. <br>
 * <code>uuid</code> is the unique ID(with hyphens) of the player. <br>
 * <code>name</code> is the player's username. <br>
 * <code>expireIn</code> is a countdown in milliseconds, which is the remaining valid time of a pending verify request. <br>
 * <code>url</code> is the URL of the skin file that need to be uploaded to complete the challenge. <br>
 * <code>skinModel</code> is the skin model of the player skin. `alex` is the 3-pixel arm model and `steve` is 4-pixel. <br>
 */

public class ChallengeDetail {

    @SerializedName("uuid")
    @Expose
    public String uuid;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("expireIn")
    @Expose
    public Long expireIn;
    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("skinModel")
    @Expose
    public String skinModel;

    public ChallengeDetail() {
    }

    public ChallengeDetail(String uuid, String name, Long expireIn, String url, String skinModel) {
        this.uuid = uuid;
        this.name = name;
        this.expireIn = expireIn;
        this.url = url;
        this.skinModel = skinModel;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Long getExpireIn() {
        return expireIn;
    }

    public String getUrl() {
        return url;
    }

    public String getSkinModel() {
        return skinModel;
    }
}