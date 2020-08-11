package moe.langua.lab.minecraft.auth.v2.plugin.api.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This is a JSON serialization class.
 * <code>challengeID</code> is a six-digit number, it is the id of a verify request belongs to this player. You can get detail of the challenge by GET `/get/challenge/<challengeID>`.
 * <code>expireIn</code> is a countdown in milliseconds, which is the remaining valid time of a pending verify request.
 */
public class ChallengeOverview {

    @SerializedName("challengeID")
    @Expose
    private int challengeID;
    @SerializedName("expireIn")
    @Expose
    private Long expireIn;

    public ChallengeOverview(int challengeID, Long expireIn) {
        this.challengeID = challengeID;
        this.expireIn = expireIn;
    }

    public ChallengeOverview() {
    }

    public int getChallengeID() {
        return challengeID;
    }

    public Long getExpireIn() {
        return expireIn;
    }
}
