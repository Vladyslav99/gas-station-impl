package com.testtask.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AtomicDoubleTest {

    @Test
    void shouldCorrectAdd() {
        AtomicDouble atomicDouble = new AtomicDouble();

        atomicDouble.add(2.2);

        assertEquals(2.2, atomicDouble.get());
    }
}
