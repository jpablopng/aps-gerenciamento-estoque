package br.ufpb.aps.estoque.model;

/** Tentativa de reservar ou retirar mais do que o estoque disponível. */
public class EstoqueInsuficienteException extends RegraNegocioException {

    private static final long serialVersionUID = 1L;

    public EstoqueInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
