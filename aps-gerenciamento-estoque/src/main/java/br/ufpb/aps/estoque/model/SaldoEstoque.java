package br.ufpb.aps.estoque.model;

import java.util.Objects;

/** Saldo de um produto: físico, reservado e disponível (físico menos reservado). Imutável. */
public final class SaldoEstoque {

    private final String produtoCodigo;
    private final Quantidade fisico;
    private final Quantidade reservado;

    private SaldoEstoque(String produtoCodigo, Quantidade fisico, Quantidade reservado) {
        this.produtoCodigo = Objects.requireNonNull(produtoCodigo);
        this.fisico = Objects.requireNonNull(fisico);
        this.reservado = Objects.requireNonNull(reservado);
        if (fisico.menorQue(reservado)) {
            throw new IllegalStateException("Reservado (" + reservado + ") maior que o físico (" + fisico + ")");
        }
    }

    public static SaldoEstoque vazio(String produtoCodigo) {
        return new SaldoEstoque(produtoCodigo, Quantidade.ZERO, Quantidade.ZERO);
    }

    public static SaldoEstoque de(String produtoCodigo, Quantidade fisico, Quantidade reservado) {
        return new SaldoEstoque(produtoCodigo, fisico, reservado);
    }

    public String produtoCodigo() {
        return produtoCodigo;
    }

    public Quantidade fisico() {
        return fisico;
    }

    public Quantidade reservado() {
        return reservado;
    }

    public Quantidade disponivel() {
        return fisico.subtrair(reservado);
    }

    /** Entrada de mercadoria: aumenta o físico. */
    public SaldoEstoque comEntrada(Quantidade quantidade) {
        return new SaldoEstoque(produtoCodigo, fisico.somar(quantidade), reservado);
    }

    /** Reserva: separa parte do disponível. O físico não muda. */
    public SaldoEstoque comReserva(Quantidade quantidade) {
        exigirDisponivel(quantidade, "reservar");
        return new SaldoEstoque(produtoCodigo, fisico, reservado.somar(quantidade));
    }

    /** Cancelamento de reserva: devolve a quantidade ao disponível. */
    public SaldoEstoque semReserva(Quantidade quantidade) {
        return new SaldoEstoque(produtoCodigo, fisico, reservado.subtrair(quantidade));
    }

    /** Saída de uma reserva confirmada: sai do físico e deixa de estar reservada. */
    public SaldoEstoque comSaidaDaReserva(Quantidade quantidade) {
        return new SaldoEstoque(produtoCodigo, fisico.subtrair(quantidade), reservado.subtrair(quantidade));
    }

    /** Saída sem reserva prévia: só pode usar o que está disponível. */
    public SaldoEstoque comSaidaDireta(Quantidade quantidade) {
        exigirDisponivel(quantidade, "retirar");
        return new SaldoEstoque(produtoCodigo, fisico.subtrair(quantidade), reservado);
    }

    private void exigirDisponivel(Quantidade quantidade, String acao) {
        Quantidade disponivel = disponivel();
        if (disponivel.menorQue(quantidade)) {
            throw new EstoqueInsuficienteException(String.format(
                    "Não é possível %s %d unidade(s) de %s: só há %d disponível(is) "
                            + "(físico %d, reservado %d). Informe uma quantidade de até %d.",
                    acao, quantidade.valor(), produtoCodigo, disponivel.valor(),
                    fisico.valor(), reservado.valor(), disponivel.valor()));
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SaldoEstoque s
                && s.produtoCodigo.equals(produtoCodigo)
                && s.fisico.equals(fisico)
                && s.reservado.equals(reservado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(produtoCodigo, fisico, reservado);
    }

    @Override
    public String toString() {
        return "físico " + fisico + " | reservado " + reservado + " | disponível " + disponivel();
    }
}
