package tech.ada.banco.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tech.ada.banco.model.Conta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DepositoControllerTest extends BaseContaTest {

    private final String baseUri = "/deposito";

    @Test
    void testDepositoContaInicioNaoZero() throws Exception {
        Conta contaBase = criarConta(BigDecimal.valueOf(15));

        String response =
                mvc.perform(post(baseUri + "/" + contaBase.getNumeroConta())
                                .param("valor", "30")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getErrorMessage();

        contaBase = obtemContaDoBanco(contaBase);
        assertEquals(BigDecimal.valueOf(45).setScale(2, RoundingMode.HALF_UP), contaBase.getSaldo());
    }

    @Test
    void testDepositoContaInicioZero() throws Exception {
        Conta contaBase = criarConta(BigDecimal.ZERO);

        String response =
                mvc.perform(post(baseUri + "/" + contaBase.getNumeroConta())
                                .param("valor", "30")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getErrorMessage();

        contaBase = obtemContaDoBanco(contaBase);
        assertEquals(BigDecimal.valueOf(30).setScale(2, RoundingMode.HALF_UP), contaBase.getSaldo());
    }

    @Test
    void testDepositoNegativo() throws Exception {
        Conta contaBase = criarConta(BigDecimal.ONE);

        String response =
                mvc.perform(post(baseUri + "/" + contaBase.getNumeroConta())
                                .param("valor", "-1")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andReturn().getResponse().getErrorMessage();

        contaBase = obtemContaDoBanco(contaBase);
        assertEquals("Valor informado está inválido.", response);
        assertEquals(BigDecimal.ONE, contaBase.getSaldo());

    }

    @Test
    void testDepositoValorQuebrado() throws Exception {
        Conta contaBase = criarConta(BigDecimal.TEN);

        String response =
                mvc.perform(post(baseUri + "/" + contaBase.getNumeroConta())
                                .param("valor", "7.1")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getErrorMessage();

        contaBase = obtemContaDoBanco(contaBase);
        assertEquals(BigDecimal.valueOf(17.1).setScale(2, RoundingMode.HALF_UP), contaBase.getSaldo());
    }

    @Test
    void testDepositoContaInvalida() throws Exception {
        Conta contaBase = criarConta(BigDecimal.ONE);
        Optional<Conta> contaInexistente = repository.findContaByNumeroConta(9988);
        assertTrue(contaInexistente.isEmpty());

        String response =
                mvc.perform(post(baseUri + "/9988")
                                .param("valor", "4")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andReturn().getResponse().getContentAsString();

        contaBase = obtemContaDoBanco(contaBase);
        assertEquals(BigDecimal.valueOf(1), contaBase.getSaldo());
    }


}