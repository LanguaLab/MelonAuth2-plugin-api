package moe.langua.lab.minecraft.auth.v2.client.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import moe.langua.lab.minecraft.auth.v2.client.api.exception.NotInitializedException;
import moe.langua.lab.minecraft.auth.v2.client.api.json.*;
import moe.langua.lab.security.otp.MelonTOTP;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MelonAuthV2API {
    private static final long TRUNCATE_VALUE = 0x100000000L;
    private static final long OTP_EXPIRATION = 30000;
    private static final Gson prettyGSON = new GsonBuilder().setPrettyPrinting().create();
    private static MelonAuthV2API instance = null;
    private static Long verificationLife = null;
    private static MelonTOTP otpServer;
    private final File cacheFile;
    private final File configFile;
    private final Logger logger;
    private final Map<UUID, Record> cacheMap;
    private final Map<UUID, Status> statusMap = new ConcurrentHashMap<>();
    private final LinkedList<UUID> refreshList = new LinkedList<>();

    public MelonAuthV2API(File dataRoot, Logger logger) throws IOException {
        if (!dataRoot.mkdir() && !dataRoot.isDirectory()) {
            throw new IOException(dataRoot.getAbsolutePath() + " should be a directory, but found a file.");
        }
        cacheFile = new File(dataRoot, "cache.json");
        configFile = new File(dataRoot, "config.json");
        boolean firstStart = configFile.createNewFile();
        if (!firstStart && !configFile.isFile()) {
            throw new IOException(configFile.getAbsolutePath() + " should be a file, but found a directory.");
        }
        Config config = prettyGSON.fromJson(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8), Config.class);
        config = config == null ? Config.getDefault() : config.check();
        FileOutputStream configOutputStream = new FileOutputStream(configFile, false);
        configOutputStream.write(prettyGSON.toJson(config).getBytes(StandardCharsets.UTF_8));
        configOutputStream.flush();
        configOutputStream.close();
        Config.instance = config;


        if (!cacheFile.createNewFile() && !cacheFile.isFile()) {
            throw new IOException(cacheFile.getAbsolutePath() + " should be a file, but found a directory.");
        }
        Map<UUID, Record> cacheMap;
        Cache cache = prettyGSON.fromJson(new InputStreamReader(new FileInputStream(cacheFile), StandardCharsets.UTF_8), Cache.class);
        cache = cache == null ? Cache.getEmpty() : cache.check();
        cacheMap = Cache.getRecordMap(cache);
        FileOutputStream cacheOutputStream = new FileOutputStream(cacheFile, false);
        cacheOutputStream.write(prettyGSON.toJson(Cache.getCacheList(cacheMap)).getBytes(StandardCharsets.UTF_8));
        cacheOutputStream.flush();
        cacheOutputStream.close();

        this.logger = logger;
        this.cacheMap = cacheMap;
        otpServer = new MelonTOTP(Config.instance.getClientKey().getBytes(StandardCharsets.UTF_8), TRUNCATE_VALUE, OTP_EXPIRATION);

        if (firstStart) {
            logger.warning("First start detected. Please configure the plugin before next start.");
            return;
        }

        logger.info("Checking server connectivity...");
        try {
            getStatusFromServer(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "API Server refused the request. It may caused by incorrect clientKey or incorrect system time settings.");
            throw e;
        }
        logger.info("Done!");

        new Timer("MelonAuthV2API#guard").scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (refreshList.size() == 0) return;
                String urlString = Config.instance.getApiURL() + "/get/status/" + refreshList.get(0).toString();
                try {
                    PlayerStatus playerStatus = getStatusFromServer(refreshList.get(0));
                    if (playerStatus.getVerified())
                        statusMap.put(refreshList.get(0), Status.VERIFIED);
                    else
                        statusMap.put(refreshList.get(0), Status.NOT_VERIFIED);
                } catch (IOException e) {
                    logger.warning("Exception occurred while trying to GET " + urlString);
                    logger.warning(e.toString());
                    statusMap.put(refreshList.get(0), Status.UNKNOWN);
                    refreshList.add(refreshList.get(0));
                }
                refreshList.remove(0);
            }
        }, 0, Config.instance.getRetryDelayInMilliSeconds());

        instance = this;
    }

    public static Status getStatus(UUID uniqueID) throws NotInitializedException {
        if (instance == null) throw new NotInitializedException("MelonAuthV2 API not initialized");
        return instance.statusMap.getOrDefault(uniqueID, Status.UNKNOWN);
    }

    public static Long getVerificationLife() {
        return verificationLife;
    }

    public static PlayerStatus getPlayerStatusExact(UUID uniqueID) throws NotInitializedException, IOException {
        if (instance == null) throw new NotInitializedException("MelonAuthV2 API not initialized");
        PlayerStatus status = getStatusFromServer(uniqueID);
        if (status.getVerified()) instance.updateAllow(uniqueID, status.getCommitTime());
        return status;
    }

    public static PlayerStatus getPlayerStatus(UUID uniqueID) throws NotInitializedException, IOException {
        if (instance == null) throw new NotInitializedException("MelonAuthV2 API not initialized");
        Record cache = instance.cacheMap.get(uniqueID);
        if (cache != null) {
            return PlayerStatus.get(uniqueID, true, cache.getExpireTime(), verificationLife);
        } else {
            return getPlayerStatusExact(uniqueID);
        }
    }

    public static ChallengeOverview getChallengeID(UUID uniqueID) throws NotInitializedException, IOException {
        URL getURL;
        getURL = new URL(Config.instance.getApiURL() + "/require/" + uniqueID.toString());
        HttpURLConnection connection = (HttpURLConnection) getURL.openConnection();
        connection.setRequestProperty("Authorization", "MelonOTP " + Long.toHexString(otpServer.getPassNow()));
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        int rCode = connection.getResponseCode();
        if (rCode == 200) {
            return prettyGSON.fromJson(new InputStreamReader(connection.getInputStream()), ChallengeOverview.class);
        }
        throw new IOException("API server returned response code " + rCode);
    }


    private static PlayerStatus getStatusFromServer(UUID uniqueID) throws IOException {
        URL getURL;
        getURL = new URL(Config.instance.getApiURL() + "/get/status/" + uniqueID.toString());
        HttpURLConnection connection = (HttpURLConnection) getURL.openConnection();
        connection.setRequestProperty("Authorization", "MelonOTP " + Long.toHexString(otpServer.getPassNow()));
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        int rCode = connection.getResponseCode();
        switch (rCode) {
            case 200:
                PlayerStatus playerStatus = prettyGSON.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), PlayerStatus.class);
                verificationLife = playerStatus.getVerificationLife();
                return playerStatus;
            case 403:
                throw new IOException("API Server refused the request. It may caused by incorrect clientKey or incorrect system time settings.");
            default:
                throw new IOException("API server returned response code " + rCode);
        }
    }

    protected void logoutPlayer(UUID uniqueID) {
        refreshList.remove(uniqueID);
        statusMap.remove(uniqueID);
    }

    private void updateAllow(UUID uniqueID, Long commitTime) {
        if (Bukkit.getPlayer(uniqueID).isOnline()) statusMap.put(uniqueID, Status.VERIFIED);
        cacheMap.put(uniqueID, Record.newCache(uniqueID, commitTime));
        refreshList.remove(uniqueID);
    }

    protected void loginPlayer(UUID uniqueID) {
        if (cacheMap.containsKey(uniqueID)) {
            if (cacheMap.get(uniqueID).getExpireTime() > System.currentTimeMillis()) {
                statusMap.put(uniqueID, Status.VERIFIED);
                return;
            } else {
                cacheMap.remove(uniqueID);
            }
        }
        try {
            PlayerStatus playerStatus = getStatusFromServer(uniqueID);
            if (playerStatus.getVerified()) {
                statusMap.put(uniqueID, Status.VERIFIED);
                cacheMap.put(uniqueID, Record.newCache(uniqueID, playerStatus.getCommitTime()));
            } else {
                statusMap.put(uniqueID, Status.NOT_VERIFIED);
            }
        } catch (IOException e) {
            logger.warning(e.toString());
            statusMap.put(uniqueID, Status.UNKNOWN);
            refreshList.add(uniqueID);
        }
    }

    protected void saveCache() throws IOException {
        long now = System.currentTimeMillis();
        for (UUID x : cacheMap.keySet()) {
            if (cacheMap.get(x).getExpireTime() < now) cacheMap.remove(x);
        }
        Cache cache = Cache.getCacheList(cacheMap);
        FileWriter writer = new FileWriter(cacheFile, false);
        writer.write(prettyGSON.toJson(cache));
        writer.flush();
        writer.close();
    }
}
