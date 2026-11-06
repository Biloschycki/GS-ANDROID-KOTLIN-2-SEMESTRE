package br.com.fiap.horacerta.ui.formulario

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.horacerta.data.local.Medicamento
import br.com.fiap.horacerta.data.local.TipoHorario
import br.com.fiap.horacerta.domain.descricaoHorario
import br.com.fiap.horacerta.data.repository.MedicamentoRepository
import br.com.fiap.horacerta.lembrete.AgendadorLembretes
import br.com.fiap.horacerta.ui.navigation.Screen
import br.com.fiap.horacerta.util.descreverRecorrencia
import br.com.fiap.horacerta.util.formatarHorario
import br.com.fiap.horacerta.util.primeiraDoseRecorrente
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FormularioUiState(
    val id: Long = 0L,
    val nome: String = "",
    val dosagem: String = "",
    val hora: Int? = null,
    val minuto: Int? = null,
    val tipoHorario: TipoHorario = TipoHorario.FIXO,
    val recorrencia: String = "",
    val estoque: String = "",
    val observacao: String = "",
    val lembreteAtivo: Boolean = true,
    val erros: ErrosFormulario = ErrosFormulario(),
    val carregando: Boolean = false,
    val salvando: Boolean = false
) {
    val editando: Boolean get() = id != 0L
    val horarioTexto: String? get() = if (hora != null && minuto != null) formatarHorario(hora, minuto) else null
    val recorrente: Boolean get() = tipoHorario == TipoHorario.RECORRENTE

    /** Prévia mostrada no formulário: "A cada 8 horas (00:00, 08:00 e 16:00)". */
    val previaRecorrencia: String?
        get() {
            val horas = recorrencia.toIntOrNull()
            return if (recorrente && hora != null && minuto != null && horas != null && horas in 1..24) {
                descreverRecorrencia(hora, minuto, horas)
            } else null
        }
}

@HiltViewModel
class FormularioViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MedicamentoRepository,
    private val agendador: AgendadorLembretes
) : ViewModel() {

    private val idRecebido: Long = savedStateHandle.get<Long>(Screen.ARG_ID) ?: 0L

    private val _uiState = MutableStateFlow(FormularioUiState(carregando = idRecebido != 0L))
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    private val _salvo = Channel<String>(Channel.BUFFERED)
    /** Emite a mensagem de sucesso quando o registro é salvo (a tela então volta). */
    val salvo: Flow<String> = _salvo.receiveAsFlow()

    /** Mantém campos que o formulário não edita (última dose, data de criação). */
    private var original: Medicamento? = null

    /** Depois da primeira tentativa de salvar, os erros passam a atualizar a cada digitação. */
    private var tentouSalvar = false

    init {
        if (idRecebido != 0L) carregar(idRecebido)
    }

    private fun carregar(id: Long) {
        viewModelScope.launch {
            val m = repository.buscarPorId(id)
            original = m
            _uiState.value = if (m == null) {
                FormularioUiState()
            } else {
                FormularioUiState(
                    id = m.id,
                    nome = m.nome,
                    dosagem = m.dosagem,
                    hora = m.hora,
                    minuto = m.minuto,
                    tipoHorario = m.tipoHorario,
                    recorrencia = if (m.tipoHorario == TipoHorario.RECORRENTE) m.intervaloHoras.toString() else "",
                    estoque = if (m.estoque > 0) m.estoque.toString() else "",
                    observacao = m.observacao,
                    lembreteAtivo = m.lembreteAtivo
                )
            }
        }
    }

    fun onNomeChange(valor: String) = atualizar { it.copy(nome = valor) }
    fun onDosagemChange(valor: String) = atualizar { it.copy(dosagem = valor) }
    fun onHorarioChange(hora: Int, minuto: Int) = atualizar { it.copy(hora = hora, minuto = minuto) }
    fun onTipoHorarioChange(tipo: TipoHorario) = atualizar { it.copy(tipoHorario = tipo) }
    fun onRecorrenciaChange(valor: String) {
        if (valor.length <= 2 && valor.all { it.isDigit() }) atualizar { it.copy(recorrencia = valor) }
    }
    fun onObservacaoChange(valor: String) = atualizar { it.copy(observacao = valor) }
    fun onLembreteChange(valor: Boolean) = atualizar { it.copy(lembreteAtivo = valor) }
    fun onEstoqueChange(valor: String) {
        // aceita só dígitos (no máximo 3) para não poluir o campo
        if (valor.length <= 3 && valor.all { it.isDigit() }) atualizar { it.copy(estoque = valor) }
    }

    private fun atualizar(transformacao: (FormularioUiState) -> FormularioUiState) {
        _uiState.update { atual ->
            val novo = transformacao(atual)
            if (tentouSalvar) novo.copy(erros = validar(novo)) else novo
        }
    }

    private fun validar(s: FormularioUiState) =
        validarFormulario(s.nome, s.dosagem, s.hora, s.minuto, s.estoque, s.recorrente, s.recorrencia)

    fun salvar() {
        val estado = _uiState.value
        if (estado.salvando) return
        tentouSalvar = true

        val erros = validar(estado)
        if (erros.temErro) {
            _uiState.value = estado.copy(erros = erros)
            return
        }

        _uiState.value = estado.copy(erros = erros, salvando = true)
        viewModelScope.launch {
            val base = original
            val hora = estado.hora!!
            val minuto = estado.minuto!!
            val intervalo = if (estado.recorrente) estado.recorrencia.toInt() else 24
            // Na recorrência, mantém a sequência antiga se horário e intervalo não mudaram;
            // senão calcula a nova primeira dose a partir de agora.
            val referencia = when {
                !estado.recorrente -> null
                base != null && base.tipoHorario == TipoHorario.RECORRENTE && base.referencia != null &&
                    base.hora == hora && base.minuto == minuto && base.intervaloHoras == intervalo -> base.referencia
                else -> primeiraDoseRecorrente(hora, minuto, intervalo, System.currentTimeMillis())
            }
            val medicamento = Medicamento(
                id = estado.id,
                nome = estado.nome.trim(),
                dosagem = estado.dosagem.trim(),
                hora = hora,
                minuto = minuto,
                tipoHorario = estado.tipoHorario,
                intervaloHoras = intervalo,
                referencia = referencia,
                observacao = estado.observacao.trim(),
                estoque = estado.estoque.toIntOrNull() ?: 0,
                lembreteAtivo = estado.lembreteAtivo,
                ultimaTomada = base?.ultimaTomada,
                dataCriacao = base?.dataCriacao ?: System.currentTimeMillis()
            )
            val idSalvo = repository.salvar(medicamento)
            agendador.agendar(medicamento.copy(id = idSalvo))

            val acao = if (estado.editando) "atualizado" else "cadastrado"
            val lembrete = if (medicamento.lembreteAtivo) {
                " Lembrete: ${medicamento.descricaoHorario().replaceFirstChar { it.lowercase() }}."
            } else ""
            _salvo.send("${medicamento.nome} $acao.$lembrete")
        }
    }
}
