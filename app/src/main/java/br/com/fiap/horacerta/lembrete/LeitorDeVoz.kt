package br.com.fiap.horacerta.lembrete

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Fala um texto uma única vez usando o TextToSpeech do próprio aparelho
 * (funciona sem internet quando o pacote de voz em português está instalado).
 *
 * Usado pelo [LembreteReceiver] quando o alarme toca. Como um BroadcastReceiver
 * tem pouco tempo de vida, há um limite de segurança: [aoTerminar] é chamado
 * quando a fala acaba ou, no máximo, após [LIMITE_MS].
 */
object LeitorDeVoz {

    private const val LIMITE_MS = 9_000L
    private val localeBr: Locale = Locale.forLanguageTag("pt-BR")

    fun falarUmaVez(context: Context, texto: String, aoTerminar: () -> Unit) {
        val principal = Handler(Looper.getMainLooper())
        val terminou = AtomicBoolean(false)
        var tts: TextToSpeech? = null

        fun encerrar() {
            if (terminou.compareAndSet(false, true)) {
                principal.post {
                    tts?.stop()
                    tts?.shutdown()
                    aoTerminar()
                }
            }
        }

        principal.post {
            tts = TextToSpeech(context.applicationContext) { status ->
                val motor = tts
                if (status != TextToSpeech.SUCCESS || motor == null) {
                    encerrar()
                    return@TextToSpeech
                }
                motor.setLanguage(localeBr)
                motor.setSpeechRate(0.9f) // um pouco mais devagar, pensando no público idoso
                motor.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit
                    override fun onDone(utteranceId: String?) = encerrar()
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) = encerrar()
                })
                motor.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "lembrete")
            }
            principal.postDelayed({ encerrar() }, LIMITE_MS)
        }
    }
}
