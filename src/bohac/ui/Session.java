package bohac.ui;

import bohac.entity.User;

/**
 * The {@code Session} interface represents an established communication bridge with the backend infrastructure.
 */
public interface Session {
    /**
     * The onLogin hook
     *
     * @param user the logged-in user
     */
    void onLogin(User user);

    /**
     * The onLogout hook
     */
    void onLogout(User user);

    /**
     * @return whether the session is active
     */
    boolean isActive();

    /**
     * Ends the session
     */
    void endSession();
}
