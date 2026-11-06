package br.com.fiap.horacerta.di

import br.com.fiap.horacerta.data.repository.MedicamentoRepository
import br.com.fiap.horacerta.data.repository.MedicamentoRepositoryImpl
import br.com.fiap.horacerta.lembrete.AgendadorLembretes
import br.com.fiap.horacerta.lembrete.AgendadorLembretesImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMedicamentoRepository(impl: MedicamentoRepositoryImpl): MedicamentoRepository

    @Binds
    @Singleton
    abstract fun bindAgendadorLembretes(impl: AgendadorLembretesImpl): AgendadorLembretes
}
