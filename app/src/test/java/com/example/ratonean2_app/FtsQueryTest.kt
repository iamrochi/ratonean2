package com.example.ratonean2_app

import com.example.ratonean2_app.product.data.dao.toFtsQuery
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * El sanitizador FTS evita que input del usuario rompa el MATCH
 * (comillas, *, OR/NEAR) o cambie su semántica.
 */
class FtsQueryTest {

    @Test
    fun `una palabra genera prefijo`() {
        assertEquals("\"leche\"*", "leche".toFtsQuery())
    }

    @Test
    fun `varias palabras hacen AND con prefijo final`() {
        assertEquals("\"leche\" \"ente\"*", "leche ente".toFtsQuery())
    }

    @Test
    fun `comillas se escapan duplicandolas`() {
        // FTS escapa " como "" dentro de frase: """x""" es correcto.
        assertEquals("\"lala\" \"\"\"x\"\"\"*", "lala \"x\"".toFtsQuery())
    }

    @Test
    fun `vacio no rompe el match`() {
        assertEquals("\"\"", "   ".toFtsQuery())
    }
}
