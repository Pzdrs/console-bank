package bohac.ui;

import java.util.AbstractMap;
import java.util.function.Supplier;

public class Menu {
    /**
     * This objects represents a single menu item within a menu
     */
    public static class MenuItem {
        /**
         * Represents a result of a submitted option in a menu
         *
         * @param exitMenu whether the used exits the menu afterwards
         * @param message  result message
         */
        public record Result(boolean exitMenu, String message) {
        }

        private final String description;
        private Supplier<Result> resultSupplier;
        private Runnable action;
        private boolean exitMenuAfter = false;
        private boolean clearBefore = true;

        /**
         * @param description language key
         * @param action      what happens should a user choose this option
         */
        public MenuItem(String description, Runnable action) {
            this.description = description;
            this.action = action;
        }

        /**
         * @param description    language key
         * @param resultSupplier result
         */
        public MenuItem(String description, Supplier<Result> resultSupplier) {
            this.description = description;
            this.resultSupplier = resultSupplier;
        }

        /**
         * This constructor is used when creating a menu item, that is supposed to do nothing (i.e. Log out - exitMenuAfter() is
         * applied - does nothing, but jumps to a lower menu, in this case there is none, so it logs you out)
         *
         * @param description language key
         */
        public MenuItem(String description) {
            this.description = description;
        }

        /**
         * Set the auto clear functionality
         *
         * @param clearBefore boolean
         */
        public MenuItem clearBefore(boolean clearBefore) {
            this.clearBefore = clearBefore;
            return this;
        }

        /**
         * The user exits the menu after the option action has taken place
         */
        public MenuItem exitMenuAfter() {
            this.exitMenuAfter = true;
            return this;
        }

        /**
         * This method runs every time, an option is chosen
         *
         * @return result
         */
        public Result run() {
            if (clearBefore) TerminalUtils.clear();
            if (action != null) action.run();
            if (resultSupplier != null) {
                return resultSupplier.get();
            }
            return new Result(exitMenuAfter, null);
        }
    }

    private static final LanguageManager LANGUAGE_MANAGER = TerminalSession.languageManager;

    /**
     * Menu item representing an option to go back. Needs to be a method instead of a static final member,
     * because it wouldn't update its description according to the current language. Instantiated once and never
     * changed => undesired behaviour
     */
    public static MenuItem getBackItem() {
        return new MenuItem("menu_back").exitMenuAfter();
    }

    /**
     * Menu with a single option - Go back - primarily used to keep something on the console before the user is done with it
     */
    public static final Menu BACK_ONLY = new Menu(getBackItem()).dontClear();
    private final MenuItem[] menuItems;
    private boolean clearBeforePrompt = true;


    public Menu(MenuItem... menuItems) {
        this.menuItems = menuItems;
    }

    /**
     * Disables the auto clear functionality
     */
    public Menu dontClear() {
        this.clearBeforePrompt = false;
        return this;
    }

    /**
     * Runs some predefined code + prints out the result of the last chosen option + prompts the user for the next choice
     *
     * @param beforeEach Runnable definition
     */
    public void prompt(Runnable beforeEach, String lastResult) {
        if (menuItems.length == 0) return;
        if (clearBeforePrompt) TerminalUtils.clear();
        if (beforeEach != null) beforeEach.run();
        if (lastResult != null) {
            System.out.println();
            System.out.println(lastResult);
        }
        System.out.println();
        System.out.printf("%s: \n", LANGUAGE_MANAGER.getString("menu_choose"));
        for (int i = 0; i < menuItems.length; i++) {
            System.out.printf("[%d] - %s\n", i + 1, LANGUAGE_MANAGER.getString(menuItems[i].description));
        }
        MenuItem.Result result = menuItems[TerminalUtils.promptNumericInt("> ", new AbstractMap.SimpleEntry<>(1, menuItems.length), LANGUAGE_MANAGER) - 1].run();
        if (!result.exitMenu()) prompt(beforeEach, result.message());
    }

    /**
     * Prompts the user for a choice
     */
    public void prompt() {
        prompt(null, null);
    }

    /**
     * Runs some predefined code + prompts the user for the next choice
     *
     * @param beforeEach Runnable definition
     */
    public void prompt(Runnable beforeEach) {
        prompt(beforeEach, null);
    }
}