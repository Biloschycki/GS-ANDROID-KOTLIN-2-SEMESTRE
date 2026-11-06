package br.com.fiap.horacerta.ui.formulario

/** Erros de cada campo do formulário. `null` = campo válido. */
data class ErrosFormulario(
    val nome: String? = null,
    val dosagem: String? = null,
    val horario: String? = null,
    val recorrencia: String? = null,
    val estoque: String? = null
) {
    val temErro: Boolean get() = listOfNotNull(nome, dosagem, horario, recorrencia, estoque).isNotEmpty()
}

/**
 * Regras do formulário de cadastro (função pura, testada em ValidacaoFormularioTest):
 * - nome: obrigatório, pelo menos 2 letras
 * - dosagem: obrigatória
 * - horário: obrigatório (o usuário precisa escolher no relógio)
 * - recorrência: só quando [recorrente]; número inteiro de 1 a 24 (horas)
 * - estoque: opcional, mas se preenchido precisa ser um número inteiro entre 0 e 999
 */
fun validarFormulario(
    nome: String,
    dosagem: String,
    hora: Int?,
    minuto: Int?,
    estoque: String,
    recorrente: Boolean = false,
    recorrencia: String = ""
): ErrosFormulario = ErrosFormulario(
    nome = when {
        nome.isBlank() -> "Informe o nome do remédio"
        nome.trim().length < 2 -> "O nome precisa ter pelo menos 2 letras"
        else -> null
    },
    dosagem = if (dosagem.isBlank()) "Informe a dosagem (ex.: 1 comprimido, 50 mg)" else null,
    horario = when {
        hora != null && minuto != null -> null
        recorrente -> "Escolha o horário da primeira dose"
        else -> "Escolha o horário do lembrete"
    },
    recorrencia = if (!recorrente) null else recorrencia.trim().let { texto ->
        when {
            texto.isEmpty() -> "Indique de quantas em quantas horas"
            texto.toIntOrNull() == null -> "Use apenas números"
            texto.toInt() !in 1..24 -> "Informe um valor entre 1 e 24 horas"
            else -> null
        }
    },
    estoque = estoque.trim().let { texto ->
        when {
            texto.isEmpty() -> null
            texto.toIntOrNull() == null -> "Use apenas números"
            texto.toInt() !in 0..999 -> "Informe um valor entre 0 e 999"
            else -> null
        }
    }
)
