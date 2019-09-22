package com.yh;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CountTest {

    @Test
    public void testAdd() {
        Count count = new Count();
        int result = count.add(2, 3);
        assertEquals(result, 5);
    }
}