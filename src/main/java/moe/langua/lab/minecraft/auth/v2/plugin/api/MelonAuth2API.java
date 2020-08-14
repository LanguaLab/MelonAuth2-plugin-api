package moe.langua.lab.minecraft.auth.v2.plugin.api;

import moe.langua.lab.minecraft.auth.v2.plugin.api.json.ChallengeDetail;
import moe.langua.lab.minecraft.auth.v2.plugin.api.json.ChallengeOverview;
import moe.langua.lab.minecraft.auth.v2.plugin.api.json.PlayerStatus;
import moe.langua.lab.minecraft.auth.v2.plugin.api.json.Settings;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class MelonAuth2API {
    private static final ExecutorService scheduler = Executors.newSingleThreadExecutor();
    private static final ConcurrentHashMap<Integer, StatusUpdateEvent> statusUpdateEvents = new ConcurrentHashMap<>();
    private static MelonAuth2API instance;
    private static Settings settings;
    private final ConcurrentHashMap<UUID, PlayerStatus> playerStatusConcurrentHashMap = new ConcurrentHashMap<>();
    private final HashSet<UUID> onlinePlayers = new HashSet<>();
    private final ConcurrentHashMap<Integer, LoginEvent> loginEvents = new ConcurrentHashMap<>();
    private final LinkedList<UUID> retryList = new LinkedList<>();

    protected MelonAuth2API(File configFile) throws IOException {
        settings = Settings.load(configFile);
        Settings.check(settings);
        Settings.save(settings, configFile);
        getPlayerStatusExact(UUID.fromString("f0e8b790-8539-45b0-a052-dc6b922208c5"));
        ScheduledExecutorService retryService = Executors.newSingleThreadScheduledExecutor();
        retryService.schedule(() -> {
            if (retryList.isEmpty()) return;
            UUID uniqueID = retryList.get(0);
            retryList.remove(0);
            try {
                PlayerStatus playerStatus = getPlayerStatusExact(uniqueID);
                if (onlinePlayers.contains(uniqueID)) playerStatusConcurrentHashMap.put(uniqueID, playerStatus);
            } catch (IOException e) {
                if (onlinePlayers.contains(uniqueID)) retryList.add(uniqueID);
            }
        }, settings.getRetryInterval(), TimeUnit.MILLISECONDS);
        instance = this;
    }

    private static BufferedReader getBufferedReaderFromURL(String urlString, String authorizationHeader) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setConnectTimeout(10000);
        httpURLConnection.setReadTimeout(10000);
        httpURLConnection.setRequestProperty("Authorization", authorizationHeader);
        InputStream inputStream = httpURLConnection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        int respCode = httpURLConnection.getResponseCode();
        if (respCode != 200) throw new IOException("Server returned HTTP" +
                " response code: " + respCode + " for URL: " +
                url.toString());
        return new BufferedReader(inputStreamReader);
    }

    public static int registerLoginEvent(LoginEvent event) {
        int i = 0;
        while (instance.loginEvents.containsKey(i)) i++;
        instance.loginEvents.put(i, event);
        return i;
    }

    public static boolean unregisterLoginEvent(int eventID) {
        return instance.loginEvents.remove(eventID) != null;
    }

    public static int registerPlayerStatusUpdateEvent(StatusUpdateEvent event) {
        int i = 0;
        while (statusUpdateEvents.containsKey(i)) i++;
        statusUpdateEvents.put(i, event);
        return i;
    }

    public static boolean unregisterPlayerStatusUpdateEvent(int eventID) {
        return statusUpdateEvents.remove(eventID) != null;
    }

    /**
     * Get the player status of an online player.
     *
     * @return a {@link PlayerStatus} instance if a player is online and api plugin works properly. {@code null} if the player is offline or network error occurred while the player is login.
     * @throws NullPointerException if MelonAuthAPI is not load properly.
     */
    public static PlayerStatus getPlayerStatus(UUID uniqueID) {
        return instance.playerStatusConcurrentHashMap.get(uniqueID);
    }

    public static URL getApiURL() {
        return settings.getApiURL();
    }

    public static URL getWebAppURL() {
        return settings.getWebappURL();
    }

    /**
     * Get the player status exactly.
     *
     * @return a {@link PlayerStatus} instance.
     * @throws IOException          if network error occurred.
     * @throws NullPointerException if MelonAuthAPI config file not load properly.
     */
    public static PlayerStatus getPlayerStatusExact(UUID uniqueID) throws IOException {
        BufferedReader reader = getBufferedReaderFromURL(settings.getApiURL() + "/get/status/" + uniqueID.toString(), getAuthorizationHeader());
        PlayerStatus status = Utils.prettyGSON.fromJson(reader, PlayerStatus.class);
        scheduler.submit(() -> {
            for (StatusUpdateEvent event : statusUpdateEvents.values()) {
                event.onPlayerStatusUpdate(uniqueID, status);
            }
        });
        return status;
    }

    private static String getAuthorizationHeader() {
        String authorizationString = settings.getServerName() + ":" + settings.getSecretKey();
        byte[] encodedAuthorization = Base64.getEncoder().encode(authorizationString.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuthorization, StandardCharsets.UTF_8);
    }

    protected LoginResult loginPlayer(UUID uniqueID) {
        onlinePlayers.add(uniqueID);
        PlayerStatus playerStatus;
        try {
            playerStatus = getPlayerStatusExact(uniqueID);
            if (onlinePlayers.contains(uniqueID))
                playerStatusConcurrentHashMap.put(uniqueID, playerStatus);
        } catch (IOException e) {
            retryList.add(uniqueID);
            playerStatus = null;
        }
        Collection<LoginEvent> tasks = loginEvents.values();
        LoginResult result = LoginResult.getDefault();
        for (LoginEvent event : tasks) {
            event.login(uniqueID, playerStatus, result);

        }
        return result;
    }

    protected void logOutPlayer(UUID uniqueID) {
        onlinePlayers.remove(uniqueID);
        playerStatusConcurrentHashMap.remove(uniqueID);
    }

    /**
     * Pull a challenge for a player.
     *
     * @return a {@link ChallengeOverview} instance.
     * @throws IOException          if network error occurred.
     * @throws NullPointerException if MelonAuthAPI config file not load properly.
     */
    public ChallengeOverview requireChallenge(UUID uniqueID) throws IOException {
        BufferedReader reader = getBufferedReaderFromURL(settings.getApiURL() + "/require/" + uniqueID.toString(), getAuthorizationHeader());
        return Utils.prettyGSON.fromJson(reader, ChallengeOverview.class);
    }

    /**
     * Get the challenge detail of a player.
     *
     * @return a {@link ChallengeDetail} instance.
     * @throws IOException          if network error occurred.
     * @throws NullPointerException if MelonAuthAPI config file not load properly.
     */
    public ChallengeDetail getChallengeDetail(UUID uniqueID) throws IOException {
        BufferedReader reader = getBufferedReaderFromURL(settings.getApiURL() + "/get/challenge/" + uniqueID.toString(), getAuthorizationHeader());
        return Utils.prettyGSON.fromJson(reader, ChallengeDetail.class);
    }


}
