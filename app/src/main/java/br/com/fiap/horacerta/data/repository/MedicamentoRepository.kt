package br.com.fiap.horacerta.data.repository

import br.com.fiap.horacerta.data.local.Medicamento
import br.com.fiap.horacerta.data.local.RegistroDose
import kotlinx.coroutines.flow.Flow

/** Resultado de tentar registrar uma dose. */
enum class ResultadoDose { REGISTRADA, JA_TOMADA, NAO_ENCONTRADO }

interface MedicamentoRepository {
    fun observarTodos(): Flow<List<Medicamento>>
    fun observarPorId(id: Long): Flow<Medicamento?>
    fun observarHistorico(medicamentoId: Long): Flow<List<RegistroDose>>
    suspend fun listarTodos(): List<Medicamento>
    suspend fun buscarPorId(id: Long): Medicamento?

    /** Insere quando o id é 0, senão atualiza. Devolve o id salvo. */
    suspend fun salvar(medicamento: Medicamento): Long
    suspend fun excluir(medicamento: Medicamento)
    suspend fun alterarLembrete(medicamento: Medicamento, ativo: Boolean)

    /**
     * Registra que a dose da vez foi tomada: grava no histórico, guarda o horário
     * e desconta um comprimido do estoque. Bloqueia dose dobrada (mesmo dia no horário
     * fixo; mesma janela de horário na recorrência).
     */
    suspend fun registrarDose(medicamentoId: Long, agora: Long = System.currentTimeMillis()): ResultadoDose
}
