package io.github.kimbohy.mobilecontroller.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MidiMessagesTest {

    @Test
    fun channel_1_cc_20_value_64() {
        val expected = byteArrayOf(0xB0.toByte(), 20, 64)
        assertArrayEquals(expected, MidiMessages.controlChange(1, 20, 64))
    }

    @Test
    fun channel_16_uses_status_0xBF() {
        assertEquals(0xBF.toByte(), MidiMessages.controlChange(16, 20, 0)[0])
    }

    @Test
    fun message_is_exactly_three_bytes() {
        assertEquals(3, MidiMessages.controlChange(5, 11, 100).size)
    }

    @Test
    fun maximum_value_is_0x7F() {
        assertEquals(0x7F.toByte(), MidiMessages.controlChange(1, 29, 127)[2])
    }

    @Test
    fun data_bytes_never_have_the_high_bit_set() {
        for (cc in 0..127) {
            for (v in 0..127) {
                val m = MidiMessages.controlChange(1, cc, v)
                assertTrue((m[1].toInt() and 0x80) == 0)
                assertTrue((m[2].toInt() and 0x80) == 0)
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun channel_0_is_rejected() {
        MidiMessages.controlChange(0, 20, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun channel_17_is_rejected() {
        MidiMessages.controlChange(17, 20, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cc_128_is_rejected() {
        MidiMessages.controlChange(1, 128, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun value_128_is_rejected() {
        MidiMessages.controlChange(1, 20, 128)
    }
}