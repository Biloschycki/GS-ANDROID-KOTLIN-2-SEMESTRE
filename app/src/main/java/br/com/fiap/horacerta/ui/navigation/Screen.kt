package br.com.fiap.horacerta.ui.navigation

/** Rotas do app. Os argumentos são lidos nos ViewModels via SavedStateHandle. */
sealed class Screen(val route: String) {
    data object Inicio : Screen("inicio")
    data object Medicamentos : Screen("medicamentos")
    data object Sobre : Screen("sobre")

    /** id = 0 → cadastro novo; id > 0 → edição. */
    data object Formulario : Screen("formulario?$ARG_ID={$ARG_ID}") {
        fun createRoute(id: Long = 0L) = "formulario?$ARG_ID=$id"
    }

    data object Detalhe : Screen("detalhe/{$ARG_ID}") {
        fun createRoute(id: Long) = "detalhe/$id"
    }

    companion object {
        const val ARG_ID = "id"
    }
}
