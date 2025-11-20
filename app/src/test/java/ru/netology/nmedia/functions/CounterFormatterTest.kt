package ru.netology.nmedia.functions

import org.junit.Assert.*
import org.junit.Test

class CounterFormatterTest {
    @Test
    fun counterFormatter999() {
        assertEquals("999", counterFormatter(999))
    }

    @Test
    fun counterFormatter0() {
        assertEquals("0", counterFormatter(0))
    }

    @Test
    fun counterFormatter1k() {
        assertEquals("1K", counterFormatter(1_000))
    }

    @Test
    fun counterFormatter1k2() {
        assertEquals("1K", counterFormatter(1_099))
    }

    @Test
    fun counterFormatter10k() {
        assertEquals("10K", counterFormatter(10_000))
    }

    @Test
    fun counterFormatter11K() {
        assertEquals("11K", counterFormatter(11_000))
    }

    @Test
    fun counterFormatter999k() {
        assertEquals("999K", counterFormatter(999_000))
    }

    @Test
    fun counterFormatter1M() {
        assertEquals("1M", counterFormatter(1_000_000))
    }

    @Test
    fun counterFormatter1M2() {
        assertEquals("1M", counterFormatter(1_000_999))
    }

    @Test
    fun counterFormatter1dot1M() {
        assertEquals("1.1M", counterFormatter(1_100_000))
    }

    @Test
    fun counterFormatter10dot0M() {
        assertEquals("10.0M", counterFormatter(10_000_000))
    }

}