package br.com.fiap.horacerta

import android.app.Application
import br.com.fiap.horacerta.lembrete.NotificacaoHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HoraCertaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificacaoHelper.criarCanal(this)
    }
}
