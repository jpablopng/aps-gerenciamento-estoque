package br.ufpb.aps.estoque.repository;

import br.ufpb.aps.estoque.model.Movimentacao;
import br.ufpb.aps.estoque.model.Produto;
import br.ufpb.aps.estoque.model.Reserva;
import br.ufpb.aps.estoque.model.SaldoEstoque;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Contrato de persistência do estoque. Toda escrita passa por emTransacao: grava tudo ou nada. */
public interface EstoqueRepository {

    Optional<Produto> buscarProduto(String codigo);

    Optional<SaldoEstoque> buscarSaldo(String codigo);

    Optional<Reserva> buscarReserva(long id);

    List<Movimentacao> movimentacoesDo(String codigo);

    /**
     * Executa o trabalho numa transação. Se o trabalho terminar normalmente, as
     * gravações são confirmadas; se lançar exceção, são todas descartadas e a
     * exceção é repassada.
     */
    <T> T emTransacao(Function<Transacao, T> trabalho);
}
