package br.ufpb.aps.estoque.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Reserva de uma quantidade de produto. Só uma reserva ATIVA pode ser confirmada ou cancelada. Imutável. */
public final class Reserva {

    private final long id;
    private final String produtoCodigo;
    private final Quantidade quantidade;
    private final StatusReserva status;
    private final LocalDateTime criadaEm;

    private Reserva(long id, String produtoCodigo, Quantidade quantidade,
                    StatusReserva status, LocalDateTime criadaEm) {
        this.id = id;
        this.produtoCodigo = Objects.requireNonNull(produtoCodigo);
        this.quantidade = Objects.requireNonNull(quantidade);
        this.status = Objects.requireNonNull(status);
        this.criadaEm = Objects.requireNonNull(criadaEm);
    }

    public static Reserva nova(long id, String produtoCodigo, Quantidade quantidade, LocalDateTime criadaEm) {
        return new Reserva(id, produtoCodigo, quantidade, StatusReserva.ATIVA, criadaEm);
    }

    public Reserva confirmada() {
        exigirAtiva("confirmar");
        return new Reserva(id, produtoCodigo, quantidade, StatusReserva.CONFIRMADA, criadaEm);
    }

    public Reserva cancelada() {
        exigirAtiva("cancelar");
        return new Reserva(id, produtoCodigo, quantidade, StatusReserva.CANCELADA, criadaEm);
    }

    private void exigirAtiva(String acao) {
        if (status != StatusReserva.ATIVA) {
            throw new OperacaoJaConcluidaException(String.format(
                    "Não é possível %s a reserva %d: ela já está %s. Nenhuma alteração foi feita no estoque.",
                    acao, id, status.descricao()));
        }
    }

    public long id() {
        return id;
    }

    public String produtoCodigo() {
        return produtoCodigo;
    }

    public Quantidade quantidade() {
        return quantidade;
    }

    public StatusReserva status() {
        return status;
    }

    public LocalDateTime criadaEm() {
        return criadaEm;
    }

    @Override
    public String toString() {
        return "Reserva " + id + " de " + quantidade + " un. de " + produtoCodigo + " (" + status.descricao() + ")";
    }
}
