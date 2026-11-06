package br.com.fiap.horacerta.ui.medicamentos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.horacerta.data.local.Medicamento
import br.com.fiap.horacerta.domain.ESTOQUE_BAIXO
import br.com.fiap.horacerta.ui.comum.BotaoGrande
import br.com.fiap.horacerta.ui.comum.DialogoExclusao
import br.com.fiap.horacerta.ui.comum.EstadoVazio
import br.com.fiap.horacerta.ui.theme.HoraCertaTheme
import br.com.fiap.horacerta.domain.descricaoHorario

@Composable
fun MedicamentosScreen(
    onVoltar: () -> Unit,
    onNovo: () -> Unit,
    onEditar: (Long) -> Unit,
    onAbrir: (Long) -> Unit,
    mensagemRetorno: String?,
    onMensagemExibida: () -> Unit,
    viewModel: MedicamentosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.mensagens.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(mensagemRetorno) {
        mensagemRetorno?.let {
            snackbarHostState.showSnackbar(it)
            onMensagemExibida()
        }
    }

    MedicamentosContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onVoltar = onVoltar,
        onNovo = onNovo,
        onEditar = onEditar,
        onAbrir = onAbrir,
        onExcluir = viewModel::excluir,
        onAlterarLembrete = viewModel::alterarLembrete
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicamentosContent(
    uiState: MedicamentosUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onVoltar: () -> Unit = {},
    onNovo: () -> Unit = {},
    onEditar: (Long) -> Unit = {},
    onAbrir: (Long) -> Unit = {},
    onExcluir: (Medicamento) -> Unit = {},
    onAlterarLembrete: (Medicamento, Boolean) -> Unit = { _, _ -> }
) {
    var paraExcluir by remember { mutableStateOf<Medicamento?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus remédios") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            // Botão fixo no topo, sempre no mesmo lugar (substitui o botão flutuante,
            // que ficava por cima do conteúdo e confundia o público idoso).
            BotaoGrande(
                texto = "Cadastrar novo remédio",
                icone = Icons.Filled.Add,
                onClick = onNovo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Box(Modifier.fillMaxSize()) {
                when (uiState) {
                    is MedicamentosUiState.Carregando ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center))

                    is MedicamentosUiState.Pronto -> if (uiState.medicamentos.isEmpty()) {
                        EstadoVazio(
                            icone = Icons.AutoMirrored.Filled.List,
                            titulo = "Sua lista está vazia",
                            texto = "Toque em \"Cadastrar novo remédio\" para começar.",
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.medicamentos, key = { it.id }) { medicamento ->
                                CartaoMedicamento(
                                    medicamento = medicamento,
                                    onAbrir = { onAbrir(medicamento.id) },
                                    onEditar = { onEditar(medicamento.id) },
                                    onExcluir = { paraExcluir = medicamento },
                                    onAlterarLembrete = { onAlterarLembrete(medicamento, it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    paraExcluir?.let { medicamento ->
        DialogoExclusao(
            nome = medicamento.nome,
            onConfirmar = {
                onExcluir(medicamento)
                paraExcluir = null
            },
            onCancelar = { paraExcluir = null }
        )
    }
}

@Composable
private fun CartaoMedicamento(
    medicamento: Medicamento,
    onAbrir: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
    onAlterarLembrete: (Boolean) -> Unit
) {
    Card(onClick = onAbrir, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(medicamento.nome, style = MaterialTheme.typography.titleLarge)
                    Text(medicamento.dosagem, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        medicamento.descricaoHorario(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onEditar) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar ${medicamento.nome}")
                }
                IconButton(onClick = onExcluir) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Excluir ${medicamento.nome}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (medicamento.estoque in 1..ESTOQUE_BAIXO) {
                Spacer(Modifier.size(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "Estoque baixo: ${medicamento.estoque} " +
                            (if (medicamento.estoque == 1) "restante" else "restantes"),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (medicamento.estoque > ESTOQUE_BAIXO) {
                Text(
                    "Estoque: ${medicamento.estoque}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (medicamento.lembreteAtivo) "Lembrete ligado" else "Lembrete desligado",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = medicamento.lembreteAtivo,
                    onCheckedChange = onAlterarLembrete,
                    modifier = Modifier.semantics {
                        contentDescription = "Lembrete de ${medicamento.nome}"
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicamentosPreview() {
    HoraCertaTheme {
        MedicamentosContent(
            uiState = MedicamentosUiState.Pronto(
                listOf(
                    Medicamento(id = 1, nome = "Losartana", dosagem = "50 mg", hora = 8, minuto = 0, estoque = 20),
                    Medicamento(id = 2, nome = "Metformina", dosagem = "1 comprimido", hora = 13, minuto = 0,
                        estoque = 3, lembreteAtivo = false)
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicamentosVazioPreview() {
    HoraCertaTheme {
        MedicamentosContent(uiState = MedicamentosUiState.Pronto(emptyList()))
    }
}
