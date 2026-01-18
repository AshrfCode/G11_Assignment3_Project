package entities;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Represents a waiting list for guests.
 * <p>
 * This class maintains a FIFO queue of guest identifiers (stored as {@link String}s).
 * It is {@link Serializable} to support persistence and/or transfer between application layers.
 * </p>
 */
public class WaitingList implements Serializable {

    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** FIFO queue holding guest identifiers in the waiting list order. */
    private Queue<String> guestsQueue = new LinkedList<>();

    
    /**
     * Constructs an empty waiting list instance.
     * <p>
     * Intended for frameworks/serialization tools that require a no-args constructor.
     * </p>
     */
    public WaitingList() {}

    /**
     * Returns the underlying guests queue.
     * <p>
     * Modifications to the returned queue will affect this waiting list.
     * </p>
     *
     * @return the guests queue
     */
    public Queue<String> getGuestsQueue() {
        return guestsQueue;
    }

    /**
     * Returns the current number of guests in the waiting list.
     *
     * @return the queue size
     */
    public int getQueueSize() {
        return guestsQueue.size();
    }
}
