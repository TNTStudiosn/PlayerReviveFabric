package com.TNTStudios.playerrevivefabric.client;

public class ReviveClientHooks {

    private static ReviveClientCallbacks callbacks;

    public interface ReviveClientCallbacks {
        void updateReviveTimer(int ticks);
    }

    public static void registerCallbacks(ReviveClientCallbacks impl) {
        callbacks = impl;
    }

    public static void updateReviveTimer(int ticks) {
        if (callbacks != null) {
            callbacks.updateReviveTimer(ticks);
        }
    }
}
