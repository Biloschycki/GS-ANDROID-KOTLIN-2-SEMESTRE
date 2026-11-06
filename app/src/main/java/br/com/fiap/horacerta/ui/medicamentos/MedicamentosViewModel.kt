package br.com.fiap.horacerta.ui.medicamentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.horacerta.data.local.Medicamento
import br.com.fiap.horacerta.data.repository.MedicamentoRepository
import br.com.fiap.horacerta.lembrete.AgendadorLembretes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MedicamentosUiState {
    data object Carregando : MedicamentosUiState()
    data class Pronto(val medicamentos: List<Medicamento>) : MedicamentosUiState()
}

@HiltViewModel
class MedicamentosViewModel @Inject constructor(
    private val repository: MedicamentoRepository,
    private val agendador: AgendadorLembretes
) : ViewModel() {

    val uiState: StateFlow<MedicamentosUiState> = repository.observarTodos()
        .map<List<Medicamento>, MedicamentosUiState> { MedicamentosUiState.Pronto(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MedicamentosUiState.Carregando
        )

    private val _mensagens = Channel<String>(Channel.BUFFERED)
    val mensagens: Flow<String> = _mensagens.receiveAsFlow()

    fun excluir(medicamento: Medicamento) {
        viewModelScope.launch {
            repository.excluir(medicamento)
            agendador.cancelar(medicamento.id)
            _mensagens.send("${medicamento.nome} foi excluído.")
        }
    }

    fun alterarLembrete(medicamento: Medicamento, ativo: Boolean) {
        viewModelScope.launch {
            repository.alterarLembrete(medicamento, ativo)
            agendador.agendar(medicamento.copy(lembreteAtivo = ativo)) // desativado → cancela
            _mensagens.send(
                if (ativo) "Lembrete de ${medicamento.nome} ligado."
                else "Lembrete de ${medicamento.nome} desligado."
            )
        }
    }
}
