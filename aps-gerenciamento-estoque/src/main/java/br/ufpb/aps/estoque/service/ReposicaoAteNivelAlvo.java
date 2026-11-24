package br.ufpb.aps.estoque.service;

import br.ufpb.aps.estoque.model.Produto;
import br.ufpb.aps.estoque.model.Quantidade;
import br.ufpb.aps.estoque.model.SaldoEstoque;

/** Repõe até o disponível chegar a um múltiplo do estoque mínimo. Ex.: mínimo 3, multiplicador 2, disponível 2: compra 4. */
public final class ReposicaoAteNivelAlvo implements PoliticaReposicao {

    private final int multiplicador;

    public ReposicaoAteNivelAlvo(int multiplicador) {
        if (multiplicador < 1) {
            throw new IllegalArgumentException("O multiplicador do nível alvo deve ser pelo menos 1.");
        }
        this.multiplicador = multiplicador;
    }

    @Override
    public Quantidade sugerirReposicao(Produto produto, SaldoEstoque saldo) {
        int alvo = Math.multiplyExact(produto.estoqueMinimo().valor(), multiplicador);
        int disponivel = saldo.disponivel().valor();
        return disponivel >= alvo ? Quantidade.ZERO : Quantidade.de(alvo - disponivel);
    }

    @Override
    public String descricao() {
        return "repor até " + multiplicador + "x o estoque mínimo";
    }
}
