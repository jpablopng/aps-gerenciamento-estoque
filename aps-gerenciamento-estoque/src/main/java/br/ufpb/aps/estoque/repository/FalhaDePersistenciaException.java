package br.ufpb.aps.estoque.repository;

/** Erro técnico ao gravar dados. */
public class FalhaDePersistenciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FalhaDePersistenciaException(String mensagem) {
        super(mensagem);
    }
}
