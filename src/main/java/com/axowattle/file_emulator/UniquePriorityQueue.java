package com.axowattle.file_emulator;

import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings({"UnusedReturnValue", "BooleanMethodIsAlwaysInverted"})
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
    public @NotNull Iterator<E> iterator() {
        return pq.iterator();
    }

    public int size(){
        return pq.size();
    }
}
