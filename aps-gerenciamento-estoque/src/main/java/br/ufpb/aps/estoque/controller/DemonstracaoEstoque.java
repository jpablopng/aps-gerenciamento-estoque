package br.ufpb.aps.estoque.controller;

import br.ufpb.aps.estoque.model.Movimentacao;
import br.ufpb.aps.estoque.model.Perfil;
import br.ufpb.aps.estoque.model.RegraNegocioException;
import br.ufpb.aps.estoque.model.Reserva;
import br.ufpb.aps.estoque.model.Usuario;
import br.ufpb.aps.estoque.service.EstoqueService;
import br.ufpb.aps.estoque.service.ReposicaoPorLoteFixo;

import java.io.PrintStream;

/** Roteiro de demonstração do desafio do tema, executado pelo Main. */
public class DemonstracaoEstoque {

    private static final String PRODUTO = "MON-24";

    private final EstoqueService estoque;
    private final PrintStream saida;
    private final Usuario gerente = new Usuario("Gerente", Perfil.GERENTE);
    private final Usuario operador = new Usuario("Operador de Caixa", Perfil.OPERADOR);

    public DemonstracaoEstoque(EstoqueService estoque, PrintStream saida) {
        this.estoque = estoque;
        this.saida = saida;
    }

    public void executar() {
        passo("0. Operador tenta cadastrar produto (só o Gerente pode)");
        tentar(() -> estoque.cadastrarProduto(operador, "TEC-01", "Teclado", 2));

        passo("1. Gerente cadastra o produto (estoque mínimo 3)");
        estoque.cadastrarProduto(gerente, PRODUTO, "Monitor 24 polegadas", 3);
        mostrarSaldo();

        passo("2. Operador dá entrada de 10 unidades");
        estoque.registrarEntrada(operador, PRODUTO, 10);
        mostrarSaldo();

        passo("3. Operador reserva 4 unidades: o físico não muda, o disponível cai");
        Reserva reserva = estoque.reservar(operador, PRODUTO, 4);
        saida.println("   " + reserva);
        mostrarSaldo();

        passo("4. Tentativa de reservar 7 (só há 6 disponíveis)");
        tentar(() -> estoque.reservar(operador, PRODUTO, 7));
        mostrarSaldo();

        passo("5. Confirmação da reserva " + reserva.id() + ": as 4 unidades saem do físico");
        estoque.confirmarReserva(operador, reserva.id());
        mostrarSaldo();

        passo("6. Segunda confirmação da mesma reserva");
        tentar(() -> estoque.confirmarReserva(operador, reserva.id()));
        mostrarSaldo();

        passo("7. Saída direta de 7 (só há 6 disponíveis)");
        tentar(() -> estoque.registrarSaida(operador, PRODUTO, 7));

        passo("8. Saída direta de 4: o disponível cai para 2 e dispara o alerta");
        estoque.registrarSaida(operador, PRODUTO, 4);
        mostrarSaldo();

        passo("9. Operador tenta trocar a política de reposição (só o Gerente pode)");
        tentar(() -> estoque.trocarPoliticaReposicao(operador, new ReposicaoPorLoteFixo(12)));

        passo("10. Gerente troca a política para lotes de 12 e o Operador reserva 1");
        estoque.trocarPoliticaReposicao(gerente, new ReposicaoPorLoteFixo(12));
        Reserva outra = estoque.reservar(operador, PRODUTO, 1);
        mostrarSaldo();
        estoque.cancelarReserva(operador, outra.id());
        saida.println("   Reserva " + outra.id() + " cancelada, unidade devolvida ao disponível.");
        mostrarSaldo();

        passo("Histórico de movimentações de " + PRODUTO);
        for (Movimentacao m : estoque.movimentacoes(PRODUTO)) {
            saida.printf("   #%d %-20s %3s un.%s  -> físico %s, disponível %s%n",
                    m.id(), m.tipo(), m.quantidade(),
                    m.reservaId() == null ? "            " : " (reserva " + m.reservaId() + ")",
                    m.fisicoApos(), m.disponivelApos());
        }
    }

    private void passo(String titulo) {
        saida.println();
        saida.println(titulo);
    }

    private void mostrarSaldo() {
        saida.println("   Saldo: " + estoque.saldo(PRODUTO));
    }

    private void tentar(Runnable operacao) {
        try {
            operacao.run();
            saida.println("   (operação aceita)");
        } catch (RegraNegocioException e) {
            saida.println("   Recusado: " + e.getMessage());
        }
    }
}
