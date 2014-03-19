package de.huwig.heimlich.rng.fortuna;

import java.security.MessageDigest;

/**
 * This file is part of the Heimlich project.
 * Read the file README.md for copyright and other information
 */
public class Pool {
    private final MessageDigest sha256;
    private final byte[] prefix = new byte[2];
    private int length = 0;

    public Pool() {
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new RuntimeException("cannot initialze", e);
        }
    }

    public void addEvent(byte source, byte[] data, int offset, int dataLength) {
        prefix[0] = source;
        prefix[1] = (byte) data.length;
        sha256.update(prefix);
        sha256.update(data, offset, dataLength);

        length += dataLength + 2;
    }

    public int getLength() {
        return length;
    }

    public byte[] getHash() {
        length = 0;
        return sha256.digest();
    }
}
