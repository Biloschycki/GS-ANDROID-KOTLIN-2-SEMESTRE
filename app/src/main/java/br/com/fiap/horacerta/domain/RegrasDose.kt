package br.com.fiap.horacerta.domain

import br.com.fiap.horacerta.data.local.Medicamento
import br.com.fiap.horacerta.data.local.TipoHorario
import br.com.fiap.horacerta.util.descreverRecorrencia
import br.com.fiap.horacerta.util.doseAtualRecorrente
import br.com.fiap.horacerta.util.doseRecorrenteTomada
import br.com.fiap.horacerta.util.foiTomadoHoje
import br.com.fiap.horacerta.util.formatarHorario
import br.com.fiap.horacerta.util.horarioDeHoje
import br.com.fiap.horacerta.util.intervaloEmMs
import br.com.fiap.horacerta.util.proximoDisparo
import br.com.fiap.horacerta.util.proximoDisparoRecorrente

/*
 * Regras de dose que valem para os dois tipos de horário (fixo e recorrência).
 * Telas, repositório e lembretes usam só estas funções, sem precisar saber o tipo.
 */

val Medicamento.ehRecorrente: Boolean
    get() = tipoHorario == TipoHorario.RECORRENTE && referencia != null

/** Horário (millis) da dose "da vez". */
fun Medicamento.horarioDaDoseAtual(agora: Long): Long =
    if (ehRecorrente) doseAtualRecorrente(referencia!!, intervaloHoras, agora)
    else horarioDeHoje(hora, minuto, agora)

/** A dose da vez já foi confirmada? (fixo: hoje; recorrência: nesta janela de horário) */
fun Medicamento.doseAtualJaTomada(agora: Long): Boolean =
    if (ehRecorrente) doseRecorrenteTomada(referencia!!, intervaloHoras, ultimaTomada, agora)
    else foiTomadoHoje(ultimaTomada, agora)

/** Quando o alarme deve tocar da próxima vez. */
fun Medicamento.proximoLembrete(agora: Long): Long =
    if (ehRecorrente) proximoDisparoRecorrente(referencia!!, intervaloHoras, agora)
    else proximoDisparo(hora, minuto, agora)

/** Próxima dose depois da atual (só faz sentido na recorrência). */
fun Medicamento.horarioDaDoseSeguinte(agora: Long): Long =
    horarioDaDoseAtual(agora) + intervaloEmMs(intervaloHoras)

/** Texto para as telas: "Todo dia às 08:00" ou "A cada 8 horas (00:00, 08:00 e 16:00)". */
fun Medicamento.descricaoHorario(): String =
    if (tipoHorario == TipoHorario.RECORRENTE) descreverRecorrencia(hora, minuto, intervaloHoras)
    else "Todo dia às ${formatarHorario(hora, minuto)}"
