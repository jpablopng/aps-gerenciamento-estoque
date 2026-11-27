package br.ufpb.aps.estoque.service;

import br.ufpb.aps.estoque.model.AcessoNegadoException;
import br.ufpb.aps.estoque.model.Perfil;
import br.ufpb.aps.estoque.model.Permissao;
import br.ufpb.aps.estoque.model.Quantidade;
import br.ufpb.aps.estoque.model.Reserva;
import br.ufpb.aps.estoque.model.SaldoEstoque;
import br.ufpb.aps.estoque.model.Usuario;
import br.ufpb.aps.estoque.repository.memoria.EstoqueEmMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ControleAcessoTest {

    private static final String P = "MON-24";

    private final Usuario operador = new Usuario("Carlos", Perfil.OPERADOR);
    private final Usuario gerente = new Usuario("Ana", Perfil.GERENTE);

    private EstoqueEmMemoria repositorio;
    private EstoqueService estoque;

    @BeforeEach
    void preparar() {
        repositorio = new EstoqueEmMemoria();
        estoque = new EstoqueService(repositorio, new ReposicaoAteNivelAlvo(2));
        estoque.cadastrarProduto(gerente, P, "Monitor", 3);
    }

    // Perfis

    @Test
    @DisplayName("Operador pode entrada, reserva e saída, e só isso")
    void permissoesDoOperador() {
        assertTrue(Perfil.OPERADOR.pode(Permissao.REGISTRAR_ENTRADA));
        assertTrue(Perfil.OPERADOR.pode(Permissao.RESERVAR));
        assertTrue(Perfil.OPERADOR.pode(Permissao.REGISTRAR_SAIDA));
        assertFalse(Perfil.OPERADOR.pode(Permissao.CADASTRAR_PRODUTO));
        assertFalse(Perfil.OPERADOR.pode(Permissao.TROCAR_POLITICA_REPOSICAO));
    }

    @Test
    @DisplayName("Gerente pode tudo")
    void permissoesDoGerente() {
        for (Permissao p : Permissao.values()) {
            assertTrue(Perfil.GERENTE.pode(p), "Gerente deveria poder " + p);
        }
    }

    // Operador: permitido

    @Test
    @DisplayName("Operador registra entrada, reserva, confirma, cancela e dá saída")
    void operadorFazOperacoesDoDiaADia() {
        estoque.registrarEntrada(operador, P, 10);
        Reserva confirmada = estoque.reservar(operador, P, 2);
        estoque.confirmarReserva(operador, confirmada.id());
        Reserva cancelada = estoque.reservar(operador, P, 1);
        estoque.cancelarReserva(operador, cancelada.id());
        estoque.registrarSaida(operador, P, 3);

        assertEquals(Quantidade.de(5), estoque.saldo(P).fisico());
        assertEquals(Quantidade.de(5), estoque.saldo(P).disponivel());
    }

    // Operador: acesso negado

    @Test
    @DisplayName("Operador não cadastra produto e nada é gravado")
    void operadorNaoCadastraProduto() {
        AcessoNegadoException e = assertThrows(AcessoNegadoException.class,
                () -> estoque.cadastrarProduto(operador, "TEC-01", "Teclado", 2));

        assertTrue(e.getMessage().contains("Operador"));
        assertTrue(repositorio.buscarProduto("TEC-01").isEmpty());
    }

    @Test
    @DisplayName("Operador não troca a política de reposição e a atual continua")
    void operadorNaoTrocaPolitica() {
        PoliticaReposicao antes = estoque.politicaReposicao();

        assertThrows(AcessoNegadoException.class,
                () -> estoque.trocarPoliticaReposicao(operador, new ReposicaoPorLoteFixo(12)));

        assertSame(antes, estoque.politicaReposicao());
    }

    @Test
    @DisplayName("A permissão é verificada antes de validar os dados")
    void permissaoAntesDaValidacao() {
        assertThrows(AcessoNegadoException.class,
                () -> estoque.cadastrarProduto(operador, "TEC-01", "Teclado", -1));
    }

    // Gerente

    @Test
    @DisplayName("Gerente cadastra produto e troca a política")
    void gerenteFazTudo() {
        estoque.cadastrarProduto(gerente, "TEC-01", "Teclado", 2);
        ReposicaoPorLoteFixo nova = new ReposicaoPorLoteFixo(12);
        estoque.trocarPoliticaReposicao(gerente, nova);

        assertTrue(repositorio.buscarProduto("TEC-01").isPresent());
        assertSame(nova, estoque.politicaReposicao());
    }

    @Test
    @DisplayName("Gerente também faz as operações do Operador")
    void gerenteFazOperacoesDoOperador() {
        estoque.registrarEntrada(gerente, P, 5);
        Reserva r = estoque.reservar(gerente, P, 2);
        estoque.confirmarReserva(gerente, r.id());
        estoque.registrarSaida(gerente, P, 1);

        assertEquals(Quantidade.de(2), estoque.saldo(P).fisico());
    }

    // Sem usuário

    @Test
    @DisplayName("Operação sem usuário é recusada e não altera o estoque")
    void semUsuario() {
        SaldoEstoque antes = estoque.saldo(P);

        assertThrows(AcessoNegadoException.class, () -> estoque.registrarEntrada(null, P, 5));

        assertEquals(antes, estoque.saldo(P));
        assertTrue(estoque.movimentacoes(P).isEmpty());
    }
}
