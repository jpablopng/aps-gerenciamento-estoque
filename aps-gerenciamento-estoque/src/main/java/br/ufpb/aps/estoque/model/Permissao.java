package br.ufpb.aps.estoque.model;

/** Ações do estoque que dependem do perfil do usuário. */
public enum Permissao {

    REGISTRAR_ENTRADA("registrar entrada de mercadoria"),
    RESERVAR("reservar ou cancelar reserva"),
    REGISTRAR_SAIDA("registrar saída"),
    CADASTRAR_PRODUTO("cadastrar produto"),
    TROCAR_POLITICA_REPOSICAO("trocar a política de reposição");

    private final String descricao;

    Permissao(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }
}