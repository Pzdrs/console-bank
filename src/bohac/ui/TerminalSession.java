package bohac.ui;

import bohac.Configuration;
import bohac.Utils;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Scanner;

public class TerminalSession {
    public static final Scanner SCANNER = new Scanner(System.in);
    public static final LanguageManager languageManager = LanguageManager.getInstance();
    private boolean active;

    private TerminalSession() {
        this.active = true;
    }

    public static TerminalSession createSession() {
        languageManager.load(Path.of(Configuration.DATA_ROOT.toString(), languageManager.getLocale().toLanguageTag() + ".yaml"));
        return new TerminalSession();
    }

    public void endSession() {

    }

    public String promptUsername() {
        System.out.printf("%s: ", Utils.getMessage("login_prompt_username"));
        return SCANNER.next();
    }

    public String promptPassword() {
        System.out.printf("%s: ", Utils.getMessage("login_prompt_password"));
        return SCANNER.next();
    }

    public void userNotFound(String login) {
        System.out.printf("%s\n", Utils.getMessage("login_user_not_found").replace("{user}", login));
    }

    public void wrongPassword(int triesLeft) {
        System.out.printf("%s\n", Utils.getMessage(triesLeft == 0 ? "login_wrong_password_timeout" : "login_wrong_password")
                .replace("{tries}", String.valueOf(triesLeft)));
    }

    public void stillOnTimeout(long timeLeft) {
        long secondsLeft = Duration.ofMillis(timeLeft).toSeconds();
        System.out.printf("%s\n", Utils.getMessage("login_still_timeout")
                .replace("{duration}", String.format("%dh%02dm%02ds", secondsLeft / 3600, (secondsLeft % 3600) / 60, (secondsLeft % 60))));
    }

    public boolean isActive() {
        return active;
    }
}
