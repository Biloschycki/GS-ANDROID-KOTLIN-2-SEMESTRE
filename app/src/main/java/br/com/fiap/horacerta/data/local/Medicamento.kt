package br.com.fiap.horacerta.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Como o lembrete se repete. */
enum class TipoHorario {
    /** Uma vez por dia, no horário escolhido. */
    FIXO,

    /** A cada N horas ([Medicamento.intervaloHoras]), a partir da primeira dose. */
    RECORRENTE
}

/**
 * Remédio cadastrado pelo idoso ou pelo cuidador.
 *
 * - FIXO: lembra todo dia às [hora]:[minuto]. A dose conta como tomada quando
 *   [ultimaTomada] cai no dia de hoje.
 * - RECORRENTE: lembra a cada [intervaloHoras], começando em [referencia]
 *   (instante da primeira dose, alinhado a [hora]:[minuto]).
 *
 * O Room grava o enum [TipoHorario] pelo nome ("FIXO"/"RECORRENTE").
 */
@Entity(tableName = "medicamentos")
data class Medicamento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val dosagem: String,
    val hora: Int,
    val minuto: Int,
    val tipoHorario: TipoHorario = TipoHorario.FIXO,
    val intervaloHoras: Int = 24,
    val referencia: Long? = null,
    val observacao: String = "",
    val estoque: Int = 0,
    val lembreteAtivo: Boolean = true,
    val ultimaTomada: Long? = null,
    val dataCriacao: Long = System.currentTimeMillis()
)
