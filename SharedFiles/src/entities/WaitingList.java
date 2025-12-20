package entities;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class WaitingList implements Serializable {

    private static final long serialVersionUID = 1L;

    private Queue<String> guestsQueue = new LinkedList<>();

    public WaitingList() {}

    public Queue<String> getGuestsQueue() {
        return guestsQueue;
    }

    public int getQueueSize() {
        return guestsQueue.size();
    }
}
