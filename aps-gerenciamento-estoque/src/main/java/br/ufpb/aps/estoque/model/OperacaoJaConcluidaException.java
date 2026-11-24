package br.ufpb.aps.estoque.model;

/** Tentativa de confirmar ou cancelar uma reserva que já foi confirmada ou cancelada. */
public class OperacaoJaConcluidaException extends RegraNegocioException {

    private static final long serialVersionUID = 1L;

    public OperacaoJaConcluidaException(String mensagem) {
        super(mensagem);
    }
}
