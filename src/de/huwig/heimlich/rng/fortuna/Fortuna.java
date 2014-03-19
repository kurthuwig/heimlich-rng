package de.huwig.heimlich.rng.fortuna;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;

/**
 * This file is part of the Heimlich project.
 * Read the file README.md for copyright and other information
 */
public class Fortuna {
    public static final int POOL_COUNT = 32;

    private static final int AES_KEYBITS = 256;
    private static final int AES_BLOCKBITS = 128;
    private static final int MIN_POOL_SIZE = 64;
    private static final int MIN_RESEED_DELAY_MS = 100;

    private final Cipher aes;
    private final MessageDigest sha256;
    private final byte[] key = new byte[AES_KEYBITS / 8];
    private final Counter counter = new Counter();
    private final Pool[] pools = new Pool[POOL_COUNT];

    private long reseedCount = 0;
    private long lastReseedTime = 0;

    public Fortuna() {
        try {
            aes = Cipher.getInstance("AES/ECB/NoPadding");
            sha256 = MessageDigest.getInstance("SHA-256");
            for (int i = 0; i < pools.length; i++) {
                pools[i] = new Pool();
            }
        } catch (Exception e) {
            throw new RuntimeException("cannot initialze", e);
        }
    }

    public void random(byte[] buffer) {
        checkBuffersSize(buffer);

        if (reseedAllowed()) {
            reseed();
        }
        if (!isSeeded()) {
            throw new IllegalArgumentException("not seeded");
        }
        fillBufferAndRekey(buffer);
    }

    private boolean isSeeded() {
        return 0 != reseedCount;
    }

    public boolean isInitialized() {
        return isSeeded() || reseedAllowed();
    }

    private void checkBuffersSize(byte[] buffer) {
        if (1 << 30 <= buffer.length) {
            throw new IllegalArgumentException("buffer too large");
        }
        if (0 != buffer.length % (AES_BLOCKBITS / 8)) {
            throw new IllegalArgumentException("invalid block length");
        }
    }

    private boolean reseedAllowed() {
        final Pool pool0 = pools[0];
        synchronized (pool0) {
            if (pool0.getLength() < MIN_POOL_SIZE) {
                return false;
            }
        }

        if (System.currentTimeMillis() - lastReseedTime < MIN_RESEED_DELAY_MS) {
            return false;
        }

        return true;
    }

    private void reseed() {
        reseedCount++;
        sha256.update(key);
        long divisor = 1;
        for (int poolNo = 0; poolNo < 32; poolNo++, divisor <<= 1) {
            if (0 != (reseedCount % divisor)) {
                break;
            }
            final Pool pool = pools[poolNo];
            synchronized (pool) {
                sha256.update(pool.getHash());
            }
        }
        System.arraycopy(sha256.digest(), 0, key, 0, key.length);
        counter.increment();
        lastReseedTime = System.currentTimeMillis();
    }

    private void fillBufferAndRekey(byte[] buffer) {
        try {
            aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("cannot set key", e);
        }
        generate(buffer);
        generate(key);
    }

    private void generate(byte[] buffer) {
        for (int pos = 0; pos < buffer.length - 1; pos += (AES_BLOCKBITS / 8)) {
            System.arraycopy(aes.update(counter.value),
                             0,
                             buffer,
                             pos,
                             AES_BLOCKBITS / 8);
            counter.increment();
        }
    }

    public void addRandomEvent(byte source,
                               int poolNo,
                               byte[] data, int offset, int length) {
        if (255 < data.length) {
            throw new IllegalArgumentException("data too long");
        }

        final Pool pool = pools[poolNo];
        synchronized (pool) {
            pool.addEvent(source, data, offset, length);
        }
    }
}
