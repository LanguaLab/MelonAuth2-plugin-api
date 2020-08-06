package moe.langua.lab.minecraft.auth.v2.plugin.api;

import moe.langua.lab.minecraft.auth.v2.plugin.api.json.PlayerStatus;
import moe.langua.lab.minecraft.auth.v2.plugin.api.json.Settings;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MelonAuth2API {
    private static MelonAuth2API instance;
    private final ConcurrentHashMap<UUID, PlayerStatus> playerStatusConcurrentHashMap = new ConcurrentHashMap<>();
    private final HashSet<UUID> onlinePlayers = new HashSet<>();
    private final ConcurrentHashMap<Integer, LoginEvent> loginEvents = new ConcurrentHashMap<>();
    private final LinkedList<UUID> retryList = new LinkedList<>();
    private final File configFile;
    private final Settings settings;
    private ScheduledExecutorService retryService = Executors.newSingleThreadScheduledExecutor();

    public MelonAuth2API(File configFile) throws IOException {
        this.configFile = configFile;
        settings = Settings.load(configFile);
        Settings.check(settings);
        Settings.save(settings, configFile);
        getPlayerStatusExact(UUID.fromString("f0e8b790-8539-45b0-a052-dc6b922208c5"));
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

    public static PlayerStatus getPlayerStatus(UUID uniqueID) {
        return instance.playerStatusConcurrentHashMap.get(uniqueID);
    }

    protected void loginPlayer(UUID uniqueID) {
        onlinePlayers.add(uniqueID);
        PlayerStatus playerStatus;
        try {
            playerStatus = getPlayerStatusExact(uniqueID);
            if (onlinePlayers.contains(uniqueID)) ;
            playerStatusConcurrentHashMap.put(uniqueID, playerStatus);
        } catch (IOException e) {
            retryList.add(uniqueID);
            playerStatus = null;
        }
        Collection<LoginEvent> tasks = loginEvents.values();
        for (LoginEvent event : tasks) {
            event.login(uniqueID, playerStatus);
        }
    }

    protected void logOutPlayer(UUID uniqueID) {
        onlinePlayers.remove(uniqueID);
        playerStatusConcurrentHashMap.remove(uniqueID);
    }

    public PlayerStatus getPlayerStatusExact(UUID uniqueID) throws IOException {
        BufferedReader reader = getBufferedReaderFromURL(settings.getApiURL() + "/get/status/" + uniqueID.toString(), getAuthorizationHeader());
        return Utils.prettyGSON.fromJson(reader, PlayerStatus.class);
    }

    public ChallengeOverview getChallenge(UUID uniqueID) throws IOException {
        BufferedReader reader = getBufferedReaderFromURL(settings.getApiURL() + "/require/" + uniqueID.toString(), getAuthorizationHeader());
        return Utils.prettyGSON.fromJson(reader, ChallengeOverview.class);
    }

    private String getAuthorizationHeader() {
        String authorizationString = settings.getServerName() + ":" + settings.getSecretKey();
        byte[] encodedAuthorization = Base64.getEncoder().encode(authorizationString.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuthorization, StandardCharsets.UTF_8);
    }
}
