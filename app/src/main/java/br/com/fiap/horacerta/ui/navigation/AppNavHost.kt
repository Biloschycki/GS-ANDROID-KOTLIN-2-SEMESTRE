package br.com.fiap.horacerta.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.fiap.horacerta.ui.detalhe.DetalheScreen
import br.com.fiap.horacerta.ui.formulario.FormularioScreen
import br.com.fiap.horacerta.ui.inicio.InicioScreen
import br.com.fiap.horacerta.ui.medicamentos.MedicamentosScreen
import br.com.fiap.horacerta.ui.sobre.SobreScreen

/** Chave usada para mandar uma mensagem de volta para a tela anterior (ex.: "Remédio salvo"). */
private const val CHAVE_MENSAGEM = "mensagem"

/**
 * Grafo de navegação. As telas não conhecem o NavController: recebem callbacks
 * (onVoltar, onNovo...) e o NavHost decide para onde ir.
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Inicio.route) {

        composable(Screen.Inicio.route) { entry ->
            val mensagem by entry.mensagemRetorno()
            InicioScreen(
                onMeusRemedios = { navController.navigate(Screen.Medicamentos.route) },
                onNovoRemedio = { navController.navigate(Screen.Formulario.createRoute()) },
                onAbrirRemedio = { id -> navController.navigate(Screen.Detalhe.createRoute(id)) },
                onSobre = { navController.navigate(Screen.Sobre.route) },
                mensagemRetorno = mensagem,
                onMensagemExibida = { entry.limparMensagem() }
            )
        }

        composable(Screen.Medicamentos.route) { entry ->
            val mensagem by entry.mensagemRetorno()
            MedicamentosScreen(
                onVoltar = { navController.voltar() },
                onNovo = { navController.navigate(Screen.Formulario.createRoute()) },
                onEditar = { id -> navController.navigate(Screen.Formulario.createRoute(id)) },
                onAbrir = { id -> navController.navigate(Screen.Detalhe.createRoute(id)) },
                mensagemRetorno = mensagem,
                onMensagemExibida = { entry.limparMensagem() }
            )
        }

        composable(
            route = Screen.Formulario.route,
            arguments = listOf(
                navArgument(Screen.ARG_ID) {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            FormularioScreen(
                onVoltar = { navController.voltar() },
                onSalvo = { mensagem -> navController.voltarComMensagem(mensagem) }
            )
        }

        composable(
            route = Screen.Detalhe.route,
            arguments = listOf(navArgument(Screen.ARG_ID) { type = NavType.LongType })
        ) { entry ->
            val mensagem by entry.mensagemRetorno()
            DetalheScreen(
                onVoltar = { navController.voltar() },
                onEditar = { id -> navController.navigate(Screen.Formulario.createRoute(id)) },
                onExcluido = { msg -> navController.voltarComMensagem(msg) },
                mensagemRetorno = mensagem,
                onMensagemExibida = { entry.limparMensagem() }
            )
        }

        composable(Screen.Sobre.route) {
            SobreScreen(onVoltar = { navController.voltar() })
        }
    }
}

/** Grava a mensagem na tela anterior e volta para ela. */
private fun NavHostController.voltarComMensagem(mensagem: String) {
    previousBackStackEntry?.savedStateHandle?.set(CHAVE_MENSAGEM, mensagem)
    voltar()
}

/** Volta uma tela, mas nunca esvazia a pilha (evita tela branca com toque duplo em "Voltar"). */
private fun NavHostController.voltar() {
    if (previousBackStackEntry != null) popBackStack()
}

@Composable
private fun NavBackStackEntry.mensagemRetorno() =
    savedStateHandle.getStateFlow<String?>(CHAVE_MENSAGEM, null).collectAsStateWithLifecycle()

private fun NavBackStackEntry.limparMensagem() {
    savedStateHandle.set<String?>(CHAVE_MENSAGEM, null)
}
