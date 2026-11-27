package br.ufpb.aps.estoque.service;

import br.ufpb.aps.estoque.model.EstoqueInsuficienteException;
import br.ufpb.aps.estoque.model.Movimentacao;
import br.ufpb.aps.estoque.model.OperacaoJaConcluidaException;
import br.ufpb.aps.estoque.model.Perfil;
import br.ufpb.aps.estoque.model.Quantidade;
import br.ufpb.aps.estoque.model.RegraNegocioException;
import br.ufpb.aps.estoque.model.Reserva;
import br.ufpb.aps.estoque.model.SaldoEstoque;
import br.ufpb.aps.estoque.model.StatusReserva;
import br.ufpb.aps.estoque.model.TipoMovimentacao;
import br.ufpb.aps.estoque.model.Usuario;
import br.ufpb.aps.estoque.repository.memoria.EstoqueEmMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EstoqueServiceTest {

    private static final String P = "MON-24";

    private final Usuario gerente = new Usuario("Gerente", Perfil.GERENTE);

    private EstoqueEmMemoria repositorio;
    private RelogioQueFalha relogio;
    private EstoqueService estoque;
    private List<AlertaEstoque> alertas;

    /**
     * Relógio de teste que pode ser mandado falhar. O serviço consulta o relógio
     * DEPOIS de salvar o saldo na transação e ANTES de registrar a movimentação,
     * então a falha cai exatamente no meio da operação.
     */
    private static final class RelogioQueFalha extends Clock {
        private final Clock real = Clock.fixed(Instant.parse("2026-11-23T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        private boolean falhar;

        @Override
        public Instant instant() {
            if (falhar) {
                throw new IllegalStateException("Falha simulada no meio da operação.");
            }
            return real.instant();
        }

        @Override
        public ZoneId getZone() {
            return real.getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return real.withZone(zone);
        }
    }

    @BeforeEach
    void preparar() {
        repositorio = new EstoqueEmMemoria();
        relogio = new RelogioQueFalha();
        estoque = new EstoqueService(repositorio, new ReposicaoAteNivelAlvo(2), relogio);
        alertas = new ArrayList<>();
        estoque.adicionarOuvinte(alertas::add);
        estoque.cadastrarProduto(gerente, P, "Monitor", 3);
    }

    private static Quantidade q(int valor) {
        return Quantidade.de(valor);
    }

    private SaldoEstoque saldo() {
        return estoque.saldo(P);
    }

    // Entrada

    @Test
    @DisplayName("Entrada soma ao físico e ao disponível e registra a movimentação")
    void entrada() {
        Movimentacao mov = estoque.registrarEntrada(gerente, P, 10);
        assertEquals(q(10), saldo().fisico());
        assertEquals(q(10), saldo().disponivel());
        assertEquals(TipoMovimentacao.ENTRADA, mov.tipo());
        assertEquals(q(10), mov.fisicoApos());
        assertEquals(1, estoque.movimentacoes(P).size());
    }

    @Test
    @DisplayName("Quantidade zero ou negativa é recusada")
    void quantidadeInvalida() {
        assertThrows(RegraNegocioException.class, () -> estoque.registrarEntrada(gerente, P, 0));
        assertThrows(RegraNegocioException.class, () -> estoque.reservar(gerente, P, -1));
        assertEquals(0, estoque.movimentacoes(P).size());
    }

    @Test
    @DisplayName("Produto inexistente é recusado")
    void produtoInexistente() {
        assertThrows(RegraNegocioException.class, () -> estoque.registrarEntrada(gerente, "NAO-EXISTE", 1));
    }

    @Test
    @DisplayName("Código de produto duplicado é recusado")
    void produtoDuplicado() {
        assertThrows(RegraNegocioException.class, () -> estoque.cadastrarProduto(gerente, P, "Outro", 1));
    }

    // Reserva e saída

    @Test
    @DisplayName("Reserva reduz o disponível sem mexer no físico")
    void reserva() {
        estoque.registrarEntrada(gerente, P, 10);
        Reserva r = estoque.reservar(gerente, P, 4);
        assertEquals(StatusReserva.ATIVA, r.status());
        assertEquals(q(10), saldo().fisico());
        assertEquals(q(6), saldo().disponivel());
    }

    @Test
    @DisplayName("Reserva acima do disponível é recusada e não altera nada")
    void reservaAcimaDoDisponivel() {
        estoque.registrarEntrada(gerente, P, 10);
        estoque.reservar(gerente, P, 4);
        SaldoEstoque antes = saldo();
        int movimentacoesAntes = estoque.movimentacoes(P).size();

        assertThrows(EstoqueInsuficienteException.class, () -> estoque.reservar(gerente, P, 7));

        assertEquals(antes, saldo());
        assertEquals(movimentacoesAntes, estoque.movimentacoes(P).size());
    }

    @Test
    @DisplayName("Saída direta acima do disponível é recusada, contando o que está reservado")
    void saidaAcimaDoDisponivel() {
        estoque.registrarEntrada(gerente, P, 10);
        estoque.reservar(gerente, P, 4);
        assertThrows(EstoqueInsuficienteException.class, () -> estoque.registrarSaida(gerente, P, 7));
        assertEquals(q(10), saldo().fisico());
    }

    @Test
    @DisplayName("Confirmar a reserva tira do físico e libera o reservado")
    void confirmarReserva() {
        estoque.registrarEntrada(gerente, P, 10);
        Reserva r = estoque.reservar(gerente, P, 4);

        Movimentacao mov = estoque.confirmarReserva(gerente, r.id());

        assertEquals(TipoMovimentacao.SAIDA, mov.tipo());
        assertEquals(Long.valueOf(r.id()), mov.reservaId());
        assertEquals(q(6), saldo().fisico());
        assertEquals(Quantidade.ZERO, saldo().reservado());
        assertEquals(StatusReserva.CONFIRMADA, repositorio.buscarReserva(r.id()).orElseThrow().status());
    }

    @Test
    @DisplayName("Confirmação duplicada é recusada e não baixa o estoque de novo")
    void confirmacaoDuplicada() {
        estoque.registrarEntrada(gerente, P, 10);
        Reserva r = estoque.reservar(gerente, P, 4);
        estoque.confirmarReserva(gerente, r.id());
        SaldoEstoque depoisDaPrimeira = saldo();
        int movimentacoes = estoque.movimentacoes(P).size();

        assertThrows(OperacaoJaConcluidaException.class, () -> estoque.confirmarReserva(gerente, r.id()));

        assertEquals(depoisDaPrimeira, saldo());
        assertEquals(movimentacoes, estoque.movimentacoes(P).size());
    }

    @Test
    @DisplayName("Cancelar a reserva devolve ao disponível; depois disso não pode confirmar")
    void cancelarReserva() {
        estoque.registrarEntrada(gerente, P, 10);
        Reserva r = estoque.reservar(gerente, P, 4);
        estoque.cancelarReserva(gerente, r.id());
        assertEquals(q(10), saldo().disponivel());
        assertThrows(OperacaoJaConcluidaException.class, () -> estoque.confirmarReserva(gerente, r.id()));
        assertEquals(q(10), saldo().fisico());
    }

    @Test
    @DisplayName("Reserva inexistente é recusada")
    void reservaInexistente() {
        assertThrows(RegraNegocioException.class, () -> estoque.confirmarReserva(gerente, 99));
    }

    // Alerta e política

    @Test
    @DisplayName("Alerta quando o disponível chega ao estoque mínimo, com sugestão da política")
    void alertaAoAtingirMinimo() {
        estoque.registrarEntrada(gerente, P, 10);
        estoque.registrarSaida(gerente, P, 7);

        assertEquals(1, alertas.size());
        AlertaEstoque alerta = alertas.get(0);
        assertEquals(q(3), alerta.saldo().disponivel());
        assertEquals(q(3), alerta.reposicaoSugerida());
    }

    @Test
    @DisplayName("Não alerta enquanto o disponível está acima do mínimo")
    void naoAlertaAcimaDoMinimo() {
        estoque.registrarEntrada(gerente, P, 10);
        estoque.registrarSaida(gerente, P, 6);
        assertTrue(alertas.isEmpty());
    }

    @Test
    @DisplayName("Reserva também conta para o alerta, porque reduz o disponível")
    void reservaDisparaAlerta() {
        estoque.registrarEntrada(gerente, P, 5);
        estoque.reservar(gerente, P, 3);
        assertEquals(1, alertas.size());
    }

    @Test
    @DisplayName("Trocar a política muda a sugestão sem mexer no serviço")
    void trocarPolitica() {
        estoque.registrarEntrada(gerente, P, 10);
        estoque.trocarPoliticaReposicao(gerente, new ReposicaoPorLoteFixo(12));
        estoque.registrarSaida(gerente, P, 9);

        AlertaEstoque alerta = alertas.get(alertas.size() - 1);
        assertEquals(q(12), alerta.reposicaoSugerida());
        assertEquals("comprar em lotes de 12", alerta.criterio());
    }

    // Atomicidade

    @Test
    @DisplayName("Falha no meio da entrada desfaz tudo (saldo e movimentação)")
    void entradaComFalhaNaoDeixaRegistroParcial() {
        estoque.registrarEntrada(gerente, P, 10);
        SaldoEstoque antes = saldo();
        List<Movimentacao> historicoAntes = estoque.movimentacoes(P);

        relogio.falhar = true;
        assertThrows(IllegalStateException.class, () -> estoque.registrarEntrada(gerente, P, 5));

        assertEquals(antes, saldo());
        assertEquals(historicoAntes, estoque.movimentacoes(P));
    }

    @Test
    @DisplayName("Falha na confirmação mantém a reserva ativa e o estoque intacto; dá para confirmar de novo")
    void confirmacaoComFalhaNaoDeixaRegistroParcial() {
        estoque.registrarEntrada(gerente, P, 10);
        Reserva r = estoque.reservar(gerente, P, 4);
        SaldoEstoque antes = saldo();

        relogio.falhar = true;
        assertThrows(IllegalStateException.class, () -> estoque.confirmarReserva(gerente, r.id()));

        assertEquals(antes, saldo());
        assertEquals(StatusReserva.ATIVA, repositorio.buscarReserva(r.id()).orElseThrow().status());

        relogio.falhar = false;
        estoque.confirmarReserva(gerente, r.id());
        assertEquals(q(6), saldo().fisico());
    }

    @Test
    @DisplayName("Histórico bate com o saldo: o último registro mostra o saldo atual")
    void historicoBateComSaldo() {
        estoque.registrarEntrada(gerente, P, 10);
        Reserva r = estoque.reservar(gerente, P, 4);
        estoque.confirmarReserva(gerente, r.id());
        estoque.registrarSaida(gerente, P, 2);

        List<Movimentacao> historico = estoque.movimentacoes(P);
        Movimentacao ultima = historico.get(historico.size() - 1);
        assertEquals(4, historico.size());
        assertEquals(saldo().fisico(), ultima.fisicoApos());
        assertEquals(saldo().disponivel(), ultima.disponivelApos());
    }
}
