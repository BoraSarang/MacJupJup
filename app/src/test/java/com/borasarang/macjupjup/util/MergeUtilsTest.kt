package com.borasarang.macjupjup.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MergeUtilsTest {

    @Test
    fun `동일앱_대소문자공백차이_동일키`() {
        assertEquals(
            MergeUtils.generateId("Raycast", "Raycast Technologies"),
            MergeUtils.generateId("  raycast ", "raycast-technologies!"),
        )
    }

    @Test
    fun `다른앱_다른키`() {
        assertNotEquals(
            MergeUtils.generateId("Raycast", "Raycast Technologies"),
            MergeUtils.generateId("Alfred", "Running with Crayons"),
        )
    }

    @Test
    fun `긴이름_전체128자절단`() {
        val id = MergeUtils.generateId("A".repeat(100), "B".repeat(100))
        assertEquals(128, id.length)
    }
}
