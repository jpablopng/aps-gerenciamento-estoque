package br.ufpb.aps.estoque.model;

/** Quem está operando o sistema e com qual perfil. */
public record Usuario(String nome, Perfil perfil) {

    public Usuario {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome do usuário é obrigatório.");
        }
        if (perfil == null) {
            throw new RegraNegocioException("O perfil do usuário é obrigatório.");
        }
        nome = nome.trim();
    }

    public boolean pode(Permissao permissao) {
        return perfil.pode(permissao);
    }

    @Override
    public String toString() {
        return nome + " (" + perfil.rotulo() + ")";
    }
}
