package br.com.fiap.horacerta.domain

import br.com.fiap.horacerta.data.local.Medicamento
import br.com.fiap.horacerta.data.repository.MedicamentoRepository
import br.com.fiap.horacerta.data.repository.ResultadoDose
import br.com.fiap.horacerta.lembrete.AgendadorLembretes
import br.com.fiap.horacerta.util.formatarSoHora
import javax.inject.Inject

/** Abaixo deste número de comprimidos o app avisa que está na hora de comprar mais. */
const val ESTOQUE_BAIXO = 5

/**
 * Regra compartilhada pelas telas Início e Detalhe: confirma a dose da vez,
 * desliga o lembrete pendente e devolve a mensagem que a tela deve mostrar.
 */
class RegistrarDoseUseCase @Inject constructor(
    private val repository: MedicamentoRepository,
    private val agendador: AgendadorLembretes
) {
    suspend operator fun invoke(medicamento: Medicamento): String =
        when (repository.registrarDose(medicamento.id)) {
            ResultadoDose.REGISTRADA -> {
                agendador.doseConfirmada(medicamento.id)
                val restante = repository.buscarPorId(medicamento.id)?.estoque ?: 0
                if (medicamento.estoque > 0 && restante <= ESTOQUE_BAIXO) {
                    if (restante == 1) "Dose de ${medicamento.nome} registrada. Atenção: resta 1 comprimido."
                    else "Dose de ${medicamento.nome} registrada. Atenção: restam $restante comprimidos."
                } else {
                    "Dose de ${medicamento.nome} registrada. Muito bem!"
                }
            }
            ResultadoDose.JA_TOMADA ->
                if (medicamento.ehRecorrente) {
                    val proxima = formatarSoHora(medicamento.horarioDaDoseSeguinte(System.currentTimeMillis()))
                    "Esta dose de ${medicamento.nome} já foi tomada. A próxima é às $proxima."
                } else {
                    "${medicamento.nome} já foi tomado hoje. Não tome de novo."
                }
            ResultadoDose.NAO_ENCONTRADO ->
                "Remédio não encontrado."
        }
}
