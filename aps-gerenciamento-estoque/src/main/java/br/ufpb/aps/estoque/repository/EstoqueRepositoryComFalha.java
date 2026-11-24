package br.ufpb.aps.estoque.repository;

import br.ufpb.aps.estoque.model.Movimentacao;
import br.ufpb.aps.estoque.model.Produto;
import br.ufpb.aps.estoque.model.Reserva;
import br.ufpb.aps.estoque.model.SaldoEstoque;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Decorator usado na demonstração e nos testes: faz a próxima gravação de movimentação falhar. */
public class EstoqueRepositoryComFalha implements EstoqueRepository {

    private final EstoqueRepository real;
    private boolean falharNaProximaMovimentacao;

    public EstoqueRepositoryComFalha(EstoqueRepository real) {
        this.real = real;
    }

    public void falharNaProximaMovimentacao() {
        this.falharNaProximaMovimentacao = true;
    }

    @Override
    public Optional<Produto> buscarProduto(String codigo) {
        return real.buscarProduto(codigo);
    }

    @Override
    public Optional<SaldoEstoque> buscarSaldo(String codigo) {
        return real.buscarSaldo(codigo);
    }

    @Override
    public Optional<Reserva> buscarReserva(long id) {
        return real.buscarReserva(id);
    }

    @Override
    public List<Movimentacao> movimentacoesDo(String codigo) {
        return real.movimentacoesDo(codigo);
    }

    @Override
    public <T> T emTransacao(Function<Transacao, T> trabalho) {
        return real.emTransacao(tx -> trabalho.apply(new TransacaoQueFalha(tx)));
    }

    private final class TransacaoQueFalha implements Transacao {

        private final Transacao tx;

        private TransacaoQueFalha(Transacao tx) {
            this.tx = tx;
        }

        @Override
        public Optional<Produto> buscarProduto(String codigo) {
            return tx.buscarProduto(codigo);
        }

        @Override
        public Optional<SaldoEstoque> buscarSaldo(String codigo) {
            return tx.buscarSaldo(codigo);
        }

        @Override
        public Optional<Reserva> buscarReserva(long id) {
            return tx.buscarReserva(id);
        }

        @Override
        public void salvarProduto(Produto produto) {
            tx.salvarProduto(produto);
        }

        @Override
        public void salvarSaldo(SaldoEstoque saldo) {
            tx.salvarSaldo(saldo);
        }

        @Override
        public long proximoIdReserva() {
            return tx.proximoIdReserva();
        }

        @Override
        public void salvarReserva(Reserva reserva) {
            tx.salvarReserva(reserva);
        }

        @Override
        public long proximoIdMovimentacao() {
            return tx.proximoIdMovimentacao();
        }

        @Override
        public void registrarMovimentacao(Movimentacao movimentacao) {
            if (falharNaProximaMovimentacao) {
                falharNaProximaMovimentacao = false;
                throw new FalhaDePersistenciaException("Falha simulada ao gravar a movimentação.");
            }
            tx.registrarMovimentacao(movimentacao);
        }
    }
}
