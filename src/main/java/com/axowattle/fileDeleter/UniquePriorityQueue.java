package com.axowattle.fileDeleter;

import java.util.*;

class UniquePriorityQueue<E> implements Iterable<E>{
    private final PriorityQueue<E> pq;
    private final Set<E> seen;

    public UniquePriorityQueue(Comparator<? super E> comparator) {
        this.pq = new PriorityQueue<>(comparator);
        this.seen = new HashSet<>();
    }

    public boolean add(E e) {
        if (seen.add(e)) {
            pq.add(e);
            return true;
        }
        return false;
    }

    public E poll() {
        E e = pq.poll();
        if (e != null) seen.remove(e);
        return e;
    }

    public boolean isEmpty() { return pq.isEmpty(); }


    @Override
    public Iterator<E> iterator() {
        return pq.iterator();
    }

    public int size(){
        return pq.size();
    }
}
