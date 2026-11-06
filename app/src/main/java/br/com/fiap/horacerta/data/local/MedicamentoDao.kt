package br.com.fiap.horacerta.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {

    @Query("SELECT * FROM medicamentos ORDER BY hora, minuto, nome COLLATE NOCASE")
    fun observarTodos(): Flow<List<Medicamento>>

    @Query("SELECT * FROM medicamentos ORDER BY hora, minuto, nome COLLATE NOCASE")
    suspend fun listarTodos(): List<Medicamento>

    @Query("SELECT * FROM medicamentos WHERE id = :id")
    fun observarPorId(id: Long): Flow<Medicamento?>

    @Query("SELECT * FROM medicamentos WHERE id = :id")
    suspend fun buscarPorId(id: Long): Medicamento?

    @Insert
    suspend fun inserir(medicamento: Medicamento): Long

    @Update
    suspend fun atualizar(medicamento: Medicamento)

    @Delete
    suspend fun excluir(medicamento: Medicamento)

    @Insert
    suspend fun inserirRegistro(registro: RegistroDose)

    @Query("SELECT * FROM registros_dose WHERE medicamentoId = :medicamentoId ORDER BY tomadoEm DESC")
    fun observarHistorico(medicamentoId: Long): Flow<List<RegistroDose>>
}
