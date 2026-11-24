package br.ufpb.aps.estoque.model;

import java.util.Objects;

/** Produto controlado no estoque. Identificado pelo código. */
public final class Produto {

    private final String codigo;
    private final String nome;
    private final Quantidade estoqueMinimo;

    public Produto(String codigo, String nome, Quantidade estoqueMinimo) {
        this.codigo = exigirTexto(codigo, "código");
        this.nome = exigirTexto(nome, "nome");
        this.estoqueMinimo = Objects.requireNonNull(estoqueMinimo, "estoque mínimo");
    }

    private static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("O campo " + campo + " do produto é obrigatório.");
        }
        return valor.trim();
    }

    public String codigo() {
        return codigo;
    }

    public String nome() {
        return nome;
    }

    public Quantidade estoqueMinimo() {
        return estoqueMinimo;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Produto p && p.codigo.equals(codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }

    @Override
    public String toString() {
        return codigo + " (" + nome + ")";
    }
}
