# ADR 002: Persistência em SQLite

**Status:** aceita

## Contexto

Produtos, entradas, vendas e usuários precisam sobreviver ao fechamento do programa.
O sistema roda em um único computador no caixa, com no mínimo Dual Core e 4 GB de RAM (RNF01, RNF03),
e quem usa não é técnico, então não pode depender de instalar e manter um servidor de banco.
Na entrevista o cliente relatou que o estoque registrado não bate com o real. Por isso a venda precisa
gravar a venda e baixar o estoque juntos: ou tudo acontece, ou nada (RN04, RN08).

## Opções avaliadas

1. **Arquivos JSON ou CSV:** simples, mas sem transação. Se o programa cair no meio da venda, a venda pode ficar gravada sem a baixa do estoque, que é justamente o problema atual do cliente.
2. **MySQL ou PostgreSQL:** robustos, mas exigem servidor instalado e configurado na loja e pesam na máquina.
3. **SQLite via JDBC:** banco em um único arquivo, sem servidor, leve, com transações e SQL.

## Decisão

Opção 3, com o driver `org.xerial:sqlite-jdbc`. O banco fica em `dados/runtime/estoque.db`
(ignorado pelo Git) e é criado na primeira execução a partir de `src/main/resources/schema.sql`.

* Cada repositório tem uma interface em `repository` e uma implementação em `repository.sqlite`.
* A confirmação da venda roda numa transação: grava a venda e os itens, baixa o estoque de cada produto e só então faz o commit. Se qualquer item falhar, nada é gravado.
* Restrições também no banco: `UNIQUE(nome, marca)` em produto (RN02) e `CHECK(quantidade >= 0)` (RN03), como segunda barreira além do domínio.
* Os testes de serviço usam implementações em memória das mesmas interfaces, sem tocar no arquivo.

## Consequências

* Nada para instalar além do Java; o banco é um arquivo que dá para copiar como backup.
* A transação garante que venda e estoque não fiquem divergentes.
* SQLite não lida bem com muitos acessos simultâneos; aceitável porque a loja tem um caixa só.
* Se um dia precisar de outro banco, basta uma nova implementação das interfaces de `repository`.

## Evidência no código ou nos testes

* A implementar: `repository/sqlite/ConexaoSQLite.java`, `repository/sqlite/SqliteVendaRepository.java` e `src/main/resources/schema.sql`.
* Teste previsto: `SqliteVendaRepositoryTest.falhaNoMeioDaVendaNaoBaixaEstoque` (rollback quando um item falha).
