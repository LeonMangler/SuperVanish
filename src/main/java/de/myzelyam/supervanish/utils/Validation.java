package de.myzelyam.supervanish.utils;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public abstract class Validation {

    public static void checkNotNull(Object... objects) {
        checkNotNull(null, objects);
    }

    public static void checkIsTrue(boolean... bool) {
        checkIsTrue(null, bool);
    }

    public static void checkNotNull(String message, Object... objects) {
        for (Object obj : objects) {
            if (obj == null) {
                if (message != null)
                    log(Level.SEVERE, message);
                throw new IllegalArgumentException(message == null ? "" : message);
            }
        }
    }


    public static void checkIsTrue(String message, boolean... booleans) {
        for (boolean bool : booleans) {
            if (!bool) {
                if (message != null)
                    log(Level.SEVERE, message);
                throw new IllegalArgumentException(message == null ? "" : message);
            }
        }
    }

    private static void log(Level level, String message) {
        try {
            Class.forName("org.bukkit.plugin.java.JavaPlugin");
            Bukkit.getLogger().log(level, message);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
