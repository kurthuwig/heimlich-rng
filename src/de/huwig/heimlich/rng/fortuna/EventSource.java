package de.huwig.heimlich.rng.fortuna;

/**
 * This file is part of the Heimlich project.
 * Read the file README.md for copyright and other information
 */
public abstract class EventSource {
    protected final Fortuna fortuna;
    protected final byte sourceId;
    protected int eventCount = 0;

    protected EventSource(Fortuna fortuna, byte sourceId) {
        this.fortuna = fortuna;
        this.sourceId = sourceId;
    }

    public abstract void addRandomEvents(byte[] data);

    public abstract void addRandomEvents(byte[] data, int offset, int length);

    public synchronized int getEventCount() {
        return eventCount;
    }

    public synchronized int getNextPoolNo() {
        synchronized (this) {
            return (eventCount++) % Fortuna.POOL_COUNT;
        }
    }
}
