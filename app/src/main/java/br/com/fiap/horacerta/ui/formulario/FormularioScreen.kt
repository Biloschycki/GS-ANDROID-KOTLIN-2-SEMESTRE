package br.com.fiap.horacerta.ui.formulario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.ui.semantics.Role
import br.com.fiap.horacerta.data.local.TipoHorario
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.horacerta.ui.comum.BotaoGrande
import br.com.fiap.horacerta.ui.theme.HoraCertaTheme

@Composable
fun FormularioScreen(
    onVoltar: () -> Unit,
    onSalvo: (String) -> Unit,
    viewModel: FormularioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.salvo.collect { mensagem -> onSalvo(mensagem) }
    }

    FormularioContent(
        uiState = uiState,
        onVoltar = onVoltar,
        onNomeChange = viewModel::onNomeChange,
        onDosagemChange = viewModel::onDosagemChange,
        onHorarioChange = viewModel::onHorarioChange,
        onTipoHorarioChange = viewModel::onTipoHorarioChange,
        onRecorrenciaChange = viewModel::onRecorrenciaChange,
        onEstoqueChange = viewModel::onEstoqueChange,
        onObservacaoChange = viewModel::onObservacaoChange,
        onLembreteChange = viewModel::onLembreteChange,
        onSalvar = viewModel::salvar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContent(
    uiState: FormularioUiState,
    onVoltar: () -> Unit = {},
    onNomeChange: (String) -> Unit = {},
    onDosagemChange: (String) -> Unit = {},
    onHorarioChange: (Int, Int) -> Unit = { _, _ -> },
    onTipoHorarioChange: (TipoHorario) -> Unit = {},
    onRecorrenciaChange: (String) -> Unit = {},
    onEstoqueChange: (String) -> Unit = {},
    onObservacaoChange: (String) -> Unit = {},
    onLembreteChange: (Boolean) -> Unit = {},
    onSalvar: () -> Unit = {}
) {
    var mostrarRelogio by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.editando) "Editar remédio" else "Novo remédio") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        // Salvar fixo no rodapé: sempre visível, sem precisar rolar a tela
        bottomBar = {
            if (!uiState.carregando) {
                Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                    BotaoGrande(
                        texto = if (uiState.salvando) "Salvando..." else "Salvar",
                        icone = Icons.Filled.Check,
                        onClick = onSalvar,
                        enabled = !uiState.salvando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        if (uiState.carregando) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Campos com * são obrigatórios.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.nome,
                onValueChange = onNomeChange,
                label = { Text("Nome do remédio *") },
                placeholder = { Text("Ex.: Losartana") },
                isError = uiState.erros.nome != null,
                supportingText = textoDeErro(uiState.erros.nome),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.dosagem,
                onValueChange = onDosagemChange,
                label = { Text("Dosagem *") },
                placeholder = { Text("Ex.: 50 mg ou 1 comprimido") },
                isError = uiState.erros.dosagem != null,
                supportingText = textoDeErro(uiState.erros.dosagem),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            // Tipo de horário: duas opções grandes, fáceis de tocar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Como você toma este remédio? *", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OpcaoTipoHorario(
                        titulo = "Horário fixo",
                        descricao = "Uma vez por dia",
                        selecionada = !uiState.recorrente,
                        onClick = { onTipoHorarioChange(TipoHorario.FIXO) },
                        modifier = Modifier.weight(1f)
                    )
                    OpcaoTipoHorario(
                        titulo = "Recorrência",
                        descricao = "A cada tantas horas",
                        selecionada = uiState.recorrente,
                        onClick = { onTipoHorarioChange(TipoHorario.RECORRENTE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (uiState.recorrente) {
                Text("Indique a recorrência do remédio *", style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = uiState.recorrencia,
                    onValueChange = onRecorrenciaChange,
                    label = { Text("A cada quantas horas") },
                    placeholder = { Text("Ex.: 8") },
                    suffix = { Text("horas") },
                    isError = uiState.erros.recorrencia != null,
                    supportingText = {
                        Text(uiState.erros.recorrencia ?: "Ex.: 8 = um lembrete a cada 8 horas")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Horário: botão grande que abre o relógio (mais fácil que digitar "08:00")
            Column {
                Text(
                    if (uiState.recorrente) "Horário da primeira dose *" else "Horário do lembrete *",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.size(6.dp))
                OutlinedButton(
                    onClick = { mostrarRelogio = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)
                ) {
                    Icon(Icons.Filled.DateRange, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Text(
                        uiState.horarioTexto ?: "Escolher horário",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                uiState.erros.horario?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                uiState.previaRecorrencia?.let {
                    Text(
                        "Lembretes: ${it.replaceFirstChar { c -> c.lowercase() }}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }
            }

            OutlinedTextField(
                value = uiState.estoque,
                onValueChange = onEstoqueChange,
                label = { Text("Comprimidos em casa (opcional)") },
                placeholder = { Text("Ex.: 30") },
                isError = uiState.erros.estoque != null,
                supportingText = {
                    Text(uiState.erros.estoque ?: "O app avisa quando estiver acabando")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.observacao,
                onValueChange = onObservacaoChange,
                label = { Text("Observação (opcional)") },
                placeholder = { Text("Ex.: caixa branca e azul, tomar em jejum") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Lembrete com som e voz", style = MaterialTheme.typography.titleSmall)
                    Text(
                        if (uiState.recorrente) "Avisa no horário de cada dose" else "Avisa todo dia no horário escolhido",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = uiState.lembreteAtivo, onCheckedChange = onLembreteChange)
            }
        }
    }

    if (mostrarRelogio) {
        val estadoRelogio = rememberTimePickerState(
            initialHour = uiState.hora ?: 8,
            initialMinute = uiState.minuto ?: 0,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { mostrarRelogio = false },
            title = { Text(if (uiState.recorrente) "Primeira dose" else "Horário do remédio") },
            text = { TimePicker(state = estadoRelogio) },
            confirmButton = {
                TextButton(onClick = {
                    onHorarioChange(estadoRelogio.hour, estadoRelogio.minute)
                    mostrarRelogio = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarRelogio = false }) { Text("Cancelar") }
            }
        )
    }
}

/** Cartão de opção com RadioButton: a área toda é clicável (alvo grande para o dedo). */
@Composable
private fun OpcaoTipoHorario(
    titulo: String,
    descricao: String,
    selecionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.selectable(
            selected = selecionada,
            onClick = onClick,
            role = Role.RadioButton
        ),
        border = BorderStroke(
            width = if (selecionada) 3.dp else 1.dp,
            color = if (selecionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selecionada) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selecionada, onClick = null)
            Column {
                Text(titulo, style = MaterialTheme.typography.titleSmall)
                Text(
                    descricao,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Mostra a mensagem de erro abaixo do campo (ou nada, se o campo está válido). */
private fun textoDeErro(erro: String?): (@Composable () -> Unit)? =
    if (erro == null) null else { { Text(erro) } }

@Preview(showBackground = true)
@Composable
private fun FormularioPreview() {
    HoraCertaTheme {
        FormularioContent(
            uiState = FormularioUiState(
                nome = "Amoxicilina", dosagem = "500 mg", hora = 8, minuto = 0,
                tipoHorario = TipoHorario.RECORRENTE, recorrencia = "8"
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FormularioComErrosPreview() {
    HoraCertaTheme {
        FormularioContent(
            uiState = FormularioUiState(
                erros = validarFormulario(nome = "", dosagem = "", hora = null, minuto = null, estoque = "")
            )
        )
    }
}
