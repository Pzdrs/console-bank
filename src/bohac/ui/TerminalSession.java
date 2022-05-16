package bohac.ui;

import bohac.Configuration;
import bohac.entity.User;

import java.time.Duration;
import java.util.Locale;
import java.util.Scanner;

import static bohac.ui.TerminalUtils.*;

public class TerminalSession {
    public static final Scanner SCANNER = new Scanner(System.in);
    public static final LanguageManager languageManager = LanguageManager.getInstance();
    private boolean active;

   /*         ACCOUNT_MENU = new Menu(
                    new Menu.MenuItem("menu_select_account"),
                    new Menu.MenuItem("menu_open_new_account"),
                    new Menu.MenuItem("menu_back")
            );
*/

    private TerminalSession() {
        this.active = true;
    }

    public static TerminalSession createSession() {
        languageManager.setLocale(Configuration.DEFAULT_LANGUAGE);
        return new TerminalSession();
    }

    public void endSession() {
        this.active = false;
    }

    public void onLogin(User user) {
        String userLocale = user.getPreferences().getProperty("locale");
        if (userLocale != null) languageManager.setLocale(new Locale(userLocale));

        String dashboardSidePadding = TerminalUtils.ws(10);
        String dashboardDisplay = String.format("%s>>> %s <<<%s",
                dashboardSidePadding,
                languageManager.getString("user_dashboard"),
                dashboardSidePadding);
        String welcomeMessage = languageManager.getString("user_welcome", "user", user.getFullName());
        int i = dashboardDisplay.length() - welcomeMessage.length();

        new Menu(
                new Menu.MenuItem("menu_accounts", () -> {

                }),
                new Menu.MenuItem("menu_my_preferences", () -> {

                }),
                new Menu.MenuItem("menu_logout", () -> false),
                new Menu.MenuItem("menu_logout_exit", () -> {
                    endSession();
                    return false;
                })
        ).prompt(() -> {
            clear();
            println(dashboardDisplay);
            println(TerminalUtils.repeat("=", dashboardDisplay.length()));
            println(ws(i / 2) + welcomeMessage + ws(i / 2));
            println("");
        });
        logout();
    }

    private void logout() {
        clear();
        println(languageManager.getString(isActive() ? "user_logout" : "user_logout_exit"));
    }

    public String promptUsername() {
        System.out.printf("%s: ", languageManager.getString("login_prompt_username"));
        return SCANNER.nextLine().trim().split(" ")[0];
    }

    public String promptPassword() {
        System.out.printf("%s: ", languageManager.getString("login_prompt_password"));
        return SCANNER.nextLine().trim().split(" ")[0];
    }

    public void userNotFound(String login) {
        System.out.printf("%s\n", languageManager.getString("login_user_not_found", "user", login));
    }

    public void wrongPassword(int triesLeft) {
        System.out.printf("%s\n", languageManager.getString(triesLeft == 0 ? "login_wrong_password_timeout" : "login_wrong_password", "tries", String.valueOf(triesLeft)));
    }

    public void stillOnTimeout(long timeLeft) {
        long secondsLeft = Duration.ofMillis(timeLeft).toSeconds();
        System.out.printf("%s\n", languageManager.getString("login_still_timeout")
                .replace("{duration}", String.format("%dh%02dm%02ds", secondsLeft / 3600, (secondsLeft % 3600) / 60, (secondsLeft % 60))));
    }

    public boolean isActive() {
        return active;
    }
}
