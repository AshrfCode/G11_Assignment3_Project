package client;

import java.util.function.Consumer;

public class ClientSession {

    public static Consumer<Object> activeHandler = null;

    // Logged-in user info (for Subscriber auto-fill)
    public static int userId = -1;
    public static String userRole = "";
    public static String userName = "";
    public static String userEmail = "";
    public static String userPhone = "";

    public static void clear() {
        activeHandler = null;
        userId = -1;
        userRole = "";
        userName = "";
        userEmail = "";
        userPhone = "";
    }

    public static boolean isLoggedIn() {
        return userId > 0;
    }

    private ClientSession() {}
}