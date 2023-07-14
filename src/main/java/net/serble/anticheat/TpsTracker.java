package net.serble.anticheat;

import java.util.ArrayList;
import java.util.List;

public class TpsTracker implements Runnable {
    public static List<Long> ticks = new ArrayList<>();

    public static double getTPS() {
        return getTPS(Config.getConfiguration().getInt("tps-sample-size"));
    }

    public static double getTPS(int sampleSize) {
        if (ticks.size() < sampleSize) {
            return 20;
        }
        long elapsed = getElapsed(sampleSize);
        return (double) sampleSize / (elapsed / 1000D);
    }

    public static long getElapsed(int tickID) {
        return ticks.get(ticks.size() - 1) - ticks.get(ticks.size() - tickID);
    }

    public void run() {
        ticks.add(System.currentTimeMillis());
        if (ticks.size() > Config.getConfiguration().getInt("tps-sample-size")) {
            ticks.remove(0);
        }
    }
}
