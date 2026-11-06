package br.com.fiap.horacerta.lembrete

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.fiap.horacerta.domain.doseAtualJaTomada
import br.com.fiap.horacerta.util.textoDoLembrete
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Recebe três tipos de evento:
 * - [ACAO_LEMBRAR]: o alarme tocou → mostra a notificação, fala em voz alta e reagenda;
 * - [ACAO_TOMEI]: botão "TOMEI" da notificação → registra a dose no Room;
 * - [ACAO_ADIAR]: botão "Lembrar em 10 min" → agenda nova repetição.
 */
class LembreteReceiver : BroadcastReceiver() {

    companion object {
        const val ACAO_LEMBRAR = "br.com.fiap.horacerta.LEMBRAR"
        const val ACAO_TOMEI = "br.com.fiap.horacerta.TOMEI"
        const val ACAO_ADIAR = "br.com.fiap.horacerta.ADIAR"
        const val EXTRA_MEDICAMENTO_ID = "medicamento_id"
        const val EXTRA_REPETICAO = "repeticao"

        /** Se a dose não for confirmada, o lembrete toca de novo depois deste tempo. */
        const val MINUTOS_REPETICAO = 15
        const val MINUTOS_ADIAR = 10
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicamentoId = intent.getLongExtra(EXTRA_MEDICAMENTO_ID, -1L)
        if (medicamentoId <= 0L) return

        val app = context.applicationContext
        val dependencias = EntryPointAccessors.fromApplication(app, LembreteEntryPoint::class.java)
        val repository = dependencias.repository()
        val agendador = dependencias.agendador()

        // goAsync() dá alguns segundos extras para acessar o banco e falar o lembrete.
        val pendente = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            var falando = false
            try {
                when (intent.action) {
                    ACAO_LEMBRAR -> {
                        val repeticao = intent.getBooleanExtra(EXTRA_REPETICAO, false)
                        val medicamento = repository.buscarPorId(medicamentoId) ?: return@launch
                        if (!medicamento.lembreteAtivo) return@launch

                        // Garante o próximo alarme (amanhã ou daqui a N horas) antes de tudo.
                        if (!repeticao) agendador.agendar(medicamento)

                        if (medicamento.doseAtualJaTomada(System.currentTimeMillis())) {
                            return@launch
                        }

                        NotificacaoHelper.mostrarLembrete(app, medicamento, repeticao)
                        if (!repeticao) agendador.agendarRepeticao(medicamentoId, MINUTOS_REPETICAO)

                        falando = true
                        val texto = textoDoLembrete(
                            medicamento.nome, medicamento.dosagem, medicamento.observacao
                        )
                        LeitorDeVoz.falarUmaVez(app, texto) { pendente.finish() }
                    }

                    ACAO_TOMEI -> {
                        repository.registrarDose(medicamentoId)
                        agendador.doseConfirmada(medicamentoId)
                    }

                    ACAO_ADIAR -> {
                        agendador.agendarRepeticao(medicamentoId, MINUTOS_ADIAR)
                        NotificacaoHelper.cancelar(app, medicamentoId)
                    }
                }
            } finally {
                if (!falando) pendente.finish()
            }
        }
    }
}
