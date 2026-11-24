package br.ufpb.aps.estoque.service;

import br.ufpb.aps.estoque.model.Produto;
import br.ufpb.aps.estoque.model.Quantidade;
import br.ufpb.aps.estoque.model.SaldoEstoque;

/** Aviso de produto no estoque mínimo, com a sugestão de compra da política em uso. */
public record AlertaEstoque(
        Produto produto,
        SaldoEstoque saldo,
        Quantidade reposicaoSugerida,
        String criterio) {
}
