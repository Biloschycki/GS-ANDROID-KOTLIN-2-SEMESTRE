package br.com.fiap.horacerta.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class RecorrenciaTest {

    private fun millis(dia: Int, hora: Int, minuto: Int = 0): Long =
        Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.NOVEMBER, dia, hora, minuto)
        }.timeInMillis

    /** Primeira dose às 08:00 do dia 6, a cada 8 horas. */
    private val referencia = millis(6, 8)

    @Test
    fun primeiraDoseEhOHorarioEscolhidoQuandoAindaNaoPassou() {
        assertEquals(millis(6, 8), primeiraDoseRecorrente(8, 0, 8, agora = millis(6, 7)))
    }

    @Test
    fun primeiraDoseSegueASequenciaQuandoCadastraDepois() {
        // 08:00 a cada 8 h, cadastrado às 20:00 → primeira dose à 00:00
        assertEquals(millis(7, 0), primeiraDoseRecorrente(8, 0, 8, agora = millis(6, 20)))
    }

    @Test
    fun doseAtualAvancaACadaIntervalo() {
        assertEquals(millis(6, 8), doseAtualRecorrente(referencia, 8, agora = millis(6, 12)))
        assertEquals(millis(7, 0), doseAtualRecorrente(referencia, 8, agora = millis(7, 1)))
    }

    @Test
    fun doseAtualConsideraUmaHoraDeAntecedencia() {
        assertEquals(millis(6, 16), doseAtualRecorrente(referencia, 8, agora = millis(6, 15, 10)))
    }

    @Test
    fun doseAtualNuncaEhAnteriorAPrimeiraDose() {
        assertEquals(referencia, doseAtualRecorrente(referencia, 8, agora = millis(6, 6)))
    }

    @Test
    fun proximoLembreteEhODoProximoIntervalo() {
        assertEquals(millis(6, 8), proximoDisparoRecorrente(referencia, 8, agora = millis(6, 6)))
        assertEquals(millis(6, 16), proximoDisparoRecorrente(referencia, 8, agora = millis(6, 8)))
        assertEquals(millis(7, 0), proximoDisparoRecorrente(referencia, 8, agora = millis(6, 17)))
    }

    @Test
    fun doseTomadaValeSoParaAJanelaAtual() {
        val tomada = millis(6, 8, 5)
        assertTrue(doseRecorrenteTomada(referencia, 8, tomada, agora = millis(6, 10)))
        assertFalse(doseRecorrenteTomada(referencia, 8, tomada, agora = millis(6, 16, 30)))
        assertFalse(doseRecorrenteTomada(referencia, 8, null, agora = millis(6, 10)))
    }

    @Test
    fun horariosDoDiaParaIntervalosQueDividem24Horas() {
        assertEquals(listOf("00:00", "08:00", "16:00"), horariosDoDia(8, 0, 8))
        assertEquals(listOf("08:00", "20:00"), horariosDoDia(8, 0, 12))
        assertTrue(horariosDoDia(8, 0, 5).isEmpty())
    }

    @Test
    fun descricaoDaRecorrencia() {
        assertEquals("A cada 8 horas (00:00, 08:00 e 16:00)", descreverRecorrencia(8, 0, 8))
        assertEquals("A cada 5 horas, a partir das 08:00", descreverRecorrencia(8, 0, 5))
    }
}
