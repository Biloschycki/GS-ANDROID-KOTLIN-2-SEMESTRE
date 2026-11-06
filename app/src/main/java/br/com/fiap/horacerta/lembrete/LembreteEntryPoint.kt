package br.com.fiap.horacerta.lembrete

import br.com.fiap.horacerta.data.repository.MedicamentoRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Ponte do Hilt para os BroadcastReceivers.
 *
 * Receivers são criados pelo sistema Android, não pelo Hilt. Em vez de
 * `@AndroidEntryPoint`, pegamos as dependências explicitamente com
 * `EntryPointAccessors.fromApplication(...)` — fica claro de onde vêm.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface LembreteEntryPoint {
    fun repository(): MedicamentoRepository
    fun agendador(): AgendadorLembretes
}
