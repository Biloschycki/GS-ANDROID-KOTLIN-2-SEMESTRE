package br.com.fiap.horacerta

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import br.com.fiap.horacerta.data.repository.MedicamentoRepository
import br.com.fiap.horacerta.lembrete.AgendadorLembretes
import br.com.fiap.horacerta.ui.navigation.AppNavHost
import br.com.fiap.horacerta.ui.theme.HoraCertaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var repository: MedicamentoRepository

    @Inject lateinit var agendador: AgendadorLembretes

    private val pedirPermissaoNotificacao = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* se negar, o app continua funcionando; só não mostra notificação */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pedirPermissaoNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Garante que todo remédio ativo tenha seu alarme agendado
        // (ex.: depois de reinstalar o app ou limpar os alarmes do sistema).
        lifecycleScope.launch {
            repository.listarTodos().forEach { agendador.agendar(it) }
        }

        setContent {
            HoraCertaTheme {
                AppNavHost()
            }
        }
    }
}
