package br.com.fiap.horacerta.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/*
 * Funções puras de data/hora. Ficam fora do ViewModel e do Android para que
 * possam ser testadas com JUnit comum (ver HorarioUtilTest).
 * Usamos Calendar (e não java.time) porque o minSdk é 24.
 */

private val localeBr: Locale = Locale.forLanguageTag("pt-BR")

/** 8, 5 -> "08:05" */
fun formatarHorario(hora: Int, minuto: Int): String = "%02d:%02d".format(hora, minuto)

/** Millis -> "05/11/2026 às 14:30" */
fun formatarDataHora(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", localeBr).format(Date(millis))

/** Millis -> "14:30" */
fun formatarSoHora(millis: Long): String =
    SimpleDateFormat("HH:mm", localeBr).format(Date(millis))

/** Millis -> "Quinta-feira, 5 de novembro" */
fun formatarDataPorExtenso(millis: Long): String =
    SimpleDateFormat("EEEE, d 'de' MMMM", localeBr).format(Date(millis))
        .replaceFirstChar { it.titlecase(localeBr) }

/** true se os dois instantes caem no mesmo dia do calendário (fuso do aparelho). */
fun mesmoDia(a: Long, b: Long): Boolean {
    val ca = Calendar.getInstance().apply { timeInMillis = a }
    val cb = Calendar.getInstance().apply { timeInMillis = b }
    return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
        ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
}

/** A dose de hoje já foi confirmada? */
fun foiTomadoHoje(ultimaTomada: Long?, agora: Long): Boolean =
    ultimaTomada != null && mesmoDia(ultimaTomada, agora)

/** Instante (millis) de hoje no horário informado. */
fun horarioDeHoje(hora: Int, minuto: Int, agora: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = agora
        set(Calendar.HOUR_OF_DAY, hora)
        set(Calendar.MINUTE, minuto)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

/**
 * Próximo disparo do lembrete diário: hoje no horário, se ainda não passou;
 * caso contrário, amanhã no mesmo horário.
 */
fun proximoDisparo(hora: Int, minuto: Int, agora: Long): Long {
    val hoje = horarioDeHoje(hora, minuto, agora)
    if (hoje > agora) return hoje
    return Calendar.getInstance().apply {
        timeInMillis = hoje
        add(Calendar.DAY_OF_YEAR, 1)
    }.timeInMillis
}

/** O horário de hoje já passou (dose atrasada se ainda não foi tomada)? */
fun horarioJaPassou(hora: Int, minuto: Int, agora: Long): Boolean =
    horarioDeHoje(hora, minuto, agora) <= agora

/** Texto falado pelo lembrete de voz. Ex.: "Hora do remédio. Losartana, 50 mg. Tomar em jejum." */
fun textoDoLembrete(nome: String, dosagem: String, observacao: String): String = buildString {
    append("Hora do remédio. ")
    append(nome.trim())
    if (dosagem.isNotBlank()) append(", ").append(dosagem.trim())
    append(".")
    if (observacao.isNotBlank()) append(" ").append(observacao.trim().removeSuffix(".").replaceFirstChar { it.uppercase() }).append(".")
}
