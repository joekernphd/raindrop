package com.example.raymond.raindrop.logic;

/**
 * Created by Raymond on 11/5/2017.
 */

public final class LatLongDistance {
    private static final double TOO_FAR = 0.01;
    private static final double FAR_ENOUGH = 0.05;
    private LatLongDistance() {
        throw new AssertionError("Dont instantiate this.");
    }

    public static boolean isClose(double latA, double longA, double latB, double longB) {
        if(Math.abs(latA-latB) < TOO_FAR && Math.abs(longA-longB) < TOO_FAR) {
            return true;
        }

        return false;
    }

    public static boolean isCloseEnough(double latA, double longA, double latB, double longB) {
        if(Math.abs(latA-latB) < FAR_ENOUGH && Math.abs(longA-longB) < FAR_ENOUGH) {
            return true;
        }

        return false;
    }
}
