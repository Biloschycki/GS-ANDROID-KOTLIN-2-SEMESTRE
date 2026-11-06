package br.com.fiap.horacerta.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.fiap.horacerta.data.local.AppDatabase
import br.com.fiap.horacerta.data.local.Medicamento
import br.com.fiap.horacerta.data.local.TipoHorario
import br.com.fiap.horacerta.data.repository.MedicamentoRepositoryImpl
import br.com.fiap.horacerta.data.repository.ResultadoDose
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Testa Room + regras do repositório com um banco em memória (roda no emulador). */
@RunWith(AndroidJUnit4::class)
class MedicamentoRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: MedicamentoRepositoryImpl

    @Before
    fun criarBanco() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = MedicamentoRepositoryImpl(database, database.medicamentoDao())
    }

    @After
    fun fecharBanco() = database.close()

    private fun losartana() =
        Medicamento(nome = "Losartana", dosagem = "50 mg", hora = 8, minuto = 0, estoque = 10)

    @Test
    fun salvarEListarOrdenadoPorHorario() = runTest {
        repository.salvar(Medicamento(nome = "Sinvastatina", dosagem = "20 mg", hora = 21, minuto = 0))
        repository.salvar(losartana())

        val lista = repository.observarTodos().first()
        assertEquals(listOf("Losartana", "Sinvastatina"), lista.map { it.nome })
    }

    @Test
    fun editarAtualizaORegistro() = runTest {
        val id = repository.salvar(losartana())
        val salvo = repository.buscarPorId(id)!!

        repository.salvar(salvo.copy(dosagem = "100 mg"))

        assertEquals("100 mg", repository.buscarPorId(id)!!.dosagem)
    }

    @Test
    fun registrarDoseGravaHistoricoEDescontaEstoque() = runTest {
        val id = repository.salvar(losartana())

        val resultado = repository.registrarDose(id)

        assertEquals(ResultadoDose.REGISTRADA, resultado)
        assertEquals(9, repository.buscarPorId(id)!!.estoque)
        assertEquals(1, repository.observarHistorico(id).first().size)
    }

    @Test
    fun naoPermiteDoseDobradaNoMesmoDia() = runTest {
        val id = repository.salvar(losartana())
        repository.registrarDose(id)

        val segunda = repository.registrarDose(id)

        assertEquals(ResultadoDose.JA_TOMADA, segunda)
        assertEquals(9, repository.buscarPorId(id)!!.estoque)
        assertEquals(1, repository.observarHistorico(id).first().size)
    }

    @Test
    fun excluirApagaMedicamentoEHistorico() = runTest {
        val id = repository.salvar(losartana())
        repository.registrarDose(id)

        repository.excluir(repository.buscarPorId(id)!!)

        assertNull(repository.buscarPorId(id))
        assertTrue(repository.observarHistorico(id).first().isEmpty())
    }

    @Test
    fun recorrenciaLiberaNovaDoseNoProximoIntervalo() = runTest {
        val primeiraDose = System.currentTimeMillis() - 30 * 60_000L // começou há 30 min
        val id = repository.salvar(
            Medicamento(
                nome = "Amoxicilina", dosagem = "500 mg", hora = 8, minuto = 0,
                tipoHorario = TipoHorario.RECORRENTE, intervaloHoras = 8, referencia = primeiraDose
            )
        )
        val agora = System.currentTimeMillis()

        assertEquals(ResultadoDose.REGISTRADA, repository.registrarDose(id, agora))
        // mesma janela: bloqueia
        assertEquals(ResultadoDose.JA_TOMADA, repository.registrarDose(id, agora + 60 * 60_000L))
        // 8 horas depois: nova dose liberada
        assertEquals(ResultadoDose.REGISTRADA, repository.registrarDose(id, agora + 8 * 60 * 60_000L))
    }
}
