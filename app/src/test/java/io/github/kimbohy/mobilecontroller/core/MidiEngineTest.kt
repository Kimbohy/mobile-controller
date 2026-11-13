package io.github.kimbohy.mobilecontroller.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MidiEngineTest {

    private val transport = FakeTransport()
    private val engine = MidiEngine(transport)

    @Test
    fun first_value_is_sent_as_a_three_byte_control_change() {
        assertTrue(engine.sendControlChange(cc = 20, value = 64))
        assertEquals(listOf(listOf(0xB0, 20, 64)), transport.sent)
    }

    @Test
    fun same_value_twice_sends_only_once() {
        engine.sendControlChange(20, 64)
        assertFalse(engine.sendControlChange(20, 64))
        assertEquals(1, transport.sent.size)
    }

    @Test
    fun changed_value_is_sent_again() {
        engine.sendControlChange(20, 64)
        engine.sendControlChange(20, 65)
        assertEquals(2, transport.sent.size)
        assertEquals(listOf(0xB0, 20, 65), transport.sent.last())
    }

    @Test
    fun each_cc_number_is_filtered_independently() {
        engine.sendControlChange(20, 64)
        engine.sendControlChange(21, 64)
        assertEquals(2, transport.sent.size)
    }

    @Test
    fun out_of_range_values_are_clamped_not_crashed() {
        engine.sendControlChange(20, 500)
        engine.sendControlChange(21, -9)
        assertEquals(listOf(0xB0, 20, 127), transport.sent[0])
        assertEquals(listOf(0xB0, 21, 0), transport.sent[1])
    }

    @Test
    fun channel_is_encoded_in_the_status_byte() {
        engine.setChannel(5)
        engine.sendControlChange(20, 10)
        assertEquals(listOf(0xB4, 20, 10), transport.sent.single())
    }

    @Test
    fun channel_is_clamped_to_1_16() {
        engine.setChannel(99)
        assertEquals(16, engine.channel)
        engine.setChannel(-4)
        assertEquals(1, engine.channel)
    }

    @Test
    fun changing_channel_resets_the_duplicate_filter() {
        engine.sendControlChange(20, 64)
        engine.setChannel(2)
        assertTrue(engine.sendControlChange(20, 64))
        assertEquals(listOf(0xB1, 20, 64), transport.sent.last())
    }

    @Test
    fun a_message_lost_while_disconnected_is_resent_after_reconnection() {
        transport.accepting = false
        assertFalse(engine.sendControlChange(20, 64))
        assertTrue(transport.sent.isEmpty())

        transport.accepting = true
        assertTrue(engine.sendControlChange(20, 64))
        assertEquals(1, transport.sent.size)
    }

    @Test
    fun a_full_slide_never_produces_a_burst_of_duplicates() {
        // 1000 petits mouvements de doigt de 0.0 à 1.0 : au plus 128 messages (0..127),
        // tous différents du précédent et strictement croissants.
        for (i in 0..1000) {
            engine.sendControlChange(20, ValueQuantizer.toMidi(i / 1000f))
        }
        val values = transport.sent.map { it[2] }
        assertTrue(values.size <= 128)
        assertEquals(values.sorted(), values)
        assertEquals(values.distinct(), values)
        assertEquals(127, values.last())
    }

    @Test
    fun forced_message_is_sent_even_if_the_value_did_not_change() {
        engine.sendControlChange(20, 64)
        assertTrue(engine.forceControlChange(20, 64))
        assertEquals(2, transport.sent.size)
    }

    @Test
    fun resendAll_sends_every_entry_and_counts_them() {
        engine.sendControlChange(20, 10) // déjà « connu » du filtre
        val count = engine.resendAll(listOf(20 to 10, 21 to 20, 22 to 30))
        assertEquals(3, count)
        assertEquals(
            listOf(listOf(0xB0, 20, 10), listOf(0xB0, 20, 10), listOf(0xB0, 21, 20), listOf(0xB0, 22, 30)),
            transport.sent,
        )
    }

    @Test
    fun resendAll_returns_zero_when_disconnected() {
        transport.accepting = false
        assertEquals(0, engine.resendAll(listOf(20 to 10, 21 to 20)))
    }

    @Test
    fun after_resendAll_the_duplicate_filter_knows_the_values() {
        engine.resendAll(listOf(20 to 10))
        assertFalse(engine.sendControlChange(20, 10))
    }

    @Test
    fun resetFilter_allows_the_same_value_to_be_sent_again() {
        engine.sendControlChange(20, 64)
        engine.resetFilter()
        assertTrue(engine.sendControlChange(20, 64))
        assertEquals(2, transport.sent.size)
    }
}