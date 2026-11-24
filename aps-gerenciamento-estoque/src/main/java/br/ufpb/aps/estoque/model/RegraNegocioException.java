package br.ufpb.aps.estoque.model;

/** Violação de uma regra de negócio, com mensagem para o usuário. */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
