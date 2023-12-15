package com.x8bit.bitwarden.data.vault.datasource.disk.convertor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class ZonedDateTimeTypeConverterTest {

    @Test
    fun `fromTimestamp should return null when value is null`() {
        val value: Long? = null

        val result = ZonedDateTimeTypeConverter.fromTimestamp(value)

        assertNull(result)
    }

    @Test
    fun `fromTimestamp should return correct ZonedDateTime when value is not null`() {
        val expected = ZonedDateTime.parse("2023-12-15T20:38:06Z")
        val value = expected.toEpochSecond()

        val result = ZonedDateTimeTypeConverter.fromTimestamp(value)

        assertEquals(expected, result)
    }

    @Test
    fun `toTimestamp should return null when value is null`() {
        val value: ZonedDateTime? = null

        val result = ZonedDateTimeTypeConverter.toTimestamp(value)

        assertNull(result)
    }

    @Test
    fun `toTimestamp should return correct Long when value is not null`() {
        val value = ZonedDateTime.parse("2023-12-15T20:38:06Z")
        val expected = value.toEpochSecond()

        val result = ZonedDateTimeTypeConverter.toTimestamp(value)

        assertEquals(expected, result)
    }
}
