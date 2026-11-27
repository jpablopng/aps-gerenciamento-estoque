package br.ufpb.aps.estoque.model;

/** O perfil do usuário não permite a operação pedida. */
public class AcessoNegadoException extends RegraNegocioException {

    private static final long serialVersionUID = 1L;

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}