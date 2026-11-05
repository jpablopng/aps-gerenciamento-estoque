package br.ufpb.aps.estoque.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuantidadeTest {

    @Test
    @DisplayName("Não permite criar quantidade negativa")
    void rejeitaNegativa() {
        assertThrows(IllegalArgumentException.class, () -> Quantidade.de(-1));
    }

    @Test
    @DisplayName("Fronteira: subtrair exatamente o disponível resulta em zero")
    void subtrairTudoDaZero() {
        assertEquals(Quantidade.ZERO, Quantidade.de(5).subtrair(Quantidade.de(5)));
    }

    @Test
    @DisplayName("Fronteira: subtrair um a mais que o disponível é rejeitado")
    void subtrairAcimaDoDisponivelFalha() {
        assertThrows(IllegalStateException.class,
                () -> Quantidade.de(5).subtrair(Quantidade.de(6)));
    }
}
