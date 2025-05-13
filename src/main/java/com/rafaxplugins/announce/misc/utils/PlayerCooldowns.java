package com.rafaxplugins.announce.misc.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;

public class PlayerCooldowns {

    public static final Table<String, String, Long> COOLDOWNS = HashBasedTable.create();

    public static void start(Player player, String key, long value, TimeUnit unit) {
        COOLDOWNS.put(player.getName(), key, System.currentTimeMillis() + unit.toMillis(value));
    }

    public static boolean end(Player user, String key) {
        return COOLDOWNS.remove(user.getName(), key) != null;
    }

    public static boolean hasEnded(Player user, String key) {
        String name = user.getName();

        Long cooldown = COOLDOWNS.get(name, key);

        if (cooldown == null) {
            return true;
        }

        if (cooldown <= System.currentTimeMillis()) {
            COOLDOWNS.remove(name, key);
            return true;
        }

        return false;
    }

    public static long getMillisLeft(Player user, String key) {
        return hasEnded(user, key) ? 0L : COOLDOWNS.get(user.getName(), key) - System.currentTimeMillis();
    }

    public static int getSecondsLeft(Player user, String key) {
        return hasEnded(user, key) ? 0 : ((int) TimeUnit.MILLISECONDS.toSeconds(getMillisLeft(user, key))) + 1;
    }

    public static String getFormattedTimeLeft(Player user, String key) {
        return getFormattedTimeLeft(getMillisLeft(user, key));
    }

    public static String getFormattedTimeLeft(long millis) {
        if (millis < 0) {
            return "";
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);

        if (days > 0) {
            sb.append(days).append("d ");
        }

        if (hours > 0) {
            sb.append(hours).append("h ");
        }

        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }

        if (days == 0 && hours == 0 && minutes == 0 && seconds == 0 && millis < 1000) {

            double halfsec = millis / 1000.0;

            NumberFormat nf = new DecimalFormat("#.#");
            String value = nf.format(new BigDecimal(halfsec).setScale(1, RoundingMode.CEILING).doubleValue());

            sb.append(value).append("s");

        } else if (seconds > 0) {
            sb.append(seconds).append("s");
        }

        return (sb.toString().trim());
    }
}
