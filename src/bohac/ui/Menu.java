package bohac.ui;

import java.util.AbstractMap;
import java.util.function.Supplier;

/**
 * This record represents an interactive menu
 *
 * @param menuItems menu options
 */
public record Menu(MenuItem... menuItems) {
    private static final LanguageManager LANGUAGE_MANAGER = TerminalSession.languageManager;
    public static final MenuItem BACK_ITEM = new MenuItem("menu_back", () -> false);
    public static final Menu BACK_ONLY = new Menu(BACK_ITEM);

    /**
     * This objects represents a single menu item within a menu
     */
    public static class MenuItem {
        private final String description;
        private final Supplier<Boolean> action;
        private boolean clearBefore = true;

        /**
         * @param description language key
         * @param action      what happens should a user choose this option,
         *                    returns, whether this menu prompts the user again, after
         *                    the action has taken place
         */
        public MenuItem(String description, Supplier<Boolean> action) {
            this.description = LANGUAGE_MANAGER.getString(description);
            this.action = action;
        }

        /**
         * @param description language key
         * @param action      what happens should a user choose this option, this menu
         *                    will prompt the user again after the action has taken place
         */
        public MenuItem(String description, Runnable action) {
            this(description, () -> {
                action.run();
                return true;
            });
        }

        /**
         * @param description language key
         * @param menu        submenu
         * @param beforeEach  code that runs every prompt iteration
         */
        public MenuItem(String description, Menu menu, Runnable beforeEach) {
            this(description, () -> {
                menu.prompt(beforeEach);
                return true;
            });
        }

        /**
         * @param description language key
         * @param menu        submenu
         */
        public MenuItem(String description, Menu menu) {
            this(description, () -> {
                menu.prompt();
                return true;
            });
        }

        public MenuItem clearBefore(boolean clearBefore) {
            this.clearBefore = clearBefore;
            return this;
        }

        /**
         * This method runs every time, an option is chosen
         *
         * @return whether the menu prompts the user again, after the action has taken place
         */
        public boolean run() {
            if (clearBefore) TerminalUtils.clear();
            boolean continuePrompt = false;
            if (action != null) continuePrompt = action.get();
            return continuePrompt;
        }
    }

    /**
     * Runs some predefined code + prompts the user for a choice
     *
     * @param beforeEach Runnable definition
     */
    public void prompt(Runnable beforeEach, boolean clear) {
        if (clear) TerminalUtils.clear();
        if (beforeEach != null) beforeEach.run();
        if (menuItems.length == 0) return;
        System.out.println();
        System.out.printf("%s: \n", LANGUAGE_MANAGER.getString("menu_choose"));
        for (int i = 0; i < menuItems.length; i++) {
            System.out.printf("[%d] - %s\n", i + 1, menuItems[i].description);
        }
        if (menuItems[TerminalUtils.promptNumericInt("> ", new AbstractMap.SimpleEntry<>(1, menuItems.length), LANGUAGE_MANAGER) - 1].run())
            prompt(beforeEach, clear);
    }

    /**
     * Prompts the user for a choice
     */
    public void prompt() {
        prompt(null, true);
    }

    public void prompt(boolean clear) {
        prompt(null, clear);
    }

    public void prompt(Runnable runnable) {
        prompt(runnable, true);
    }
}