package br.ufpb.aps.estoque.service;

import br.ufpb.aps.estoque.model.EstoqueInsuficienteException;
import br.ufpb.aps.estoque.model.Movimentacao;
import br.ufpb.aps.estoque.model.OperacaoJaConcluidaException;
import br.ufpb.aps.estoque.model.Quantidade;
import br.ufpb.aps.estoque.model.RegraNegocioException;
import br.ufpb.aps.estoque.model.Reserva;
import br.ufpb.aps.estoque.model.SaldoEstoque;
import br.ufpb.aps.estoque.model.StatusReserva;
import br.ufpb.aps.estoque.model.TipoMovimentacao;
import br.ufpb.aps.estoque.repository.EstoqueRepositoryComFalha;
import br.ufpb.aps.estoque.repository.FalhaDePersistenciaException;
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

    private EstoqueRepositoryComFalha repositorio;
    private EstoqueService estoque;
    private List<AlertaEstoque> alertas;

    @BeforeEach
    void preparar() {
        repositorio = new EstoqueRepositoryComFalha(new EstoqueEmMemoria());
        Clock relogio = Clock.fixed(Instant.parse("2026-11-23T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        estoque = new EstoqueService(repositorio, new ReposicaoAteNivelAlvo(2), relogio);
        alertas = new ArrayList<>();
        estoque.adicionarOuvinte(alertas::add);
        estoque.cadastrarProduto(P, "Monitor", 3);
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
        Movimentacao mov = estoque.registrarEntrada(P, 10);
        assertEquals(q(10), saldo().fisico());
        assertEquals(q(10), saldo().disponivel());
        assertEquals(TipoMovimentacao.ENTRADA, mov.tipo());
        assertEquals(q(10), mov.fisicoApos());
        assertEquals(1, estoque.movimentacoes(P).size());
    }

    @Test
    @DisplayName("Quantidade zero ou negativa é recusada")
    void quantidadeInvalida() {
        assertThrows(RegraNegocioException.class, () -> estoque.registrarEntrada(P, 0));
        assertThrows(RegraNegocioException.class, () -> estoque.reservar(P, -1));
        assertEquals(0, estoque.movimentacoes(P).size());
    }

    @Test
    @DisplayName("Produto inexistente é recusado")
    void produtoInexistente() {
        assertThrows(RegraNegocioException.class, () -> estoque.registrarEntrada("NAO-EXISTE", 1));
    }

    @Test
    @DisplayName("Código de produto duplicado é recusado")
    void produtoDuplicado() {
        assertThrows(RegraNegocioException.class, () -> estoque.cadastrarProduto(P, "Outro", 1));
    }

    // Reserva e saída

    @Test
    @DisplayName("Reserva reduz o disponível sem mexer no físico")
    void reserva() {
        estoque.registrarEntrada(P, 10);
        Reserva r = estoque.reservar(P, 4);
        assertEquals(StatusReserva.ATIVA, r.status());
        assertEquals(q(10), saldo().fisico());
        assertEquals(q(6), saldo().disponivel());
    }

    @Test
    @DisplayName("Reserva acima do disponível é recusada e não altera nada")
    void reservaAcimaDoDisponivel() {
        estoque.registrarEntrada(P, 10);
        estoque.reservar(P, 4);
        SaldoEstoque antes = saldo();
        int movimentacoesAntes = estoque.movimentacoes(P).size();

        assertThrows(EstoqueInsuficienteException.class, () -> estoque.reservar(P, 7));

        assertEquals(antes, saldo());
        assertEquals(movimentacoesAntes, estoque.movimentacoes(P).size());
    }

    @Test
    @DisplayName("Saída direta acima do disponível é recusada, contando o que está reservado")
    void saidaAcimaDoDisponivel() {
        estoque.registrarEntrada(P, 10);
        estoque.reservar(P, 4);
        assertThrows(EstoqueInsuficienteException.class, () -> estoque.registrarSaida(P, 7));
        assertEquals(q(10), saldo().fisico());
    }

    @Test
    @DisplayName("Confirmar a reserva tira do físico e libera o reservado")
    void confirmarReserva() {
        estoque.registrarEntrada(P, 10);
        Reserva r = estoque.reservar(P, 4);

        Movimentacao mov = estoque.confirmarReserva(r.id());

        assertEquals(TipoMovimentacao.SAIDA, mov.tipo());
        assertEquals(Long.valueOf(r.id()), mov.reservaId());
        assertEquals(q(6), saldo().fisico());
        assertEquals(Quantidade.ZERO, saldo().reservado());
        assertEquals(StatusReserva.CONFIRMADA, repositorio.buscarReserva(r.id()).orElseThrow().status());
    }

    @Test
    @DisplayName("Confirmação duplicada é recusada e não baixa o estoque de novo")
    void confirmacaoDuplicada() {
        estoque.registrarEntrada(P, 10);
        Reserva r = estoque.reservar(P, 4);
        estoque.confirmarReserva(r.id());
        SaldoEstoque depoisDaPrimeira = saldo();
        int movimentacoes = estoque.movimentacoes(P).size();

        assertThrows(OperacaoJaConcluidaException.class, () -> estoque.confirmarReserva(r.id()));

        assertEquals(depoisDaPrimeira, saldo());
        assertEquals(movimentacoes, estoque.movimentacoes(P).size());
    }

    @Test
    @DisplayName("Cancelar a reserva devolve ao disponível; depois disso não pode confirmar")
    void cancelarReserva() {
        estoque.registrarEntrada(P, 10);
        Reserva r = estoque.reservar(P, 4);
        estoque.cancelarReserva(r.id());
        assertEquals(q(10), saldo().disponivel());
        assertThrows(OperacaoJaConcluidaException.class, () -> estoque.confirmarReserva(r.id()));
        assertEquals(q(10), saldo().fisico());
    }

    @Test
    @DisplayName("Reserva inexistente é recusada")
    void reservaInexistente() {
        assertThrows(RegraNegocioException.class, () -> estoque.confirmarReserva(99));
    }

    // Alerta e política

    @Test
    @DisplayName("Alerta quando o disponível chega ao estoque mínimo, com sugestão da política")
    void alertaAoAtingirMinimo() {
        estoque.registrarEntrada(P, 10);
        estoque.registrarSaida(P, 7);

        assertEquals(1, alertas.size());
        AlertaEstoque alerta = alertas.get(0);
        assertEquals(q(3), alerta.saldo().disponivel());
        assertEquals(q(3), alerta.reposicaoSugerida());
    }

    @Test
    @DisplayName("Não alerta enquanto o disponível está acima do mínimo")
    void naoAlertaAcimaDoMinimo() {
        estoque.registrarEntrada(P, 10);
        estoque.registrarSaida(P, 6);
        assertTrue(alertas.isEmpty());
    }

    @Test
    @DisplayName("Reserva também conta para o alerta, porque reduz o disponível")
    void reservaDisparaAlerta() {
        estoque.registrarEntrada(P, 5);
        estoque.reservar(P, 3);
        assertEquals(1, alertas.size());
    }

    @Test
    @DisplayName("Trocar a política muda a sugestão sem mexer no serviço")
    void trocarPolitica() {
        estoque.registrarEntrada(P, 10);
        estoque.trocarPoliticaReposicao(new ReposicaoPorLoteFixo(12));
        estoque.registrarSaida(P, 9);

        AlertaEstoque alerta = alertas.get(alertas.size() - 1);
        assertEquals(q(12), alerta.reposicaoSugerida());
        assertEquals("comprar em lotes de 12", alerta.criterio());
    }

    // Atomicidade

    @Test
    @DisplayName("Falha ao gravar a movimentação desfaz a entrada inteira")
    void entradaComFalhaNaoDeixaRegistroParcial() {
        estoque.registrarEntrada(P, 10);
        SaldoEstoque antes = saldo();
        List<Movimentacao> historicoAntes = estoque.movimentacoes(P);

        repositorio.falharNaProximaMovimentacao();
        assertThrows(FalhaDePersistenciaException.class, () -> estoque.registrarEntrada(P, 5));

        assertEquals(antes, saldo());
        assertEquals(historicoAntes, estoque.movimentacoes(P));
    }

    @Test
    @DisplayName("Falha na confirmação mantém a reserva ativa e o estoque intacto; dá para confirmar de novo")
    void confirmacaoComFalhaNaoDeixaRegistroParcial() {
        estoque.registrarEntrada(P, 10);
        Reserva r = estoque.reservar(P, 4);
        SaldoEstoque antes = saldo();

        repositorio.falharNaProximaMovimentacao();
        assertThrows(FalhaDePersistenciaException.class, () -> estoque.confirmarReserva(r.id()));

        assertEquals(antes, saldo());
        assertEquals(StatusReserva.ATIVA, repositorio.buscarReserva(r.id()).orElseThrow().status());

        estoque.confirmarReserva(r.id());
        assertEquals(q(6), saldo().fisico());
    }

    @Test
    @DisplayName("Falha na reserva não deixa reserva criada nem disponível reduzido")
    void reservaComFalhaNaoDeixaRegistroParcial() {
        estoque.registrarEntrada(P, 10);

        repositorio.falharNaProximaMovimentacao();
        assertThrows(FalhaDePersistenciaException.class, () -> estoque.reservar(P, 4));

        assertEquals(q(10), saldo().disponivel());
        assertTrue(repositorio.buscarReserva(1).isEmpty());
    }

    @Test
    @DisplayName("Histórico bate com o saldo: o último registro mostra o saldo atual")
    void historicoBateComSaldo() {
        estoque.registrarEntrada(P, 10);
        Reserva r = estoque.reservar(P, 4);
        estoque.confirmarReserva(r.id());
        estoque.registrarSaida(P, 2);

        List<Movimentacao> historico = estoque.movimentacoes(P);
        Movimentacao ultima = historico.get(historico.size() - 1);
        assertEquals(4, historico.size());
        assertEquals(saldo().fisico(), ultima.fisicoApos());
        assertEquals(saldo().disponivel(), ultima.disponivelApos());
    }
}
