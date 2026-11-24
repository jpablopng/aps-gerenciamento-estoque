package br.ufpb.aps.estoque.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SaldoEstoqueTest {

    private static Quantidade q(int valor) {
        return Quantidade.de(valor);
    }

    private static SaldoEstoque saldo(int fisico, int reservado) {
        return SaldoEstoque.de("P1", q(fisico), q(reservado));
    }

    @Test
    @DisplayName("Disponível é o físico menos o reservado")
    void disponivelEhFisicoMenosReservado() {
        assertEquals(q(6), saldo(10, 4).disponivel());
    }

    @Test
    @DisplayName("Entrada aumenta físico e disponível")
    void entradaAumentaFisico() {
        SaldoEstoque novo = saldo(10, 4).comEntrada(q(5));
        assertEquals(q(15), novo.fisico());
        assertEquals(q(11), novo.disponivel());
    }

    @Test
    @DisplayName("Reserva reduz o disponível e não mexe no físico")
    void reservaNaoMexeNoFisico() {
        SaldoEstoque novo = saldo(10, 0).comReserva(q(4));
        assertEquals(q(10), novo.fisico());
        assertEquals(q(4), novo.reservado());
        assertEquals(q(6), novo.disponivel());
    }

    @Test
    @DisplayName("Fronteira: reservar exatamente o disponível é aceito")
    void reservarTodoODisponivel() {
        assertEquals(Quantidade.ZERO, saldo(10, 4).comReserva(q(6)).disponivel());
    }

    @Test
    @DisplayName("Fronteira: reservar um a mais que o disponível é recusado")
    void reservarAcimaDoDisponivelFalha() {
        assertThrows(EstoqueInsuficienteException.class, () -> saldo(10, 4).comReserva(q(7)));
    }

    @Test
    @DisplayName("Saída de reserva tira do físico e do reservado; o disponível não muda")
    void saidaDaReserva() {
        SaldoEstoque novo = saldo(10, 4).comSaidaDaReserva(q(4));
        assertEquals(q(6), novo.fisico());
        assertEquals(Quantidade.ZERO, novo.reservado());
        assertEquals(q(6), novo.disponivel());
    }

    @Test
    @DisplayName("Saída direta não pode usar quantidade que está reservada")
    void saidaDiretaRespeitaReservado() {
        assertThrows(EstoqueInsuficienteException.class, () -> saldo(10, 4).comSaidaDireta(q(7)));
        assertEquals(q(4), saldo(10, 4).comSaidaDireta(q(6)).fisico());
    }

    @Test
    @DisplayName("Cancelar reserva devolve ao disponível")
    void cancelarReservaDevolve() {
        assertEquals(q(10), saldo(10, 4).semReserva(q(4)).disponivel());
    }

    @Test
    @DisplayName("Reservado nunca pode passar do físico")
    void reservadoNaoPassaDoFisico() {
        assertThrows(IllegalStateException.class, () -> saldo(3, 4));
    }

    @Test
    @DisplayName("Operações devolvem um novo saldo e não alteram o original")
    void imutavel() {
        SaldoEstoque original = saldo(10, 0);
        original.comReserva(q(4));
        assertEquals(q(10), original.disponivel());
    }
}
