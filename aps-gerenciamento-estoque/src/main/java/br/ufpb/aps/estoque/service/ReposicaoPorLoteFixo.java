package br.ufpb.aps.estoque.service;

import br.ufpb.aps.estoque.model.Produto;
import br.ufpb.aps.estoque.model.Quantidade;
import br.ufpb.aps.estoque.model.SaldoEstoque;

/** Compra em lotes fechados, na menor quantidade de lotes que passa do mínimo. Ex.: lote 12, mínimo 3, disponível 1: compra 12. */
public final class ReposicaoPorLoteFixo implements PoliticaReposicao {

    private final int tamanhoLote;

    public ReposicaoPorLoteFixo(int tamanhoLote) {
        if (tamanhoLote < 1) {
            throw new IllegalArgumentException("O tamanho do lote deve ser pelo menos 1.");
        }
        this.tamanhoLote = tamanhoLote;
    }

    @Override
    public Quantidade sugerirReposicao(Produto produto, SaldoEstoque saldo) {
        int minimo = produto.estoqueMinimo().valor();
        int disponivel = saldo.disponivel().valor();
        if (disponivel > minimo) {
            return Quantidade.ZERO;
        }
        int falta = minimo - disponivel + 1;
        int lotes = (falta + tamanhoLote - 1) / tamanhoLote;
        return Quantidade.de(Math.multiplyExact(lotes, tamanhoLote));
    }

    @Override
    public String descricao() {
        return "comprar em lotes de " + tamanhoLote;
    }
}
