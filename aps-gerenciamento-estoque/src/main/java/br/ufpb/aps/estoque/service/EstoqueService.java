package br.ufpb.aps.estoque.service;

import br.ufpb.aps.estoque.model.AcessoNegadoException;
import br.ufpb.aps.estoque.model.Movimentacao;
import br.ufpb.aps.estoque.model.Permissao;
import br.ufpb.aps.estoque.model.Produto;
import br.ufpb.aps.estoque.model.Quantidade;
import br.ufpb.aps.estoque.model.RegraNegocioException;
import br.ufpb.aps.estoque.model.Reserva;
import br.ufpb.aps.estoque.model.SaldoEstoque;
import br.ufpb.aps.estoque.model.TipoMovimentacao;
import br.ufpb.aps.estoque.model.Usuario;
import br.ufpb.aps.estoque.repository.EstoqueRepository;
import br.ufpb.aps.estoque.repository.Transacao;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Casos de uso do estoque. Antes de cada operação confere se o perfil do usuário permite;
 * depois altera saldo e movimentação numa única transação e confere o estoque mínimo.
 */
public class EstoqueService {

    private final EstoqueRepository repositorio;
    private final Clock relogio;
    private final List<AlertaEstoqueListener> ouvintes = new CopyOnWriteArrayList<>();
    private volatile PoliticaReposicao politicaReposicao;

