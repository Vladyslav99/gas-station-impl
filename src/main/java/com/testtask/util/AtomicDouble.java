package com.testtask.util;

import java.util.concurrent.atomic.DoubleAdder;

public class AtomicDouble {

    private static final double DEFAULT_INITIAL_VALUE = 0.0;
    private final DoubleAdder adder;

    public AtomicDouble() {
        adder = new DoubleAdder();
        adder.add(DEFAULT_INITIAL_VALUE);
    }

    public double get() {
        return adder.sum();
    }

    public void add(double value) {
        adder.add(value);
    }
}
