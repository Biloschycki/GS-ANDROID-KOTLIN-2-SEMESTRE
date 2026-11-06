package br.com.fiap.horacerta.util

/*
 * Regras de horário para remédios de RECORRÊNCIA ("a cada N horas").
 *
 * A recorrência é uma sequência infinita de doses:
 *     referencia, referencia + N h, referencia + 2N h, ...
 * onde `referencia` é o instante da primeira dose (gravado no cadastro).
 *
 * Funções puras, testadas em RecorrenciaTest.
 */

const val HORA_MS = 3_600_000L
private const val DIA_MINUTOS = 24 * 60

fun intervaloEmMs(intervaloHoras: Int): Long = intervaloHoras * HORA_MS

/**
 * Quanto tempo antes do horário a dose já pode ser confirmada:
 * 1 hora, ou 1/4 do intervalo se ele for curto (ex.: a cada 2 h → 30 min).
 */
fun antecedencia(intervaloHoras: Int): Long = minOf(HORA_MS, intervaloEmMs(intervaloHoras) / 4)

/**
 * Primeira dose de uma recorrência cadastrada agora.
 * Mantém a sequência alinhada ao horário escolhido: com início às 08:00 a cada 8 h,
 * quem cadastra às 20:00 recebe o primeiro lembrete à 00:00 (e não às 08:00 de amanhã).
 */
fun primeiraDoseRecorrente(hora: Int, minuto: Int, intervaloHoras: Int, agora: Long): Long {
    val passo = intervaloEmMs(intervaloHoras)
    val base = horarioDeHoje(hora, minuto, agora)
    if (base >= agora) return base
    val passos = Math.floorDiv(agora - base + passo - 1, passo)
    return base + passos * passo
}

/**
 * Horário da dose "da vez": a mais recente que já começou (contando a antecedência).
 * Nunca é anterior à primeira dose.
 */
fun doseAtualRecorrente(referencia: Long, intervaloHoras: Int, agora: Long): Long {
    val passo = intervaloEmMs(intervaloHoras)
    val k = Math.floorDiv(agora - referencia + antecedencia(intervaloHoras), passo)
    return referencia + k.coerceAtLeast(0) * passo
}

/** Próximo lembrete estritamente depois de [agora]. */
fun proximoDisparoRecorrente(referencia: Long, intervaloHoras: Int, agora: Long): Long {
    val passo = intervaloEmMs(intervaloHoras)
    val k = Math.floorDiv(agora - referencia, passo) + 1
    return referencia + k.coerceAtLeast(0) * passo
}

/** A dose da vez já foi confirmada? */
fun doseRecorrenteTomada(
    referencia: Long,
    intervaloHoras: Int,
    ultimaTomada: Long?,
    agora: Long
): Boolean {
    if (ultimaTomada == null) return false
    val inicioDaJanela = doseAtualRecorrente(referencia, intervaloHoras, agora) - antecedencia(intervaloHoras)
    return ultimaTomada >= inicioDaJanela
}

/**
 * Horários de um dia quando o intervalo divide 24 h (1, 2, 3, 4, 6, 8, 12, 24).
 * Ex.: 08:00 a cada 8 h → ["00:00", "08:00", "16:00"]. Outros intervalos → lista vazia,
 * porque os horários mudam de um dia para o outro.
 */
fun horariosDoDia(hora: Int, minuto: Int, intervaloHoras: Int): List<String> {
    if (intervaloHoras !in 1..24 || 24 % intervaloHoras != 0) return emptyList()
    val inicio = hora * 60 + minuto
    return (0 until 24 / intervaloHoras)
        .map { (inicio + it * intervaloHoras * 60) % DIA_MINUTOS }
        .sorted()
        .map { formatarHorario(it / 60, it % 60) }
}

/** "a cada 8 horas (00:00, 08:00 e 16:00)" ou "a cada 5 horas, a partir das 08:00". */
fun descreverRecorrencia(hora: Int, minuto: Int, intervaloHoras: Int): String {
    val cada = if (intervaloHoras == 1) "A cada 1 hora" else "A cada $intervaloHoras horas"
    val horarios = horariosDoDia(hora, minuto, intervaloHoras)
    return when {
        horarios.size in 2..6 -> "$cada (${juntarComE(horarios)})"
        else -> "$cada, a partir das ${formatarHorario(hora, minuto)}"
    }
}

private fun juntarComE(itens: List<String>): String =
    if (itens.size <= 1) itens.joinToString() else itens.dropLast(1).joinToString(", ") + " e " + itens.last()
