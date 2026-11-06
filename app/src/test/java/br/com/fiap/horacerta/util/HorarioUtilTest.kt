package br.com.fiap.horacerta.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class HorarioUtilTest {

    private fun millis(ano: Int, mes: Int, dia: Int, hora: Int, minuto: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(ano, mes, dia, hora, minuto)
        }.timeInMillis

    private val agora = millis(2026, Calendar.NOVEMBER, 5, 14, 30)

    @Test
    fun formatarHorarioCompletaComZero() {
        assertEquals("08:05", formatarHorario(8, 5))
        assertEquals("21:30", formatarHorario(21, 30))
    }

    @Test
    fun formatarDataHoraUsaPadraoBrasileiro() {
        assertEquals("05/11/2026 às 14:30", formatarDataHora(agora))
    }

    @Test
    fun doseDeHojeContaComoTomada() {
        assertTrue(foiTomadoHoje(millis(2026, Calendar.NOVEMBER, 5, 8, 0), agora))
    }

    @Test
    fun doseDeOntemNaoContaComoTomadaHoje() {
        assertFalse(foiTomadoHoje(millis(2026, Calendar.NOVEMBER, 4, 23, 59), agora))
        assertFalse(foiTomadoHoje(null, agora))
    }

    @Test
    fun proximoDisparoEhHojeQuandoHorarioAindaNaoPassou() {
        assertEquals(millis(2026, Calendar.NOVEMBER, 5, 15, 0), proximoDisparo(15, 0, agora))
    }

    @Test
    fun proximoDisparoEhAmanhaQuandoHorarioJaPassou() {
        assertEquals(millis(2026, Calendar.NOVEMBER, 6, 8, 0), proximoDisparo(8, 0, agora))
        assertEquals(millis(2026, Calendar.NOVEMBER, 6, 14, 30), proximoDisparo(14, 30, agora))
    }

    @Test
    fun proximoDisparoAtravessaAViradaDoAno() {
        val reveillon = millis(2026, Calendar.DECEMBER, 31, 9, 0)
        assertEquals(millis(2027, Calendar.JANUARY, 1, 8, 0), proximoDisparo(8, 0, reveillon))
    }

    @Test
    fun horarioJaPassouIdentificaDoseAtrasada() {
        assertTrue(horarioJaPassou(8, 0, agora))
        assertFalse(horarioJaPassou(20, 0, agora))
    }

    @Test
    fun textoDoLembreteMontaFraseParaVoz() {
        assertEquals(
            "Hora do remédio. Losartana, 50 mg. Tomar em jejum.",
            textoDoLembrete("Losartana", "50 mg", "tomar em jejum.")
        )
        assertEquals("Hora do remédio. Losartana.", textoDoLembrete("Losartana", "", ""))
    }
}
