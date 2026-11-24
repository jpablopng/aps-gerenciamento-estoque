package br.ufpb.aps.estoque.repository.memoria;

import br.ufpb.aps.estoque.model.Produto;
import br.ufpb.aps.estoque.model.Quantidade;
import br.ufpb.aps.estoque.model.SaldoEstoque;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstoqueEmMemoriaTest {

    private final EstoqueEmMemoria repositorio = new EstoqueEmMemoria();

    @Test
    @DisplayName("Transação concluída grava tudo")
    void confirmaAoTerminar() {
        repositorio.emTransacao(tx -> {
            tx.salvarProduto(new Produto("P1", "Mouse", Quantidade.de(1)));
            tx.salvarSaldo(SaldoEstoque.vazio("P1"));
            return null;
        });
        assertTrue(repositorio.buscarProduto("P1").isPresent());
        assertTrue(repositorio.buscarSaldo("P1").isPresent());
    }

    @Test
    @DisplayName("Erro no meio da transação descarta tudo o que ela gravou")
    void descartaAoFalhar() {
        assertThrows(IllegalStateException.class, () -> repositorio.emTransacao(tx -> {
            tx.salvarProduto(new Produto("P1", "Mouse", Quantidade.de(1)));
            tx.salvarSaldo(SaldoEstoque.vazio("P1"));
            throw new IllegalStateException("erro no meio");
        }));
        assertTrue(repositorio.buscarProduto("P1").isEmpty());
        assertTrue(repositorio.buscarSaldo("P1").isEmpty());
    }

    @Test
    @DisplayName("Dentro da transação, a leitura já enxerga o que foi gravado nela")
    void leituraDentroDaTransacao() {
        boolean enxergou = repositorio.emTransacao(tx -> {
            tx.salvarSaldo(SaldoEstoque.vazio("P1"));
            return tx.buscarSaldo("P1").isPresent();
        });
        assertTrue(enxergou);
    }

    @Test
    @DisplayName("Números usados numa transação desfeita não deixam buraco na sequência")
    void idsDescartadosComATransacao() {
        assertThrows(IllegalStateException.class, () -> repositorio.emTransacao(tx -> {
            tx.proximoIdReserva();
            throw new IllegalStateException("erro");
        }));
        long id = repositorio.emTransacao(tx -> tx.proximoIdReserva());
        assertEquals(1L, id);
    }
}
