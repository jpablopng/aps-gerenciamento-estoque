package br.ufpb.aps.estoque.service;

import br.ufpb.aps.estoque.model.Produto;
import br.ufpb.aps.estoque.model.Quantidade;
import br.ufpb.aps.estoque.model.SaldoEstoque;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PoliticaReposicaoTest {

    private final Produto produto = new Produto("P1", "Teclado", Quantidade.de(3));

    private static SaldoEstoque disponivel(int valor) {
        return SaldoEstoque.de("P1", Quantidade.de(valor), Quantidade.ZERO);
    }

    @Test
    @DisplayName("Nível alvo: repõe até o múltiplo do mínimo")
    void nivelAlvo() {
        PoliticaReposicao politica = new ReposicaoAteNivelAlvo(2);
        assertEquals(Quantidade.de(4), politica.sugerirReposicao(produto, disponivel(2)));
        assertEquals(Quantidade.de(6), politica.sugerirReposicao(produto, disponivel(0)));
    }

    @Test
    @DisplayName("Nível alvo: não sugere compra se já está no alvo")
    void nivelAlvoAtingido() {
        assertEquals(Quantidade.ZERO, new ReposicaoAteNivelAlvo(2).sugerirReposicao(produto, disponivel(6)));
    }

    @Test
    @DisplayName("Lote fixo: compra a menor quantidade de lotes que passa do mínimo")
    void loteFixo() {
        assertEquals(Quantidade.de(12), new ReposicaoPorLoteFixo(12).sugerirReposicao(produto, disponivel(1)));
        assertEquals(Quantidade.de(4), new ReposicaoPorLoteFixo(2).sugerirReposicao(produto, disponivel(0)));
    }

    @Test
    @DisplayName("Lote fixo: não sugere compra acima do mínimo")
    void loteFixoAcimaDoMinimo() {
        assertEquals(Quantidade.ZERO, new ReposicaoPorLoteFixo(12).sugerirReposicao(produto, disponivel(4)));
    }

    @Test
    @DisplayName("Parâmetros inválidos são recusados")
    void parametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new ReposicaoAteNivelAlvo(0));
        assertThrows(IllegalArgumentException.class, () -> new ReposicaoPorLoteFixo(0));
    }
}
