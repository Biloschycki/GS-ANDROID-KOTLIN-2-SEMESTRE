package br.com.fiap.horacerta.ui.comum

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.fiap.horacerta.ui.theme.Petroleo
import br.com.fiap.horacerta.ui.theme.PetroleoClaro
import br.com.fiap.horacerta.ui.theme.StatusAtrasada
import br.com.fiap.horacerta.ui.theme.StatusAtrasadaFundo
import br.com.fiap.horacerta.ui.theme.StatusPendente
import br.com.fiap.horacerta.ui.theme.StatusPendenteFundo
import br.com.fiap.horacerta.ui.theme.StatusTomada
import br.com.fiap.horacerta.ui.theme.StatusTomadaFundo
import br.com.fiap.horacerta.domain.doseAtualJaTomada
import br.com.fiap.horacerta.domain.horarioDaDoseAtual
import br.com.fiap.horacerta.data.local.Medicamento

/** Situação da dose da vez de um medicamento. */
enum class StatusDose { TOMADA, HORA_DE_TOMAR, ATRASADA, PENDENTE }

/** Depois do horário, durante este tempo a dose aparece como "Hora de tomar!" (e não "Atrasado"). */
private const val TOLERANCIA_MS = 30 * 60_000L

fun statusDaDose(medicamento: Medicamento, agora: Long): StatusDose = when {
    medicamento.doseAtualJaTomada(agora) -> StatusDose.TOMADA
    else -> {
        val horario = medicamento.horarioDaDoseAtual(agora)
        when {
            agora < horario -> StatusDose.PENDENTE
            agora < horario + TOLERANCIA_MS -> StatusDose.HORA_DE_TOMAR
            else -> StatusDose.ATRASADA
        }
    }
}

/** Etiqueta colorida com ícone + texto (a cor nunca é a única pista do status). */
@Composable
fun EtiquetaStatus(status: StatusDose, modifier: Modifier = Modifier) {
    val (texto, cor, fundo, icone) = when (status) {
        StatusDose.TOMADA -> Quatro("Dose tomada", StatusTomada, StatusTomadaFundo, Icons.Filled.CheckCircle)
        StatusDose.HORA_DE_TOMAR -> Quatro("Hora de tomar!", Petroleo, PetroleoClaro, Icons.Filled.Notifications)
        StatusDose.ATRASADA -> Quatro("Atrasado", StatusAtrasada, StatusAtrasadaFundo, Icons.Filled.Warning)
        StatusDose.PENDENTE -> Quatro("Ainda não é a hora", StatusPendente, StatusPendenteFundo, Icons.Filled.DateRange)
    }
    Row(
        modifier = modifier
            .background(fundo, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, color = cor, style = MaterialTheme.typography.labelMedium)
    }
}

private data class Quatro(val texto: String, val cor: Color, val fundo: Color, val icone: ImageVector)

/** Mensagem amigável quando não há dados, com uma ação opcional. */
@Composable
fun EstadoVazio(
    icone: ImageVector,
    titulo: String,
    texto: String,
    modifier: Modifier = Modifier,
    textoAcao: String? = null,
    onAcao: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Text(
            titulo,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            texto,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (textoAcao != null) {
            Spacer(Modifier.size(4.dp))
            BotaoGrande(texto = textoAcao, onClick = onAcao)
        }
    }
}

/** Botão alto (64dp), fácil de acertar com o dedo. */
@Composable
fun BotaoGrande(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icone: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 64.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (icone != null) {
            Icon(icone, contentDescription = null, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}
