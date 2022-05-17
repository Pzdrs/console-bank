package bohac.ui;

import java.util.AbstractMap;
import java.util.function.Supplier;

public record Menu(MenuItem... menuItems) {
    private static LanguageManager languageManager = TerminalSession.languageManager;

    public static class MenuItem {
        private final String description;
        private final Supplier<Boolean> action;

        public MenuItem(String description, Supplier<Boolean> action) {
            this.description = languageManager.getString(description);
            this.action = action;
        }

        public MenuItem(String description, Runnable action) {
            this(description, () -> {
                action.run();
                return true;
            });
        }

        public boolean run() {
            TerminalUtils.clear();
            boolean continuePrompt = false;
            if (action != null) continuePrompt = action.get();
            return continuePrompt;
        }
    }

    /**
     * Defines what happens before each menu iteration, i.e. printing a header text, etc.
     *
     * @param beforeEach Runnable definition
     */
    public void prompt(Runnable beforeEach) {
        TerminalUtils.clear();
        if (beforeEach != null) beforeEach.run();
        System.out.println();
        System.out.println("Choose an option: ");
        for (int i = 0; i < menuItems.length; i++) {
            System.out.printf("[%d] - %s\n", i + 1, menuItems[i].description);
        }
        if (menuItems[TerminalUtils.promptNumericInt("> ", new AbstractMap.SimpleEntry<>(1, menuItems.length)) - 1].run())
            prompt(beforeEach);
    }
}