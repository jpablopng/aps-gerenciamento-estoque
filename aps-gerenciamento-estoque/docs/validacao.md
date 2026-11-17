# Relatório de validação

**Situação:** em preparação. As seções abaixo já trazem o plano; os resultados entram conforme o código for implementado.

## 1. Testes executados

Comando: `mvn test`

| Classe de teste | Testes | Resultado | Data |
|---|---|---|---|
| `QuantidadeTest` | 3 | A registrar | |
| `ProdutoServiceTest` | Previstos: 2 | Pendente | |
| `VendaServiceTest` | Previstos: 4 | Pendente | |
| `EstoqueServiceTest` | Previstos: 3 | Pendente | |
| `FaturamentoServiceTest` | Previstos: 2 | Pendente | |
| `MeioPagamentoTest` | Previstos: 3 | Pendente | |
| `SqliteVendaRepositoryTest` | Previstos: 1 | Pendente | |
| `ConexaoSQLiteTest` | Previstos: 1 | Pendente | |

## 2. Defeitos intencionais

Proposta de dois defeitos, escolhidos porque atingem as duas queixas principais do cliente
(estoque que não bate e faturamento errado). Para cada um: criar uma branch, inserir o defeito,
rodar `mvn test`, registrar qual teste falhou e a mensagem, e desfazer.

| Defeito | Onde inserir | Regra violada | Teste que deve falhar | Resultado |
|---|---|---|---|---|
| D1: aceitar item sem estoque suficiente | Remover a checagem em `VendaService.adicionarItem` | RN08 | `VendaServiceTest.naoAdicionaItemSemEstoque` | A registrar |
| D2: faturamento somando vendas não confirmadas | Tirar o filtro de status em `FaturamentoService.faturamentoDoDia` | RN06 | `FaturamentoServiceTest.somaSoVendasConfirmadasDoDia` | A registrar |

## 3. Antes e depois da melhoria

Proposta de melhoria a comparar: **confirmação da venda em uma transação única** ([ADR 002](adr/002-persistencia-sqlite.md)).

| | Antes | Depois |
|---|---|---|
| Comportamento | Grava a venda e baixa o estoque item a item, cada um no seu comando | Venda, itens e baixas numa transação; erro em qualquer item desfaz tudo |
| Cenário de teste | Venda de 3 itens em que o terceiro falha | Mesmo cenário |
| Resultado esperado | Venda gravada e estoque parcialmente baixado (divergência) | Nada gravado; estoque igual ao de antes |
| Teste | `SqliteVendaRepositoryTest.falhaNoMeioDaVendaNaoBaixaEstoque` | Mesmo teste |
| Resultado obtido | A registrar | A registrar |
