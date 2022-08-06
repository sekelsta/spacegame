package sekelsta.engine;

import java.util.Locale;

public class DataFolders {
    private static String userFolder = null;
    private static String userMachineFolder = null;

    public static void init(String appName) {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (os.startsWith("windows")) {
            // Java will interpret a forward slash as a backslash if necessary
            userMachineFolder = System.getenv("localappdata") + "/" + appName;
            userFolder = System.getenv("appdata") + "/" + appName;
            return;
        }
        // Else, assume *nix
        if (os.startsWith("mac os x")) {
            userFolder = System.getProperty("user.home") + "/Library/Application Support/" + appName;
            userMachineFolder = userFolder + "_local";
        }
        else {
            userMachineFolder = System.getProperty("user.home") + "/.local/share/" + appName;
            userFolder = System.getProperty("user.home") + "/." + appName;
        }
    }

    // For user-specific but not computer-specific files, like save data
    public static String getUserFolder() {
        return userFolder;
    }

    // For user-specific but not computer-specific files, like save data
    public static String getUserFolder(String path) {
        return userFolder + "/" + path;
    }

    // For user-specific and computer-specific files, like window settings
    public static String getUserMachineFolder() {
        return userMachineFolder;
    }

    // For user-specific and computer-specific files, like window settings
    public static String getUserMachineFolder(String path) {
        return userMachineFolder + "/" + path;
    }
}
