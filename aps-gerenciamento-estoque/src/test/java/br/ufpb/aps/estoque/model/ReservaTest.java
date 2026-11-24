package br.ufpb.aps.estoque.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    private final Reserva ativa = Reserva.nova(1, "P1", Quantidade.de(2), LocalDateTime.of(2026, 11, 23, 10, 0));

    @Test
    @DisplayName("Reserva nova começa ativa")
    void comecaAtiva() {
        assertEquals(StatusReserva.ATIVA, ativa.status());
    }

    @Test
    @DisplayName("Confirmar muda o status para confirmada")
    void confirmar() {
        assertEquals(StatusReserva.CONFIRMADA, ativa.confirmada().status());
    }

    @Test
    @DisplayName("Confirmar duas vezes é recusado")
    void confirmacaoDuplicada() {
        Reserva confirmada = ativa.confirmada();
        assertThrows(OperacaoJaConcluidaException.class, confirmada::confirmada);
    }

    @Test
    @DisplayName("Não dá para cancelar reserva já confirmada, nem confirmar reserva cancelada")
    void estadosFinais() {
        assertThrows(OperacaoJaConcluidaException.class, () -> ativa.confirmada().cancelada());
        assertThrows(OperacaoJaConcluidaException.class, () -> ativa.cancelada().confirmada());
    }

    @Test
    @DisplayName("Confirmar não altera a reserva original")
    void imutavel() {
        ativa.confirmada();
        assertEquals(StatusReserva.ATIVA, ativa.status());
    }
}
