package przyklady06;

import java.util.LinkedList;
import java.util.List;

public class BlockingQueue<T> {

    private final List<T> queue = new LinkedList();
    private final int capacity;

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized T take() throws InterruptedException {
        while (this.queue.size() == 0) {
            wait();
        }

        if (this.queue.size() == this.capacity) {
            notifyAll();
        }

        return this.queue.remove(0);
    }

    public synchronized void put(T item) throws InterruptedException {
        while (this.queue.size() == this.capacity) {
            wait();
        }

        this.queue.add(item);

        if (this.queue.size() == 1) {
            notifyAll();
        }
    }

    public synchronized int getSize() {
        return this.queue.size();
    }

    public int getCapacity() {
        return capacity;
    }
}