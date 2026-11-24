package br.ufpb.aps.estoque.model;

/** Ciclo de vida da reserva: ATIVA vira CONFIRMADA ou CANCELADA, e daí não muda mais. */
public enum StatusReserva {
    ATIVA("ativa"),
    CONFIRMADA("confirmada"),
    CANCELADA("cancelada");

    private final String descricao;

    StatusReserva(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }
}
