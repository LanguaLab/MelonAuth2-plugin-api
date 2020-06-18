package moe.langua.lab.minecraft.auth.v2.client.api.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Record {
    @SerializedName("uuid")
    @Expose
    private String uniqueID = null;
    @SerializedName("expireTime")
    @Expose
    private Long expireTime = null;

    public static Record getWithExpireTimeStamp(UUID uniqueID, long expireTimeStamp) {
        Record resultRecord = new Record();
        resultRecord.uniqueID = uniqueID.toString();
        resultRecord.expireTime = expireTimeStamp;
        return resultRecord;
    }

    public static Record newCache(UUID uniqueID) {
        return Record.getWithExpireTimeStamp(uniqueID,
                System.currentTimeMillis() + Config.instance.getCacheLifeInMilliSeconds());
    }

    public UUID getUniqueID() {
        return UUID.fromString(uniqueID);
    }

    public Long getExpireTime() {
        return expireTime;
    }
}
