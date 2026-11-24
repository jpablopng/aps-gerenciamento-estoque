package br.ufpb.aps.estoque.service;

import br.ufpb.aps.estoque.model.Produto;
import br.ufpb.aps.estoque.model.Quantidade;
import br.ufpb.aps.estoque.model.SaldoEstoque;

/** Critério de reposição substituível (Strategy): decide quanto comprar quando o produto chega ao mínimo. */
public interface PoliticaReposicao {

    /** Quantidade sugerida para compra. {@link Quantidade#ZERO} quando não precisa repor. */
    Quantidade sugerirReposicao(Produto produto, SaldoEstoque saldo);

    /** Descrição curta do critério, mostrada no alerta. */
    String descricao();
}
