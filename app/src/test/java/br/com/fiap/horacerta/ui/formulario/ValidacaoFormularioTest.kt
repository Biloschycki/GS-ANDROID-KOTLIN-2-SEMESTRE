package br.com.fiap.horacerta.ui.formulario

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacaoFormularioTest {

    @Test
    fun formularioCompletoEhValido() {
        val erros = validarFormulario("Losartana", "50 mg", 8, 0, "30")
        assertFalse(erros.temErro)
    }

    @Test
    fun estoqueVazioEhPermitido() {
        assertNull(validarFormulario("Losartana", "50 mg", 8, 0, "").estoque)
    }

    @Test
    fun camposObrigatoriosVaziosGeramErro() {
        val erros = validarFormulario("   ", "", null, null, "")
        assertTrue(erros.temErro)
        assertNotNull(erros.nome)
        assertNotNull(erros.dosagem)
        assertNotNull(erros.horario)
    }

    @Test
    fun nomeComUmaLetraEhInvalido() {
        assertNotNull(validarFormulario("A", "1 comprimido", 8, 0, "").nome)
    }

    @Test
    fun estoqueForaDoLimiteOuNaoNumericoEhInvalido() {
        assertNotNull(validarFormulario("Losartana", "50 mg", 8, 0, "abc").estoque)
        assertNotNull(validarFormulario("Losartana", "50 mg", 8, 0, "1000").estoque)
    }

    @Test
    fun recorrenciaEhObrigatoriaQuandoSelecionada() {
        assertNotNull(validarFormulario("Amoxicilina", "500 mg", 8, 0, "", recorrente = true, recorrencia = "").recorrencia)
        assertNull(validarFormulario("Amoxicilina", "500 mg", 8, 0, "", recorrente = true, recorrencia = "8").recorrencia)
    }

    @Test
    fun recorrenciaForaDe1a24HorasEhInvalida() {
        assertNotNull(validarFormulario("Amoxicilina", "500 mg", 8, 0, "", recorrente = true, recorrencia = "0").recorrencia)
        assertNotNull(validarFormulario("Amoxicilina", "500 mg", 8, 0, "", recorrente = true, recorrencia = "25").recorrencia)
    }

    @Test
    fun recorrenciaIgnoradaNoHorarioFixo() {
        assertNull(validarFormulario("Losartana", "50 mg", 8, 0, "", recorrente = false, recorrencia = "abc").recorrencia)
    }
}
