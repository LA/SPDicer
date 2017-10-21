package scripts.SPDicer.src;

import java.util.concurrent.TimeUnit;

/**
 * Created by Adar on 6/22/17.
 */
public class Util {
    public static String withSuffix(int count) {
        boolean isNegative = (count < 0);
        if (isNegative) { count = -count; }
        if (count < 1000 && count >= 0) return (isNegative) ? "-" + count : "" + count;

        int exp = (int) (Math.log(count) / Math.log(1000));

        String fmtCount = String.format("%d%c",
                (int) (count / Math.pow(1000, exp)),
                "kmb".charAt(exp-1)
        );

        return (isNegative) ? "-" + fmtCount : fmtCount;
    }

    public static String getRunTime(long duration) {

        String time;

        long days = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));

        if (days == 0) {
            time = (hours + ":" + minutes + ":" + seconds);
        } else {
            time = (days + ":" + hours + ":" + minutes + ":" + seconds);
        }

        return time;
    }
}
