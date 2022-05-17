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

    /**
     * This objects represents a single menu item within a menu
     */
    public static class MenuItem {
        private final String description;
        private final Supplier<Boolean> action;

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
         * This method runs every time, an option is chosen
         *
         * @return whether the menu prompts the user again, after the action has taken place
         */
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
        System.out.printf("%s: \n", LANGUAGE_MANAGER.getString("menu_choose"));
        for (int i = 0; i < menuItems.length; i++) {
            System.out.printf("[%d] - %s\n", i + 1, menuItems[i].description);
        }
        if (menuItems[TerminalUtils.promptNumericInt("> ", new AbstractMap.SimpleEntry<>(1, menuItems.length), LANGUAGE_MANAGER) - 1].run())
            prompt(beforeEach);
    }
}