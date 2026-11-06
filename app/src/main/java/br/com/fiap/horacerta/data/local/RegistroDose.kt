package br.com.fiap.horacerta.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Histórico: cada vez que o usuário confirma "Tomei", um registro é gravado.
 * Ao excluir o medicamento, o histórico dele é apagado junto (CASCADE).
 */
@Entity(
    tableName = "registros_dose",
    foreignKeys = [
        ForeignKey(
            entity = Medicamento::class,
            parentColumns = ["id"],
            childColumns = ["medicamentoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicamentoId")]
)
data class RegistroDose(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicamentoId: Long,
    val tomadoEm: Long
)
