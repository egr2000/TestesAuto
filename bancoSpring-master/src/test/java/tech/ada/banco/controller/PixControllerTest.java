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

class PixControllerTest extends BaseContaTest {

    private final String baseUri = "/pix";

    @Test
    void testPix() throws Exception {
        Conta contaBaseOrig = criarConta(BigDecimal.valueOf(23));
        Conta contaBaseDest = criarConta(BigDecimal.ONE);

        String response =
                mvc.perform(post(baseUri + "/" + contaBaseOrig.getNumeroConta())
                                .param("destino", String.valueOf(contaBaseDest.getNumeroConta()))
                                .param("valor", "18")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getErrorMessage();

        contaBaseOrig = obtemContaDoBanco(contaBaseOrig);
        contaBaseDest = obtemContaDoBanco(contaBaseDest);

        assertEquals(BigDecimal.valueOf(19), contaBaseDest.getSaldo());
        assertEquals(BigDecimal.valueOf(5), contaBaseOrig.getSaldo());

    }

    @Test
    void testPixAcimaLimite() throws Exception {
        Conta contaBaseOrig = criarConta(BigDecimal.TEN);
        Conta contaBaseDest = criarConta(BigDecimal.ONE);

        String response =
                mvc.perform(post(baseUri + "/" + contaBaseOrig.getNumeroConta())
                                .param("destino", String.valueOf(contaBaseDest.getNumeroConta()))
                                .param("valor", "18")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andReturn().getResponse().getErrorMessage();

        assertEquals("Limite acima do saldo disponível!", response);
        assertEquals(BigDecimal.valueOf(1), contaBaseDest.getSaldo());
        assertEquals(BigDecimal.valueOf(10), contaBaseOrig.getSaldo());
    }

    @Test
    void testPixQuebrado() throws Exception {
        Conta contaBaseOrig = criarConta(BigDecimal.valueOf(16.3));
        Conta contaBaseDest = criarConta(BigDecimal.valueOf(5.72));

        String response =
                mvc.perform(post(baseUri + "/" + contaBaseOrig.getNumeroConta())
                                .param("destino", String.valueOf(contaBaseDest.getNumeroConta()))
                                .param("valor", "1.37")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getErrorMessage();

        contaBaseOrig = obtemContaDoBanco(contaBaseOrig);
        contaBaseDest = obtemContaDoBanco(contaBaseDest);

        assertEquals(BigDecimal.valueOf(7.09), contaBaseDest.getSaldo());
        assertEquals(BigDecimal.valueOf(14.93), contaBaseOrig.getSaldo());

    }

    @Test
    void testPixValorNegativo() throws Exception {
        Conta contaBaseOrig = criarConta(BigDecimal.TEN);
        Conta contaBaseDest = criarConta(BigDecimal.TEN);

        String response =
                mvc.perform(post(baseUri + "/" + contaBaseOrig.getNumeroConta())
                                .param("destino", String.valueOf(contaBaseDest.getNumeroConta()))
                                .param("valor", "-3")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andReturn().getResponse().getErrorMessage();

        assertEquals("Valor informado está inválido.", response);
        assertEquals(BigDecimal.TEN, contaBaseDest.getSaldo());
        assertEquals(BigDecimal.TEN, contaBaseOrig.getSaldo());

    }

    @Test
    void testPixContaInvalidaOrig() throws Exception {
        Conta contaBaseOrig = criarConta(BigDecimal.valueOf(8));
        Conta contaBaseDest = criarConta(BigDecimal.ONE);
        Optional<Conta> contaInexistente = repository.findContaByNumeroConta(9988);
        assertTrue(contaInexistente.isEmpty());

        String response =
                mvc.perform(post(baseUri + "/9988")
                                .param("destino", String.valueOf(contaBaseDest.getNumeroConta()))
                                .param("valor", "3")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andReturn().getResponse().getErrorMessage();

        assertEquals("Recurso não encontrado.", response);
        assertEquals(BigDecimal.ONE, contaBaseDest.getSaldo());
        assertEquals(BigDecimal.valueOf(8), contaBaseOrig.getSaldo());
        //System.out.println(contaBaseOrig.getSaldo());
        //System.out.println(contaBaseDest.getSaldo());
    }

    @Test
    void testPixContaInvalidaDest() throws Exception {
        Conta contaBaseOrig = criarConta(BigDecimal.ZERO);
        Conta contaBaseDest = criarConta(BigDecimal.ONE);
        Optional<Conta> contaInexistente = repository.findContaByNumeroConta(9987);
        assertTrue(contaInexistente.isEmpty());

        String response =
                mvc.perform(post(baseUri + "/" + contaBaseOrig.getNumeroConta())
                                .param("destino", String.valueOf(9987))
                                .param("valor", "6")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andReturn().getResponse().getErrorMessage();

        assertEquals("Recurso não encontrado.", response);
        assertEquals(BigDecimal.ONE, contaBaseDest.getSaldo());
        assertEquals(BigDecimal.ZERO, contaBaseOrig.getSaldo());
        //System.out.println(contaBaseOrig.getSaldo());
        //System.out.println(contaBaseDest.getSaldo());
    }

    @Test
    void testPixContaInvalidaOrigValorNegativo() throws Exception {
        Conta contaBaseOrig = criarConta(BigDecimal.valueOf(17));
        Conta contaBaseDest = criarConta(BigDecimal.TEN);
        Optional<Conta> contaInexistente = repository.findContaByNumeroConta(9988);
        assertTrue(contaInexistente.isEmpty());

        String response =
                mvc.perform(post(baseUri + "/9988")
                                .param("destino", String.valueOf(contaBaseDest.getNumeroConta()))
                                .param("valor", "-3")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andReturn().getResponse().getErrorMessage();

        assertEquals("Recurso não encontrado.", response);
        assertEquals(BigDecimal.TEN, contaBaseDest.getSaldo());
        assertEquals(BigDecimal.valueOf(17), contaBaseOrig.getSaldo());
        //System.out.println(contaBaseOrig.getSaldo());
        //System.out.println(contaBaseDest.getSaldo());
    }

}