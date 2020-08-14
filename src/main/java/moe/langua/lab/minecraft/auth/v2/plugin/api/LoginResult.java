package moe.langua.lab.minecraft.auth.v2.plugin.api;

public class LoginResult {
    private boolean allowLogin = true;
    private String kickedMessage = "";

    public static LoginResult getDefault() {
        return new LoginResult();
    }

    public boolean isAllowLogin() {
        return allowLogin;
    }

    public void setAllowLogin(boolean allowLogin) {
        this.allowLogin = allowLogin;
    }

    public String getKickedMessage() {
        return kickedMessage;
    }

    public void setKickedMessage(String kickedMessage) {
        this.kickedMessage = kickedMessage;
    }
}
