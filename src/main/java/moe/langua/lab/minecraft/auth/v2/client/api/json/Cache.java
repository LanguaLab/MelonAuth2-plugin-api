package moe.langua.lab.minecraft.auth.v2.client.api.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    @SerializedName("verifiedPlayers")
    @Expose
    private List<Record> records = null;

    public static Map<UUID, Record> getRecordMap(Cache cache) {
        ConcurrentHashMap<UUID, Record> resultMap = new ConcurrentHashMap<>();
        long now = System.currentTimeMillis();
        for (Record x : cache.records) {
            if (x.getExpireTime() < now) continue;
            resultMap.put(x.getUniqueID(), x);
        }
        return resultMap;
    }

    public static Cache getCacheList(Map<UUID, Record> e) {
        Cache resultCache = new Cache();
        resultCache.records = new ArrayList<>();
        ArrayList<Record> records = new ArrayList<>(e.values());
        long now = System.currentTimeMillis();
        for (Record x : records) {
            long expireTime = x.getExpireTime();
            if (expireTime < now) continue;
            resultCache.records.add(x);
        }
        return resultCache;
    }

    public static Cache getEmpty() {
        Cache resultCache = new Cache();
        resultCache.records = new ArrayList<>();
        return resultCache;
    }

    public Cache check() {
        if (records == null) records = new ArrayList<>();
        return this;
    }


}
