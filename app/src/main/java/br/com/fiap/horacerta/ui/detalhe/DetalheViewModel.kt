package br.com.fiap.horacerta.ui.detalhe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.horacerta.data.local.Medicamento
import br.com.fiap.horacerta.data.local.RegistroDose
import br.com.fiap.horacerta.data.repository.MedicamentoRepository
import br.com.fiap.horacerta.domain.RegistrarDoseUseCase
import br.com.fiap.horacerta.domain.horarioDaDoseAtual
import br.com.fiap.horacerta.lembrete.AgendadorLembretes
import br.com.fiap.horacerta.ui.comum.StatusDose
import br.com.fiap.horacerta.ui.comum.statusDaDose
import br.com.fiap.horacerta.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetalheUiState {
    data object Carregando : DetalheUiState()
    data object NaoEncontrado : DetalheUiState()
    data class Pronto(
        val medicamento: Medicamento,
        val status: StatusDose,
        val horarioDose: Long,
        val historico: List<RegistroDose>
    ) : DetalheUiState()
}

@HiltViewModel
class DetalheViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MedicamentoRepository,
    private val agendador: AgendadorLembretes,
    private val registrarDose: RegistrarDoseUseCase
) : ViewModel() {

    private val medicamentoId: Long = savedStateHandle.get<Long>(Screen.ARG_ID) ?: 0L

    val uiState: StateFlow<DetalheUiState> = combine(
        repository.observarPorId(medicamentoId),
        repository.observarHistorico(medicamentoId)
    ) { medicamento, historico ->
        if (medicamento == null) {
            DetalheUiState.NaoEncontrado
        } else {
            val agora = System.currentTimeMillis()
            DetalheUiState.Pronto(
                medicamento = medicamento,
                status = statusDaDose(medicamento, agora),
                horarioDose = medicamento.horarioDaDoseAtual(agora),
                historico = historico
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetalheUiState.Carregando
    )

    private val _mensagens = Channel<String>(Channel.BUFFERED)
    val mensagens: Flow<String> = _mensagens.receiveAsFlow()

    private val _excluido = Channel<String>(Channel.BUFFERED)
    /** Emite quando o remédio é excluído, para a tela voltar. */
    val excluido: Flow<String> = _excluido.receiveAsFlow()

    fun marcarComoTomado(medicamento: Medicamento) {
        viewModelScope.launch { _mensagens.send(registrarDose(medicamento)) }
    }

    fun excluir(medicamento: Medicamento) {
        viewModelScope.launch {
            repository.excluir(medicamento)
            agendador.cancelar(medicamento.id)
            _excluido.send("${medicamento.nome} foi excluído.")
        }
    }
}
