package com.TNTStudios.playerrevivefabric.client;

public class PlayerReviveClientState {
    private static boolean downed = false;

    public static boolean isDowned() {
        return downed;
    }

    public static void setDowned(boolean value) {
        downed = value;
    }
}
