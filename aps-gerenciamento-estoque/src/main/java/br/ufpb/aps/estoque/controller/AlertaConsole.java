package br.ufpb.aps.estoque.controller;

import br.ufpb.aps.estoque.service.AlertaEstoque;
import br.ufpb.aps.estoque.service.AlertaEstoqueListener;

import java.io.PrintStream;

/** Mostra no console os alertas de estoque mínimo (ouvinte do Observer). */
public class AlertaConsole implements AlertaEstoqueListener {

    private final PrintStream saida;

    public AlertaConsole(PrintStream saida) {
        this.saida = saida;
    }

    @Override
    public void estoqueMinimoAtingido(AlertaEstoque alerta) {
        saida.printf("   [ALERTA] %s chegou ao estoque mínimo: disponível %s, mínimo %s. "
                        + "Sugestão (%s): comprar %s.%n",
                alerta.produto(), alerta.saldo().disponivel(), alerta.produto().estoqueMinimo(),
                alerta.criterio(), alerta.reposicaoSugerida());
    }
}
