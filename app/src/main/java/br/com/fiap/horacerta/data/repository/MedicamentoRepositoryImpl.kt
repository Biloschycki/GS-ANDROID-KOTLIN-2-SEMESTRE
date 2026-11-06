package br.com.fiap.horacerta.data.repository

import androidx.room.withTransaction
import br.com.fiap.horacerta.data.local.AppDatabase
import br.com.fiap.horacerta.data.local.Medicamento
import br.com.fiap.horacerta.data.local.MedicamentoDao
import br.com.fiap.horacerta.data.local.RegistroDose
import br.com.fiap.horacerta.domain.doseAtualJaTomada
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MedicamentoRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val dao: MedicamentoDao
) : MedicamentoRepository {

    override fun observarTodos(): Flow<List<Medicamento>> = dao.observarTodos()

    override fun observarPorId(id: Long): Flow<Medicamento?> = dao.observarPorId(id)

    override fun observarHistorico(medicamentoId: Long): Flow<List<RegistroDose>> =
        dao.observarHistorico(medicamentoId)

    override suspend fun listarTodos(): List<Medicamento> = dao.listarTodos()

    override suspend fun buscarPorId(id: Long): Medicamento? = dao.buscarPorId(id)

    override suspend fun salvar(medicamento: Medicamento): Long =
        if (medicamento.id == 0L) {
            dao.inserir(medicamento)
        } else {
            dao.atualizar(medicamento)
            medicamento.id
        }

    override suspend fun excluir(medicamento: Medicamento) = dao.excluir(medicamento)

    override suspend fun alterarLembrete(medicamento: Medicamento, ativo: Boolean) =
        dao.atualizar(medicamento.copy(lembreteAtivo = ativo))

    override suspend fun registrarDose(medicamentoId: Long, agora: Long): ResultadoDose =
        database.withTransaction {
            val medicamento = dao.buscarPorId(medicamentoId)
                ?: return@withTransaction ResultadoDose.NAO_ENCONTRADO

            if (medicamento.doseAtualJaTomada(agora)) {
                return@withTransaction ResultadoDose.JA_TOMADA
            }

            dao.inserirRegistro(RegistroDose(medicamentoId = medicamentoId, tomadoEm = agora))
            dao.atualizar(
                medicamento.copy(
                    ultimaTomada = agora,
                    estoque = (medicamento.estoque - 1).coerceAtLeast(0)
                )
            )
            ResultadoDose.REGISTRADA
        }
}
