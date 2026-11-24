package br.ufpb.aps.estoque.repository;

import br.ufpb.aps.estoque.model.Movimentacao;
import br.ufpb.aps.estoque.model.Produto;
import br.ufpb.aps.estoque.model.Reserva;
import br.ufpb.aps.estoque.model.SaldoEstoque;

import java.util.Optional;

/** Leituras e escritas de uma transação. As leituras já enxergam o que foi gravado nela. */
public interface Transacao {

    Optional<Produto> buscarProduto(String codigo);

    Optional<SaldoEstoque> buscarSaldo(String codigo);

    Optional<Reserva> buscarReserva(long id);

    void salvarProduto(Produto produto);

    void salvarSaldo(SaldoEstoque saldo);

    long proximoIdReserva();

    void salvarReserva(Reserva reserva);

    long proximoIdMovimentacao();

    void registrarMovimentacao(Movimentacao movimentacao);
}
