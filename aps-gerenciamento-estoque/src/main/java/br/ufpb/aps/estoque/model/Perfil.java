package br.ufpb.aps.estoque.model;

import java.util.EnumSet;
import java.util.Set;

/** Perfis de acesso. O Gerente pode tudo o que o Operador pode, e mais o cadastro e a política de reposição. */
public enum Perfil {

    OPERADOR("Operador"),
    GERENTE("Gerente");

    private static final Set<Permissao> DO_OPERADOR = EnumSet.of(
            Permissao.REGISTRAR_ENTRADA,
            Permissao.RESERVAR,
            Permissao.REGISTRAR_SAIDA);

    private static final Set<Permissao> EXCLUSIVAS_DO_GERENTE = EnumSet.of(
            Permissao.CADASTRAR_PRODUTO,
            Permissao.TROCAR_POLITICA_REPOSICAO);

    private final String rotulo;

    Perfil(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    public boolean pode(Permissao permissao) {
        return switch (this) {
            case OPERADOR -> DO_OPERADOR.contains(permissao);
            case GERENTE -> DO_OPERADOR.contains(permissao) || EXCLUSIVAS_DO_GERENTE.contains(permissao);
        };
    }
}
