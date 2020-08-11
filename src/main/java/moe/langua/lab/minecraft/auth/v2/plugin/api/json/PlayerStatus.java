package moe.langua.lab.minecraft.auth.v2.plugin.api.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * This is a JSON serialization class. <br>
 *
 * <code>uniqueID</code> is the UUID of the player.
 * <code>verified</code> is <code>true</code> or <code>false</code> depend on weather the player has completed the challenge.
 * <code>commitTime</code> will be a non-zero value if the player is verified. Is a timestamp when the player completed the challenge.
 * <code>verificationLife</code> is a api side config value, which describe when the verification will expired after commitTime. It's a static value if the server side settings has not changed.
 */
public class PlayerStatus {
    @SerializedName("uniqueID")
    @Expose
    private String uniqueID;
    @SerializedName("verified")
    @Expose
    private Boolean verified;
    @SerializedName("commitTime")
    @Expose
    private Long commitTime;
    @SerializedName("verificationLife")
    @Expose
    private Long verificationLife;

    public static PlayerStatus get(UUID uniqueID, boolean verified, Long commitTime, Long verificationLife) {
        PlayerStatus statusInstance = new PlayerStatus();
        statusInstance.uniqueID = uniqueID.toString();
        statusInstance.verified = verified;
        statusInstance.commitTime = commitTime;
        statusInstance.verificationLife = verificationLife;
        return statusInstance;
    }

    public UUID getUniqueID() {
        return UUID.fromString(uniqueID);
    }

    public Boolean getVerified() {
        return verified;
    }

    public Long getCommitTime() {
        return commitTime;
    }

    public Long getVerificationLife() {
        return verificationLife;
    }
}
