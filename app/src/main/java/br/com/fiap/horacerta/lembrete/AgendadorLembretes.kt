package br.com.fiap.horacerta.lembrete

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import br.com.fiap.horacerta.data.local.Medicamento
import br.com.fiap.horacerta.domain.proximoLembrete
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Agenda o próximo lembrete de cada medicamento (diário ou a cada N horas).
 * Quando o alarme toca, o LembreteReceiver agenda o seguinte.
 *
 * Por que AlarmManager e não WorkManager? O WorkManager pode atrasar a execução
 * para economizar bateria e tem intervalo mínimo de 15 minutos. Remédio precisa
 * tocar na hora certa, então usamos alarme exato.
 *
 * Cada medicamento pode ter dois alarmes ao mesmo tempo:
 * - o principal, no próximo horário do remédio;
 * - a repetição, que toca de novo alguns minutos depois se a dose não foi confirmada
 *   (ou quando o usuário toca em "Lembrar em 10 min").
 */
interface AgendadorLembretes {
    fun agendar(medicamento: Medicamento)
    fun agendarRepeticao(medicamentoId: Long, minutos: Int)
    fun cancelarRepeticao(medicamentoId: Long)

    /** Dose confirmada: cancela a repetição pendente e tira a notificação da tela. */
    fun doseConfirmada(medicamentoId: Long)

    /** Remove todos os alarmes e a notificação do medicamento (ex.: ao excluir). */
    fun cancelar(medicamentoId: Long)
}

class AgendadorLembretesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AgendadorLembretes {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun agendar(medicamento: Medicamento) {
        if (!medicamento.lembreteAtivo) {
            cancelar(medicamento.id)
            return
        }
        val quando = medicamento.proximoLembrete(System.currentTimeMillis())
        definirAlarme(quando, pendingIntent(medicamento.id, repeticao = false))
    }

    override fun agendarRepeticao(medicamentoId: Long, minutos: Int) {
        val quando = System.currentTimeMillis() + minutos * 60_000L
        definirAlarme(quando, pendingIntent(medicamentoId, repeticao = true))
    }

    override fun cancelarRepeticao(medicamentoId: Long) {
        alarmManager.cancel(pendingIntent(medicamentoId, repeticao = true))
    }

    override fun doseConfirmada(medicamentoId: Long) {
        cancelarRepeticao(medicamentoId)
        NotificacaoHelper.cancelar(context, medicamentoId)
    }

    override fun cancelar(medicamentoId: Long) {
        alarmManager.cancel(pendingIntent(medicamentoId, repeticao = false))
        cancelarRepeticao(medicamentoId)
        NotificacaoHelper.cancelar(context, medicamentoId)
    }

    private fun definirAlarme(quando: Long, pendingIntent: PendingIntent) {
        val podeExato = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (podeExato) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, quando, pendingIntent)
        } else {
            // Sem permissão de alarme exato: ainda lembra, com alguns minutos de tolerância.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, quando, pendingIntent)
        }
    }

    private fun pendingIntent(medicamentoId: Long, repeticao: Boolean): PendingIntent {
        val intent = Intent(context, LembreteReceiver::class.java).apply {
            action = LembreteReceiver.ACAO_LEMBRAR
            putExtra(LembreteReceiver.EXTRA_MEDICAMENTO_ID, medicamentoId)
            putExtra(LembreteReceiver.EXTRA_REPETICAO, repeticao)
        }
        // Códigos diferentes para o alarme diário e para a repetição: um não substitui o outro.
        val requestCode = (medicamentoId * 2 + if (repeticao) 1 else 0).toInt()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
