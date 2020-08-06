package moe.langua.lab.minecraft.auth.v2.plugin.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.SecureRandom;

public class Utils {
    public static final Gson prettyGSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final char[] charSets = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
    public static String getRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();
        for (int times = 0; times < length; times++) {
            stringBuilder.append(charSets[random.nextInt(62)]);
        }
        return stringBuilder.toString();
    }

    public static String removeSlashAtTheEnd(String target) {
        while (target.endsWith("/")) {
            target = target.substring(0, target.length() - 1);
        }
        return target;
    }
}
