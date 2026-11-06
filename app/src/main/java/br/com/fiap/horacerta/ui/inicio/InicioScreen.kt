package br.com.fiap.horacerta.ui.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.horacerta.data.local.Medicamento
import br.com.fiap.horacerta.data.local.TipoHorario
import br.com.fiap.horacerta.domain.descricaoHorario
import br.com.fiap.horacerta.ui.comum.BotaoGrande
import br.com.fiap.horacerta.ui.comum.EstadoVazio
import br.com.fiap.horacerta.ui.comum.EtiquetaStatus
import br.com.fiap.horacerta.ui.comum.StatusDose
import br.com.fiap.horacerta.ui.comum.rememberFalador
import br.com.fiap.horacerta.ui.theme.HoraCertaTheme
import br.com.fiap.horacerta.util.formatarDataPorExtenso
import br.com.fiap.horacerta.util.formatarSoHora
import br.com.fiap.horacerta.util.textoDoLembrete

@Composable
fun InicioScreen(
    onMeusRemedios: () -> Unit,
    onNovoRemedio: () -> Unit,
    onAbrirRemedio: (Long) -> Unit,
    onSobre: () -> Unit,
    mensagemRetorno: String?,
    onMensagemExibida: () -> Unit,
    viewModel: InicioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val falador = rememberFalador()

    LaunchedEffect(Unit) {
        viewModel.mensagens.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(mensagemRetorno) {
        mensagemRetorno?.let {
            snackbarHostState.showSnackbar(it)
            onMensagemExibida()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        InicioContent(
            uiState = uiState,
            modifier = Modifier.padding(innerPadding),
            onMeusRemedios = onMeusRemedios,
            onNovoRemedio = onNovoRemedio,
            onAbrirRemedio = onAbrirRemedio,
            onSobre = onSobre,
            onTomei = viewModel::marcarComoTomado,
            onOuvir = { m -> falador.falar(textoDoLembrete(m.nome, m.dosagem, m.observacao)) }
        )
    }
}

@Composable
fun InicioContent(
    uiState: InicioUiState,
    modifier: Modifier = Modifier,
    onMeusRemedios: () -> Unit = {},
    onNovoRemedio: () -> Unit = {},
    onAbrirRemedio: (Long) -> Unit = {},
    onSobre: () -> Unit = {},
    onTomei: (Medicamento) -> Unit = {},
    onOuvir: (Medicamento) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Cabecalho(agora = uiState.agora) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotaoGrande(
                    texto = "Meus remédios",
                    icone = Icons.AutoMirrored.Filled.List,
                    onClick = onMeusRemedios,
                    modifier = Modifier.weight(1f)
                )
                BotaoGrande(
                    texto = "Novo",
                    icone = Icons.Filled.Add,
                    onClick = onNovoRemedio,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when {
            uiState.carregando -> item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.doses.isEmpty() -> item {
                EstadoVazio(
                    icone = Icons.Filled.Notifications,
                    titulo = "Nenhum remédio cadastrado",
                    texto = "Cadastre o primeiro remédio e o app avisa na hora certa, com som e voz.",
                    textoAcao = "Cadastrar remédio",
                    onAcao = onNovoRemedio
                )
            }

            else -> {
                item { ResumoDoDia(uiState) }
                item {
                    Text(
                        "Doses de hoje",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.semantics { heading() }
                    )
                }
                items(uiState.doses, key = { it.medicamento.id }) { dose ->
                    CartaoDose(
                        dose = dose,
                        onTomei = { onTomei(dose.medicamento) },
                        onOuvir = { onOuvir(dose.medicamento) },
                        onAbrir = { onAbrirRemedio(dose.medicamento.id) }
                    )
                }
            }
        }

        item {
            TextButton(onClick = onSobre, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Info, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Sobre o projeto e os ODS")
            }
        }
    }
}

@Composable
private fun Cabecalho(agora: Long) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(formatarDataPorExtenso(agora), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "Hora Certa",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Seu remédio na hora certa: sem esquecer e sem tomar dobrado.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Para idosos e seus cuidadores.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ResumoDoDia(uiState: InicioUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "${uiState.tomadas} de ${uiState.total} " +
                    (if (uiState.total == 1) "remédio em dia" else "remédios em dia"),
                style = MaterialTheme.typography.titleLarge
            )
            LinearProgressIndicator(
                progress = { if (uiState.total == 0) 0f else uiState.tomadas / uiState.total.toFloat() },
                modifier = Modifier.fillMaxWidth().height(12.dp),
                trackColor = MaterialTheme.colorScheme.surface,
                drawStopIndicator = {} // sem o "pontinho" no fim da barra
            )
            val proxima = uiState.proxima
            Text(
                text = when {
                    proxima == null -> "Tudo certo por hoje!"
                    uiState.atrasadas == 1 -> "1 dose atrasada. Confira abaixo."
                    uiState.atrasadas > 1 -> "${uiState.atrasadas} doses atrasadas. Confira abaixo."
                    proxima.status == StatusDose.HORA_DE_TOMAR -> "Hora de tomar: ${proxima.medicamento.nome}!"
                    else -> "Próxima: ${proxima.medicamento.nome} às " +
                        formatarSoHora(proxima.horarioDose)
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun CartaoDose(
    dose: DoseDoDia,
    onTomei: () -> Unit,
    onOuvir: () -> Unit,
    onAbrir: () -> Unit
) {
    val m = dose.medicamento
    Card(onClick = onAbrir, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatarSoHora(dose.horarioDose),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(m.nome, style = MaterialTheme.typography.titleLarge)
                    Text(m.dosagem, style = MaterialTheme.typography.bodyLarge)
                }
            }
            if (m.tipoHorario == TipoHorario.RECORRENTE) {
                Text(
                    m.descricaoHorario(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (m.observacao.isNotBlank()) {
                Text(
                    m.observacao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            EtiquetaStatus(dose.status)
            dose.proximaDose?.let {
                Text("Próxima dose às ${formatarSoHora(it)}", style = MaterialTheme.typography.bodyLarge)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (dose.status == StatusDose.TOMADA) {
                    val hora = m.ultimaTomada?.let { formatarSoHora(it) } ?: ""
                    FilledTonalButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp)
                    ) { Text("Tomado às $hora") }
                } else {
                    BotaoGrande(
                        texto = "TOMEI",
                        icone = Icons.Filled.Check,
                        onClick = onTomei,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedButton(onClick = onOuvir, modifier = Modifier.heightIn(min = 56.dp)) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("Ouvir")
                }
            }
        }
    }
}

// ---------- Previews ----------

private val exemplos = listOf(
    Medicamento(id = 1, nome = "Losartana", dosagem = "50 mg", hora = 8, minuto = 0,
        observacao = "Caixa branca e azul", estoque = 20, ultimaTomada = System.currentTimeMillis()),
    Medicamento(id = 2, nome = "Metformina", dosagem = "1 comprimido", hora = 13, minuto = 0,
        observacao = "Depois do almoço", estoque = 4),
    Medicamento(id = 4, nome = "Amoxicilina", dosagem = "500 mg", hora = 8, minuto = 0,
        tipoHorario = TipoHorario.RECORRENTE, intervaloHoras = 8, referencia = 0L),
    Medicamento(id = 3, nome = "Sinvastatina", dosagem = "20 mg", hora = 21, minuto = 30)
)

@Preview(showBackground = true)
@Composable
private fun InicioPreview() {
    HoraCertaTheme {
        InicioContent(
            uiState = InicioUiState(
                carregando = false,
                doses = listOf(
                    DoseDoDia(exemplos[0], StatusDose.TOMADA, horarioDose = 8 * 3_600_000L + 3 * 3_600_000L),
                    DoseDoDia(exemplos[1], StatusDose.ATRASADA, horarioDose = 16 * 3_600_000L),
                    DoseDoDia(exemplos[3], StatusDose.TOMADA, horarioDose = 11 * 3_600_000L,
                        proximaDose = 19 * 3_600_000L),
                    DoseDoDia(exemplos[2], StatusDose.PENDENTE, horarioDose = 24 * 3_600_000L + 30 * 60_000L)
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InicioVazioPreview() {
    HoraCertaTheme {
        InicioContent(uiState = InicioUiState(carregando = false))
    }
}
