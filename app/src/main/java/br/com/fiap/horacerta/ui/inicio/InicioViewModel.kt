package br.com.fiap.horacerta.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.horacerta.data.local.Medicamento
import br.com.fiap.horacerta.data.repository.MedicamentoRepository
import br.com.fiap.horacerta.domain.RegistrarDoseUseCase
import br.com.fiap.horacerta.domain.ehRecorrente
import br.com.fiap.horacerta.domain.horarioDaDoseAtual
import br.com.fiap.horacerta.domain.horarioDaDoseSeguinte
import br.com.fiap.horacerta.ui.comum.StatusDose
import br.com.fiap.horacerta.ui.comum.statusDaDose
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Um cartão da tela inicial.
 * @param horarioDose horário da dose da vez (hoje no fixo; a janela atual na recorrência)
 * @param proximaDose na recorrência, quando a dose da vez já foi tomada: horário da seguinte
 */
data class DoseDoDia(
    val medicamento: Medicamento,
    val status: StatusDose,
    val horarioDose: Long,
    val proximaDose: Long? = null
)

data class InicioUiState(
    val carregando: Boolean = true,
    val agora: Long = System.currentTimeMillis(),
    val doses: List<DoseDoDia> = emptyList()
) {
    val total: Int get() = doses.size
    val tomadas: Int get() = doses.count { it.status == StatusDose.TOMADA }
    val atrasadas: Int get() = doses.count { it.status == StatusDose.ATRASADA }

    /** Primeira dose ainda não tomada (a lista é ordenada pelo horário da dose). */
    val proxima: DoseDoDia? get() = doses.firstOrNull { it.status != StatusDose.TOMADA }
}

@HiltViewModel
class InicioViewModel @Inject constructor(
    repository: MedicamentoRepository,
    private val registrarDose: RegistrarDoseUseCase
) : ViewModel() {

    /**
     * "Relógio" que emite a hora atual sempre na virada do minuto, para o status
     * ("Hora de tomar!", "Atrasado") mudar junto com o alarme, sem atraso.
     */
    private val relogio: Flow<Long> = flow {
        while (true) {
            val agora = System.currentTimeMillis()
            emit(agora)
            delay(60_000 - agora % 60_000 + 200)
        }
    }

    val uiState: StateFlow<InicioUiState> =
        combine(repository.observarTodos(), relogio) { medicamentos, agora ->
            InicioUiState(
                carregando = false,
                agora = agora,
                doses = medicamentos
                    .map { m ->
                        val status = statusDaDose(m, agora)
                        DoseDoDia(
                            medicamento = m,
                            status = status,
                            horarioDose = m.horarioDaDoseAtual(agora),
                            proximaDose = if (m.ehRecorrente && status == StatusDose.TOMADA) {
                                m.horarioDaDoseSeguinte(agora)
                            } else null
                        )
                    }
                    .sortedBy { it.horarioDose }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InicioUiState()
        )

    private val _mensagens = Channel<String>(Channel.BUFFERED)
    /** Mensagens de feedback (Snackbar). */
    val mensagens: Flow<String> = _mensagens.receiveAsFlow()

    fun marcarComoTomado(medicamento: Medicamento) {
        viewModelScope.launch { _mensagens.send(registrarDose(medicamento)) }
    }
}
