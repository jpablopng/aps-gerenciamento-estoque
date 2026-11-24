package br.ufpb.aps.estoque.repository.memoria;

import br.ufpb.aps.estoque.model.Movimentacao;
import br.ufpb.aps.estoque.model.Produto;
import br.ufpb.aps.estoque.model.Reserva;
import br.ufpb.aps.estoque.model.SaldoEstoque;
import br.ufpb.aps.estoque.repository.EstoqueRepository;
import br.ufpb.aps.estoque.repository.Transacao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/** Repositório em memória: as gravações ficam num rascunho e só valem se a operação terminar sem erro. */
public class EstoqueEmMemoria implements EstoqueRepository {

    private final Map<String, Produto> produtos = new HashMap<>();
    private final Map<String, SaldoEstoque> saldos = new HashMap<>();
    private final Map<Long, Reserva> reservas = new HashMap<>();
    private final List<Movimentacao> movimentacoes = new ArrayList<>();
    private long ultimoIdReserva;
    private long ultimoIdMovimentacao;

    @Override
    public synchronized Optional<Produto> buscarProduto(String codigo) {
        return Optional.ofNullable(produtos.get(codigo));
    }

    @Override
    public synchronized Optional<SaldoEstoque> buscarSaldo(String codigo) {
        return Optional.ofNullable(saldos.get(codigo));
    }

    @Override
    public synchronized Optional<Reserva> buscarReserva(long id) {
        return Optional.ofNullable(reservas.get(id));
    }

    @Override
    public synchronized List<Movimentacao> movimentacoesDo(String codigo) {
        return movimentacoes.stream()
                .filter(m -> m.produtoCodigo().equals(codigo))
                .toList();
    }

    @Override
    public synchronized <T> T emTransacao(Function<Transacao, T> trabalho) {
        Rascunho rascunho = new Rascunho();
        T resultado = trabalho.apply(rascunho);
        rascunho.confirmar();
        return resultado;
    }

    private final class Rascunho implements Transacao {

        private final Map<String, Produto> novosProdutos = new HashMap<>();
        private final Map<String, SaldoEstoque> novosSaldos = new HashMap<>();
        private final Map<Long, Reserva> novasReservas = new HashMap<>();
        private final List<Movimentacao> novasMovimentacoes = new ArrayList<>();
        private long idReserva = ultimoIdReserva;
        private long idMovimentacao = ultimoIdMovimentacao;

        @Override
        public Optional<Produto> buscarProduto(String codigo) {
            return Optional.ofNullable(novosProdutos.getOrDefault(codigo, produtos.get(codigo)));
        }

        @Override
        public Optional<SaldoEstoque> buscarSaldo(String codigo) {
            return Optional.ofNullable(novosSaldos.getOrDefault(codigo, saldos.get(codigo)));
        }

        @Override
        public Optional<Reserva> buscarReserva(long id) {
            return Optional.ofNullable(novasReservas.getOrDefault(id, reservas.get(id)));
        }

        @Override
        public void salvarProduto(Produto produto) {
            novosProdutos.put(produto.codigo(), produto);
        }

        @Override
        public void salvarSaldo(SaldoEstoque saldo) {
            novosSaldos.put(saldo.produtoCodigo(), saldo);
        }

        @Override
        public long proximoIdReserva() {
            return ++idReserva;
        }

        @Override
        public void salvarReserva(Reserva reserva) {
            novasReservas.put(reserva.id(), reserva);
        }

        @Override
        public long proximoIdMovimentacao() {
            return ++idMovimentacao;
        }

        @Override
        public void registrarMovimentacao(Movimentacao movimentacao) {
            novasMovimentacoes.add(movimentacao);
        }

        /** Copia tudo de uma vez para os dados de verdade. Nenhum passo aqui pode falhar. */
        private void confirmar() {
            produtos.putAll(novosProdutos);
            saldos.putAll(novosSaldos);
            reservas.putAll(novasReservas);
            movimentacoes.addAll(novasMovimentacoes);
            ultimoIdReserva = idReserva;
            ultimoIdMovimentacao = idMovimentacao;
        }
    }
}
