package br.ufpb.aps.estoque.controller;

import br.ufpb.aps.estoque.model.Movimentacao;
import br.ufpb.aps.estoque.model.RegraNegocioException;
import br.ufpb.aps.estoque.model.Reserva;
import br.ufpb.aps.estoque.repository.EstoqueRepositoryComFalha;
import br.ufpb.aps.estoque.repository.FalhaDePersistenciaException;
import br.ufpb.aps.estoque.service.EstoqueService;
import br.ufpb.aps.estoque.service.ReposicaoPorLoteFixo;

import java.io.PrintStream;

/** Roteiro de demonstração do desafio do tema, executado pelo Main. */
public class DemonstracaoEstoque {

    private static final String PRODUTO = "MON-24";

    private final EstoqueService estoque;
    private final EstoqueRepositoryComFalha repositorio;
    private final PrintStream saida;

    public DemonstracaoEstoque(EstoqueService estoque, EstoqueRepositoryComFalha repositorio, PrintStream saida) {
        this.estoque = estoque;
        this.repositorio = repositorio;
        this.saida = saida;
    }

    public void executar() {
        passo("1. Cadastro do produto (estoque mínimo 3)");
        estoque.cadastrarProduto(PRODUTO, "Monitor 24 polegadas", 3);
        mostrarSaldo();

        passo("2. Entrada de 10 unidades");
        estoque.registrarEntrada(PRODUTO, 10);
        mostrarSaldo();

        passo("3. Reserva de 4 unidades: o físico não muda, o disponível cai");
        Reserva reserva = estoque.reservar(PRODUTO, 4);
        saida.println("   " + reserva);
        mostrarSaldo();

        passo("4. Tentativa de reservar 7 (só há 6 disponíveis)");
        tentar(() -> estoque.reservar(PRODUTO, 7));
        mostrarSaldo();

        passo("5. Confirmação da reserva " + reserva.id() + ": as 4 unidades saem do físico");
        estoque.confirmarReserva(reserva.id());
        mostrarSaldo();

        passo("6. Segunda confirmação da mesma reserva");
        tentar(() -> estoque.confirmarReserva(reserva.id()));
        mostrarSaldo();

        passo("7. Saída direta de 7 (só há 6 disponíveis)");
        tentar(() -> estoque.registrarSaida(PRODUTO, 7));

        passo("8. Saída direta de 4: o disponível cai para 2 e dispara o alerta");
        estoque.registrarSaida(PRODUTO, 4);
        mostrarSaldo();

        passo("9. Troca da política de reposição para lotes de 12 e nova reserva de 1");
        estoque.trocarPoliticaReposicao(new ReposicaoPorLoteFixo(12));
        Reserva outra = estoque.reservar(PRODUTO, 1);
        mostrarSaldo();
        estoque.cancelarReserva(outra.id());
        saida.println("   Reserva " + outra.id() + " cancelada, unidade devolvida ao disponível.");
        mostrarSaldo();

        passo("10. Entrada de 5 com falha simulada ao gravar a movimentação");
        int movimentacoesAntes = estoque.movimentacoes(PRODUTO).size();
        String saldoAntes = estoque.saldo(PRODUTO).toString();
        repositorio.falharNaProximaMovimentacao();
        try {
            estoque.registrarEntrada(PRODUTO, 5);
        } catch (FalhaDePersistenciaException e) {
            saida.println("   Erro: " + e.getMessage());
        }
        int movimentacoesDepois = estoque.movimentacoes(PRODUTO).size();
        saida.println("   Saldo antes:  " + saldoAntes);
        saida.println("   Saldo depois: " + estoque.saldo(PRODUTO));
        saida.println("   Movimentações antes: " + movimentacoesAntes + " | depois: " + movimentacoesDepois);
        saida.println("   Nada foi gravado pela metade: o saldo e o histórico continuam iguais.");

        passo("11. A mesma entrada, agora sem falha");
        estoque.registrarEntrada(PRODUTO, 5);
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