    public EstoqueService(EstoqueRepository repositorio, PoliticaReposicao politicaReposicao, Clock relogio) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.politicaReposicao = Objects.requireNonNull(politicaReposicao);
        this.relogio = Objects.requireNonNull(relogio);
    }

    public EstoqueService(EstoqueRepository repositorio, PoliticaReposicao politicaReposicao) {
        this(repositorio, politicaReposicao, Clock.systemDefaultZone());
    }

    public void adicionarOuvinte(AlertaEstoqueListener ouvinte) {
        ouvintes.add(Objects.requireNonNull(ouvinte));
    }

    /** Só o Gerente troca a política de reposição. */
    public void trocarPoliticaReposicao(Usuario usuario, PoliticaReposicao nova) {
        exigirPermissao(usuario, Permissao.TROCAR_POLITICA_REPOSICAO);
        this.politicaReposicao = Objects.requireNonNull(nova);
    }

    public PoliticaReposicao politicaReposicao() {
        return politicaReposicao;
    }

    /** Só o Gerente cadastra produto. */
    public Produto cadastrarProduto(Usuario usuario, String codigo, String nome, int estoqueMinimo) {
        exigirPermissao(usuario, Permissao.CADASTRAR_PRODUTO);
        if (estoqueMinimo < 0) {
            throw new RegraNegocioException("O estoque mínimo não pode ser negativo. Informe zero ou mais.");
        }
        Produto produto = new Produto(codigo, nome, Quantidade.de(estoqueMinimo));
        return repositorio.emTransacao(tx -> {
            if (tx.buscarProduto(produto.codigo()).isPresent()) {
                throw new RegraNegocioException("Já existe um produto com o código " + produto.codigo()
                        + ". Use outro código.");
            }
            tx.salvarProduto(produto);
            tx.salvarSaldo(SaldoEstoque.vazio(produto.codigo()));
            return produto;
        });
    }

    /** Entrada de mercadoria: aumenta o estoque físico e o disponível. */
    public Movimentacao registrarEntrada(Usuario usuario, String codigoProduto, int quantidade) {
        exigirPermissao(usuario, Permissao.REGISTRAR_ENTRADA);
        Quantidade q = quantidadePositiva(quantidade);
        Movimentacao mov = repositorio.emTransacao(tx -> {
            SaldoEstoque novo = saldoDe(tx, codigoProduto).comEntrada(q);
            tx.salvarSaldo(novo);
            return registrar(tx, TipoMovimentacao.ENTRADA, q, null, novo);
        });
        verificarEstoqueMinimo(codigoProduto);
        return mov;
    }

    /** Reserva: separa parte do disponível. Recusa se pedir mais que o disponível. */
    public Reserva reservar(Usuario usuario, String codigoProduto, int quantidade) {
        exigirPermissao(usuario, Permissao.RESERVAR);
        Quantidade q = quantidadePositiva(quantidade);
        Reserva reserva = repositorio.emTransacao(tx -> {
            SaldoEstoque novo = saldoDe(tx, codigoProduto).comReserva(q);
            Reserva nova = Reserva.nova(tx.proximoIdReserva(), codigoProduto, q, agora());
            tx.salvarSaldo(novo);
            tx.salvarReserva(nova);
            registrar(tx, TipoMovimentacao.RESERVA, q, nova.id(), novo);
            return nova;
        });
        verificarEstoqueMinimo(codigoProduto);
        return reserva;
    }

    /**
     * Confirma a reserva: a quantidade sai do estoque físico (é uma saída). Uma reserva só pode
     * ser confirmada uma vez; a segunda tentativa é recusada sem alterar nada.
     */
    public Movimentacao confirmarReserva(Usuario usuario, long reservaId) {
        exigirPermissao(usuario, Permissao.REGISTRAR_SAIDA);
        Movimentacao mov = repositorio.emTransacao(tx -> {
            Reserva confirmada = reservaDe(tx, reservaId).confirmada();
            SaldoEstoque novo = saldoDe(tx, confirmada.produtoCodigo())
                    .comSaidaDaReserva(confirmada.quantidade());
            tx.salvarSaldo(novo);
            tx.salvarReserva(confirmada);
            return registrar(tx, TipoMovimentacao.SAIDA, confirmada.quantidade(), reservaId, novo);
        });
        verificarEstoqueMinimo(mov.produtoCodigo());
        return mov;
    }

    /** Cancela a reserva e devolve a quantidade ao disponível. */
    public Movimentacao cancelarReserva(Usuario usuario, long reservaId) {
        exigirPermissao(usuario, Permissao.RESERVAR);
        Movimentacao mov = repositorio.emTransacao(tx -> {
            Reserva cancelada = reservaDe(tx, reservaId).cancelada();
            SaldoEstoque novo = saldoDe(tx, cancelada.produtoCodigo())
                    .semReserva(cancelada.quantidade());
            tx.salvarSaldo(novo);
            tx.salvarReserva(cancelada);
            return registrar(tx, TipoMovimentacao.CANCELAMENTO_RESERVA, cancelada.quantidade(), reservaId, novo);
        });
        verificarEstoqueMinimo(mov.produtoCodigo());
        return mov;
    }

    /** Saída sem reserva prévia. Recusa se pedir mais que o disponível. */
    public Movimentacao registrarSaida(Usuario usuario, String codigoProduto, int quantidade) {
        exigirPermissao(usuario, Permissao.REGISTRAR_SAIDA);
        Quantidade q = quantidadePositiva(quantidade);
        Movimentacao mov = repositorio.emTransacao(tx -> {
            SaldoEstoque novo = saldoDe(tx, codigoProduto).comSaidaDireta(q);
            tx.salvarSaldo(novo);
            return registrar(tx, TipoMovimentacao.SAIDA, q, null, novo);
        });
        verificarEstoqueMinimo(codigoProduto);
        return mov;
    }

    public SaldoEstoque saldo(String codigoProduto) {
        return repositorio.buscarSaldo(codigoProduto)
                .orElseThrow(() -> produtoNaoEncontrado(codigoProduto));
    }

    public List<Movimentacao> movimentacoes(String codigoProduto) {
        return repositorio.movimentacoesDo(codigoProduto);
    }

    /** Recusa a operação antes de qualquer validação ou gravação se o perfil não permitir. */
    private static void exigirPermissao(Usuario usuario, Permissao permissao) {
        if (usuario == null) {
            throw new AcessoNegadoException("Nenhum usuário identificado. Entre no sistema para "
                    + permissao.descricao() + ".");
        }
        if (!usuario.pode(permissao)) {
            throw new AcessoNegadoException("Acesso negado: o perfil " + usuario.perfil().rotulo()
                    + " não pode " + permissao.descricao() + ". Peça a um Gerente.");
        }
    }

    private Movimentacao registrar(Transacao tx, TipoMovimentacao tipo, Quantidade quantidade,
                                   Long reservaId, SaldoEstoque saldoApos) {
        Movimentacao mov = Movimentacao.de(tx.proximoIdMovimentacao(), tipo, quantidade, reservaId, agora(), saldoApos);
        tx.registrarMovimentacao(mov);
        return mov;
    }

    private void verificarEstoqueMinimo(String codigoProduto) {
        Produto produto = repositorio.buscarProduto(codigoProduto).orElseThrow();
        SaldoEstoque saldo = saldo(codigoProduto);
        if (produto.estoqueMinimo().menorQue(saldo.disponivel())) {
            return;
        }
        PoliticaReposicao politica = politicaReposicao;
        AlertaEstoque alerta = new AlertaEstoque(produto, saldo,
                politica.sugerirReposicao(produto, saldo), politica.descricao());
        ouvintes.forEach(o -> o.estoqueMinimoAtingido(alerta));
    }

    private SaldoEstoque saldoDe(Transacao tx, String codigoProduto) {
        return tx.buscarSaldo(codigoProduto).orElseThrow(() -> produtoNaoEncontrado(codigoProduto));
    }

    private Reserva reservaDe(Transacao tx, long reservaId) {
        return tx.buscarReserva(reservaId).orElseThrow(() -> new RegraNegocioException(
                "Reserva " + reservaId + " não encontrada. Confira o número da reserva."));
    }

    private static RegraNegocioException produtoNaoEncontrado(String codigo) {
        return new RegraNegocioException("Produto " + codigo + " não encontrado. Cadastre o produto antes.");
    }

    private static Quantidade quantidadePositiva(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("A quantidade deve ser um número inteiro maior que zero.");
        }
        return Quantidade.de(quantidade);
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(relogio);
    }
}
