package br.ufpb.aps.estoque.model;

/**
 * Objeto de valor que representa uma quantidade de itens em estoque.
 *
 * Invariante: nunca é negativa. Operações que deixariam o valor negativo
 * são rejeitadas, o que ajuda a impedir saída ou reserva acima do disponível.
 */
public final class Quantidade {

    public static final Quantidade ZERO = new Quantidade(0);

    private final int valor;

    private Quantidade(int valor) {
        this.valor = valor;
    }

    public static Quantidade de(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa: " + valor);
        }
        return new Quantidade(valor);
    }

    public int valor() {
        return valor;
    }

    public Quantidade somar(Quantidade outra) {
        return new Quantidade(Math.addExact(this.valor, outra.valor));
    }

    public Quantidade subtrair(Quantidade outra) {
        if (outra.valor > this.valor) {
            throw new IllegalStateException(
                    "Quantidade insuficiente: disponível " + valor + ", solicitado " + outra.valor);
        }
        return new Quantidade(this.valor - outra.valor);
    }

    public boolean menorQue(Quantidade outra) {
        return this.valor < outra.valor;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Quantidade q && q.valor == valor;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(valor);
    }

    @Override
    public String toString() {
        return Integer.toString(valor);
    }
}
