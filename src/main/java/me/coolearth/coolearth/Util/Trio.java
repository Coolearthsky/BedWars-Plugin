package me.coolearth.coolearth.Util;

public class Trio<T, U, V> {
    private final T first;
    private final U second;
    private final V third;

    public Trio(T first, U second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    public V getThird() {
        return third;
    }

    @Override
    public String toString() {
        return "First: " + first + " Second: "+ second + " Third: " + third;
    }

    public boolean assertEquals(Trio<T, U, V> trio) {
        return (trio.getFirst() == first) && (trio.getSecond() == second) && (trio.getThird() == third);
    }
}