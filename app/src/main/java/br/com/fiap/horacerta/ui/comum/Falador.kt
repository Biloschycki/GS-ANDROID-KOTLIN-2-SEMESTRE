package br.com.fiap.horacerta.ui.comum

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Leitura em voz alta dentro das telas (botão "Ouvir").
 * Pensado para quem enxerga mal: o app fala o nome, a dose e a observação.
 */
class Falador(context: Context) {

    private var pronto = false
    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(context.applicationContext) { status -> aoIniciar(status) }
    }

    private fun aoIniciar(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.forLanguageTag("pt-BR"))
            tts.setSpeechRate(0.9f)
            pronto = true
        }
    }

    fun falar(texto: String) {
        if (pronto) tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "tela")
    }

    fun desligar() {
        tts.stop()
        tts.shutdown()
    }
}

/** Cria um [Falador] ligado ao ciclo de vida da tela (desliga ao sair). */
@Composable
fun rememberFalador(): Falador {
    val context = LocalContext.current
    val falador = remember { Falador(context) }
    DisposableEffect(falador) {
        onDispose { falador.desligar() }
    }
    return falador
}
