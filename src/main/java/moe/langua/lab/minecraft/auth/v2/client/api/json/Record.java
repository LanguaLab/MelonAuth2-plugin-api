package moe.langua.lab.minecraft.auth.v2.client.api.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Record {
    @SerializedName("uuid")
    @Expose
    private String uniqueID = null;
    @SerializedName("commitTime")
    @Expose
    private Long commitTime = null;
    @SerializedName("expireTime")
    @Expose
    private Long expireTime = null;

    public static Record getWithExpireTimeStamp(UUID uniqueID, Long commitTime, Long expireTimeStamp) {
        Record resultRecord = new Record();
        resultRecord.uniqueID = uniqueID.toString();
        resultRecord.commitTime = commitTime;
        resultRecord.expireTime = expireTimeStamp;
        return resultRecord;
    }

    public static Record newCache(UUID uniqueID, Long commitTime) {
        return Record.getWithExpireTimeStamp(uniqueID, commitTime,
                System.currentTimeMillis() + Config.instance.getCacheLifeInMilliSeconds());
    }

    public UUID getUniqueID() {
        return UUID.fromString(uniqueID);
    }

    public Long getExpireTime() {
        return expireTime;
    }
}
