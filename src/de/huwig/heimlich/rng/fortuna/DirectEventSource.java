package de.huwig.heimlich.rng.fortuna;

/**
 * This file is part of the Heimlich project.
 * Read the file README.md for copyright and other information
 */
public class DirectEventSource extends EventSource {

    public DirectEventSource(Fortuna fortuna, byte sourceId) {
        super(fortuna, sourceId);
    }

    @Override
    public void addRandomEvents(byte[] data) {
        addRandomEvents(data, 0, data.length);
    }

    @Override
    public void addRandomEvents(byte[] data, int offset, int length) {
        fortuna.addRandomEvent(sourceId, getNextPoolNo(), data, offset, length);
    }

    @Override
    public synchronized int getEventCount() {
        return eventCount;
    }
}
