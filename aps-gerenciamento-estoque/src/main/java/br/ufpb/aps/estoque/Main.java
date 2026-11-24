package br.ufpb.aps.estoque;

import br.ufpb.aps.estoque.controller.AlertaConsole;
import br.ufpb.aps.estoque.controller.DemonstracaoEstoque;
import br.ufpb.aps.estoque.repository.memoria.EstoqueEmMemoria;
import br.ufpb.aps.estoque.service.EstoqueService;
import br.ufpb.aps.estoque.service.ReposicaoAteNivelAlvo;

/**
 * Ponto de entrada da aplicação.
 *
 * Regra do projeto: o Main só cria e conecta os componentes
 * (repositórios, serviços e controllers). Nenhuma regra de negócio fica aqui.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Gerenciamento de Estoque | APS 2026.2 | Grupo 01");

        EstoqueService estoque = new EstoqueService(new EstoqueEmMemoria(), new ReposicaoAteNivelAlvo(2));
        estoque.adicionarOuvinte(new AlertaConsole(System.out));

        new DemonstracaoEstoque(estoque, System.out).executar();
    }
}
