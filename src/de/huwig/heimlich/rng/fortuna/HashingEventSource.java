package de.huwig.heimlich.rng.fortuna;

import android.util.Log;

import java.security.MessageDigest;

/**
 * This file is part of the Heimlich project.
 * Read the file README.md for copyright and other information
 */
public class HashingEventSource extends EventSource {

    private final MessageDigest sha256;
    private final int chunkSize;

    public HashingEventSource(Fortuna fortuna, byte sourceId, int chunkSize) {
        super(fortuna, sourceId);
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
                fortuna.addRandomEvent(sourceId,
                                       getNextPoolNo(),
                                       sha256.digest(), 0, 32);
            }
        } catch (Exception e) {
            Log.e("RNG", "Error", e);
            return;
        }
    }
}
