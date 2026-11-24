package br.ufpb.aps.estoque.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Registro de uma operação no estoque, com o saldo logo depois dela. */
public record Movimentacao(
        long id,
        String produtoCodigo,
        TipoMovimentacao tipo,
        Quantidade quantidade,
        Long reservaId,
        LocalDateTime dataHora,
        Quantidade fisicoApos,
        Quantidade disponivelApos) {

    public Movimentacao {
        Objects.requireNonNull(produtoCodigo);
        Objects.requireNonNull(tipo);
        Objects.requireNonNull(quantidade);
        Objects.requireNonNull(dataHora);
        Objects.requireNonNull(fisicoApos);
        Objects.requireNonNull(disponivelApos);
    }

    public static Movimentacao de(long id, TipoMovimentacao tipo, Quantidade quantidade,
                                  Long reservaId, LocalDateTime dataHora, SaldoEstoque saldoApos) {
        return new Movimentacao(id, saldoApos.produtoCodigo(), tipo, quantidade, reservaId,
                dataHora, saldoApos.fisico(), saldoApos.disponivel());
    }
}
