package br.com.fiap.horacerta.ui.detalhe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.horacerta.data.local.Medicamento
import br.com.fiap.horacerta.data.local.RegistroDose
import br.com.fiap.horacerta.domain.ESTOQUE_BAIXO
import br.com.fiap.horacerta.ui.comum.BotaoGrande
import br.com.fiap.horacerta.ui.comum.DialogoExclusao
import br.com.fiap.horacerta.ui.comum.EstadoVazio
import br.com.fiap.horacerta.ui.comum.EtiquetaStatus
import br.com.fiap.horacerta.ui.comum.StatusDose
import br.com.fiap.horacerta.ui.comum.rememberFalador
import br.com.fiap.horacerta.ui.theme.HoraCertaTheme
import br.com.fiap.horacerta.util.formatarDataHora
import br.com.fiap.horacerta.domain.descricaoHorario
import br.com.fiap.horacerta.util.formatarSoHora
import br.com.fiap.horacerta.util.textoDoLembrete

@Composable
fun DetalheScreen(
    onVoltar: () -> Unit,
    onEditar: (Long) -> Unit,
    onExcluido: (String) -> Unit,
    mensagemRetorno: String?,
    onMensagemExibida: () -> Unit,
    viewModel: DetalheViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val falador = rememberFalador()

    LaunchedEffect(Unit) {
        viewModel.mensagens.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(Unit) {
        viewModel.excluido.collect { onExcluido(it) }
    }
    LaunchedEffect(mensagemRetorno) {
        mensagemRetorno?.let {
            snackbarHostState.showSnackbar(it)
            onMensagemExibida()
        }
    }

    DetalheContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onVoltar = onVoltar,
        onEditar = onEditar,
        onExcluir = viewModel::excluir,
        onTomei = viewModel::marcarComoTomado,
        onOuvir = { m -> falador.falar(textoDoLembrete(m.nome, m.dosagem, m.observacao)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalheContent(
    uiState: DetalheUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onVoltar: () -> Unit = {},
    onEditar: (Long) -> Unit = {},
    onExcluir: (Medicamento) -> Unit = {},
    onTomei: (Medicamento) -> Unit = {},
    onOuvir: (Medicamento) -> Unit = {}
) {
    var confirmarExclusao by remember { mutableStateOf(false) }
    val pronto = uiState as? DetalheUiState.Pronto

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pronto?.medicamento?.nome ?: "Remédio") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (uiState) {
                is DetalheUiState.Carregando ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                is DetalheUiState.NaoEncontrado -> EstadoVazio(
                    icone = Icons.Filled.Warning,
                    titulo = "Remédio não encontrado",
                    texto = "Ele pode ter sido excluído.",
                    textoAcao = "Voltar",
                    onAcao = onVoltar
                )

                is DetalheUiState.Pronto -> ConteudoDetalhe(
                    estado = uiState,
                    onTomei = { onTomei(uiState.medicamento) },
                    onOuvir = { onOuvir(uiState.medicamento) },
                    onEditar = { onEditar(uiState.medicamento.id) },
                    onExcluir = { confirmarExclusao = true }
                )
            }
        }
    }

    if (confirmarExclusao && pronto != null) {
        DialogoExclusao(
            nome = pronto.medicamento.nome,
            onConfirmar = {
                confirmarExclusao = false
                onExcluir(pronto.medicamento)
            },
            onCancelar = { confirmarExclusao = false }
        )
    }
}

@Composable
private fun ConteudoDetalhe(
    estado: DetalheUiState.Pronto,
    onTomei: () -> Unit,
    onOuvir: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    val m = estado.medicamento
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(m.nome, style = MaterialTheme.typography.headlineMedium)
                    Text(m.dosagem, style = MaterialTheme.typography.titleMedium)
                    Text(
                        m.descricaoHorario(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Dose da vez: ${formatarSoHora(estado.horarioDose)}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (m.observacao.isNotBlank()) {
                        Text(m.observacao, style = MaterialTheme.typography.bodyLarge)
                    }
                    Text(
                        when {
                            m.estoque == 0 -> "Estoque não informado"
                            m.estoque <= ESTOQUE_BAIXO -> "Estoque baixo: ${m.estoque} " +
                                (if (m.estoque == 1) "comprimido" else "comprimidos")
                            else -> "Estoque: ${m.estoque} comprimidos"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (m.estoque in 1..ESTOQUE_BAIXO) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (m.lembreteAtivo) "Lembrete ligado" else "Lembrete desligado",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    EtiquetaStatus(estado.status)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotaoGrande(
                    texto = if (estado.status == StatusDose.TOMADA) "Dose já tomada" else "TOMEI",
                    icone = Icons.Filled.Check,
                    onClick = onTomei,
                    enabled = estado.status != StatusDose.TOMADA,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(onClick = onOuvir, modifier = Modifier.heightIn(min = 64.dp)) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("Ouvir")
                }
            }
        }

        // Editar e excluir como botões grandes e com texto (sem ícones pequenos no topo)
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onEditar,
                    modifier = Modifier.weight(1f).heightIn(min = 56.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("Editar")
                }
                OutlinedButton(
                    onClick = onExcluir,
                    modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("Excluir")
                }
            }
        }

        item {
            Text("Histórico de doses", style = MaterialTheme.typography.headlineSmall)
        }

        if (estado.historico.isEmpty()) {
            item {
                EstadoVazio(
                    icone = Icons.Filled.DateRange,
                    titulo = "Nenhuma dose registrada",
                    texto = "Quando você tocar em TOMEI, a data e a hora aparecem aqui."
                )
            }
        } else {
            items(estado.historico, key = { it.id }) { registro ->
                ListItem(
                    headlineContent = { Text(formatarDataHora(registro.tomadoEm)) },
                    leadingContent = {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetalhePreview() {
    val agora = System.currentTimeMillis()
    HoraCertaTheme {
        DetalheContent(
            uiState = DetalheUiState.Pronto(
                medicamento = Medicamento(
                    id = 1, nome = "Losartana", dosagem = "50 mg", hora = 8, minuto = 0,
                    observacao = "Caixa branca e azul", estoque = 4, ultimaTomada = agora
                ),
                status = StatusDose.TOMADA,
                horarioDose = agora,
                historico = listOf(
                    RegistroDose(id = 2, medicamentoId = 1, tomadoEm = agora),
                    RegistroDose(id = 1, medicamentoId = 1, tomadoEm = agora - 86_400_000L)
                )
            )
        )
    }
}
