package de.huwig.heimlich.rng.fortuna;

import android.util.Log;

import java.security.MessageDigest;

/**
 * This file is part of the Heimlich project.
 * Read the file README.md for copyright and other information
 */
public class EventSource {

    private final Fortuna fortuna;
    private final MessageDigest sha256;
    private final byte sourceId;
    private final int chunkSize;

    private int eventCount = 0;

    public EventSource(Fortuna fortuna, byte sourceId, int chunkSize) {
        this.fortuna = fortuna;
        this.sourceId = sourceId;
        this.chunkSize = chunkSize;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new RuntimeException("cannot initialze", e);
        }
    }

    public void addRandomEvents(byte[] data) {
        addRandomEvents(data, 0, data.length);
    }

    public void addRandomEvents(byte[] data, int offset, int length) {
        try {
            final int chunks = length / chunkSize;
            for (int i = 0, pos = offset; i < chunks ; i++, pos += chunkSize) {
                sha256.update(data, pos, chunkSize);
                final int poolNo;
                synchronized (this) {
                    poolNo = (eventCount++) % Fortuna.POOL_COUNT;
                }
                fortuna.addRandomEvent(sourceId, poolNo, sha256.digest());
            }
        } catch (Exception e) {
            Log.e("RNG", "Error", e);
            return;
        }
    }

    public synchronized int getEventCount() {
        return eventCount;
    }
}
