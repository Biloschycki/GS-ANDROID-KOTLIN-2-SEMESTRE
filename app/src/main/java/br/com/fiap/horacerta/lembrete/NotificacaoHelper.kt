package br.com.fiap.horacerta.lembrete

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import br.com.fiap.horacerta.MainActivity
import br.com.fiap.horacerta.R
import br.com.fiap.horacerta.data.local.Medicamento
import br.com.fiap.horacerta.domain.horarioDaDoseAtual
import br.com.fiap.horacerta.util.formatarSoHora

object NotificacaoHelper {

    const val CANAL_ID = "lembretes_remedio"

    fun criarCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                "Lembretes de remédio",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisa na hora de tomar cada remédio"
                enableVibration(true)
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(canal)
        }
    }

    fun podeNotificar(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // checado em podeNotificar()
    fun mostrarLembrete(context: Context, medicamento: Medicamento, repeticao: Boolean) {
        if (!podeNotificar(context)) return

        val titulo = if (repeticao) {
            "Ainda não confirmado: ${medicamento.nome}"
        } else {
            "Hora do remédio: ${medicamento.nome}"
        }
        val texto = buildString {
            append(medicamento.dosagem)
            append(" • ")
            append(formatarSoHora(medicamento.horarioDaDoseAtual(System.currentTimeMillis())))
            if (medicamento.observacao.isNotBlank()) append("\n").append(medicamento.observacao)
        }

        val abrirApp = PendingIntent.getActivity(
            context,
            medicamento.id.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacao = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .addAction(0, "TOMEI", acao(context, LembreteReceiver.ACAO_TOMEI, medicamento.id))
            .addAction(0, "Lembrar em 10 min", acao(context, LembreteReceiver.ACAO_ADIAR, medicamento.id))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(medicamento.id.toInt(), notificacao)
        } catch (_: SecurityException) {
            // Permissão revogada entre a checagem e o envio: não há o que fazer.
        }
    }

    fun cancelar(context: Context, medicamentoId: Long) {
        NotificationManagerCompat.from(context).cancel(medicamentoId.toInt())
    }

    private fun acao(context: Context, acao: String, medicamentoId: Long): PendingIntent {
        val intent = Intent(context, LembreteReceiver::class.java).apply {
            action = acao
            putExtra(LembreteReceiver.EXTRA_MEDICAMENTO_ID, medicamentoId)
        }
        // requestCode único por (ação, remédio) para os PendingIntents não se sobrescreverem
        val deslocamento = if (acao == LembreteReceiver.ACAO_TOMEI) 1 else 2
        val requestCode = (medicamentoId * 10 + deslocamento).toInt()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
