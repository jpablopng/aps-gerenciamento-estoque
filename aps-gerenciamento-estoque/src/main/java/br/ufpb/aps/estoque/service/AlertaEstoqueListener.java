package br.ufpb.aps.estoque.service;

/** Quem quer ser avisado quando um produto chega ao estoque mínimo (Observer). */
public interface AlertaEstoqueListener {

    void estoqueMinimoAtingido(AlertaEstoque alerta);
}
