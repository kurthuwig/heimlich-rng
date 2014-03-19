package de.huwig.heimlich.rng.fortuna;

/**
 * This file is part of the Heimlich project.
 * Read the file README.md for copyright and other information
 */
public class Counter {
    public final byte[] value = new byte[16];

    public void increment() {
        for (int i = 0; i < value.length; i++) {
            if (0 != ++value[i]) {
                break;
            }
        }
    }
}
