package br.com.fiap.horacerta.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = Petroleo,
    onPrimary = Color.White,
    primaryContainer = PetroleoClaro,
    onPrimaryContainer = PetroleoEscuro,
    secondary = AmbarEscuro,
    onSecondary = Color.White,
    secondaryContainer = AmbarClaro,
    onSecondaryContainer = AmbarEscuro,
    tertiary = Ambar,
    onTertiary = TextoPrincipal,
    background = Fundo,
    onBackground = TextoPrincipal,
    surface = Superficie,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieVariante,
    onSurfaceVariant = TextoSecundario,
    error = Erro,
    onError = Color.White,
    errorContainer = ErroClaro,
    onErrorContainer = Erro
)

private val EsquemaEscuro = darkColorScheme(
    primary = PetroleoNoturno,
    onPrimary = PetroleoEscuro,
    primaryContainer = PetroleoEscuro,
    onPrimaryContainer = PetroleoClaro,
    secondary = Ambar,
    onSecondary = AmbarEscuro,
    secondaryContainer = AmbarEscuro,
    onSecondaryContainer = AmbarClaro,
    tertiary = Ambar,
    onTertiary = TextoPrincipal,
    background = FundoNoturno,
    onBackground = Color(0xFFE3ECE9),
    surface = SuperficieNoturna,
    onSurface = Color(0xFFE3ECE9),
    surfaceVariant = SuperficieVarianteNoturna,
    onSurfaceVariant = Color(0xFFBFCAC6)
)

/**
 * Sem cores dinâmicas (Material You) de propósito: a identidade visual do app
 * fica igual em todos os aparelhos e o contraste fica sob nosso controle.
 */
@Composable
fun HoraCertaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaEscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}
