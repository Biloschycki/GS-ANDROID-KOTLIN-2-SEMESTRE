package br.com.fiap.horacerta.lembrete

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * O Android apaga todos os alarmes quando o celular reinicia.
 * Este receiver recria o lembrete de cada medicamento ativo.
 */
class BootReceiver : BroadcastReceiver() {

    private val acoesAceitas = setOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_MY_PACKAGE_REPLACED,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in acoesAceitas) return

        val dependencias = EntryPointAccessors.fromApplication(
            context.applicationContext, LembreteEntryPoint::class.java
        )
        val pendente = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                dependencias.repository().listarTodos().forEach { dependencias.agendador().agendar(it) }
            } finally {
                pendente.finish()
            }
        }
    }
}
