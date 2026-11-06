package br.com.fiap.horacerta.ui.sobre

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.fiap.horacerta.ui.theme.HoraCertaTheme

/** Contextualização da solução: problema, público, proposta e ODS. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SobreScreen(onVoltar: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sobre o projeto") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Secao(
                "O problema",
                "Idosos com doenças crônicas costumam tomar vários remédios em horários diferentes. " +
                    "Esquecer uma dose, tomar dobrado ou confundir as caixas é comum, e o familiar " +
                    "que cuida nem sempre está por perto para conferir."
            )
            Secao(
                "Para quem",
                "Pessoas idosas que tomam remédios de uso contínuo e os familiares ou cuidadores " +
                    "que acompanham o tratamento."
            )
            Secao(
                "Como o app ajuda",
                "• Lembrete no horário certo, com notificação e leitura em voz alta\n" +
                    "• Horário fixo (uma vez por dia) ou recorrência (a cada 8 horas, por exemplo)\n" +
                    "• Botão grande \"TOMEI\" para confirmar a dose\n" +
                    "• Bloqueio de dose dobrada\n" +
                    "• Repetição do aviso se a dose não for confirmada\n" +
                    "• Histórico de doses e aviso de estoque baixo"
            )
            Secao(
                "ODS 3 — Saúde e bem-estar",
                "Seguir o tratamento corretamente reduz complicações de hipertensão, diabetes e " +
                    "outras doenças crônicas (meta 3.4: reduzir mortes prematuras por doenças não " +
                    "transmissíveis).",
                destaque = true
            )
            Secao(
                "ODS 10 — Redução das desigualdades",
                "Letras grandes, botões amplos, voz e alto contraste tornam a tecnologia usável por " +
                    "quem tem baixa visão ou pouca familiaridade com celular (meta 10.2: inclusão de " +
                    "todos, independentemente da idade).",
                destaque = true
            )
            Text(
                "Protótipo acadêmico — FIAP Global Solution 2026 (CitizenTech). " +
                    "Os dados ficam apenas neste aparelho; o app não se conecta a nenhum serviço externo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Secao(titulo: String, texto: String, destaque: Boolean = false) {
    Card(
        colors = if (destaque) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleLarge)
            Text(texto, style = MaterialTheme.typography.bodyLarge, color = Color.Unspecified)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SobrePreview() {
    HoraCertaTheme { SobreScreen(onVoltar = {}) }
}
