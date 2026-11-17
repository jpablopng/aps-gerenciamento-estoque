# Matriz de rastreabilidade

Liga cada caso de uso e regra de negócio do MVP à classe e ao método que a implementam, ao teste
que a verifica e à evidência. IDs de histórias (US), regras (RN) e requisitos não funcionais (RNF)
seguem o Documento de Requisitos (revisão 13); RN08 a RN10 foram acrescentadas (ver README).

**Situação:** o código ainda está sendo implementado. A coluna "Evidência" diz o que já existe
no repositório; o resto é o planejado e será atualizado a cada entrega.

## Regras de negócio

| Caso de uso | História | Regra | Classe e método | Teste | Evidência |
|---|---|---|---|---|---|
| UC02 Cadastrar produto | US01 | RN01 Campos obrigatórios | `Produto` (construtor), `ProdutoService.cadastrar` | `ProdutoServiceTest.rejeitaCampoObrigatorioVazio` | Pendente |
| UC02 Cadastrar produto | US01 | RN02 Sem produto duplicado | `ProdutoService.cadastrar`, `ProdutoRepository.existePorNomeEMarca` | `ProdutoServiceTest.rejeitaProdutoDuplicado` | Pendente |
| UC01, UC02, UC03 | US01 | RN03 Quantidade nunca negativa | `Quantidade.de`, `Quantidade.subtrair` | `QuantidadeTest.rejeitaNegativa`, `subtrairAcimaDoDisponivelFalha`, `subtrairTudoDaZero` | **Implementado e testado** |
| UC01 Registrar venda | US03 | RN04 Baixa automática ao confirmar | `VendaService.confirmar`, `EstoqueService.darBaixa`, `Produto.darBaixa` | `VendaServiceTest.confirmarBaixaEstoqueDeCadaItem`, `SqliteVendaRepositoryTest.falhaNoMeioDaVendaNaoBaixaEstoque` | Pendente |
| UC04 Consultar faturamento | US05 | RN06 Faturamento atualizado a cada venda confirmada | `FaturamentoService.faturamentoDoDia` | `FaturamentoServiceTest.somaSoVendasConfirmadasDoDia`, `ignoraVendasDeOutroDia` | Pendente |
| UC01 Registrar venda | US07 | RN07 Alerta de estoque mínimo | `EstoqueService.darBaixa`, `AlertaEstoqueListener.estoqueMinimoAtingido` | `EstoqueServiceTest.alertaAoAtingirMinimo`, `naoAlertaAcimaDoMinimo` | Pendente |
| UC01 Registrar venda | US02 | RN08 Só vende com estoque | `VendaService.adicionarItem` | `VendaServiceTest.naoAdicionaItemSemEstoque`, `vendeExatamenteOQueTemEmEstoque` | Pendente |
| UC03 Registrar entrada | US06 | RN09 Entrada soma ao estoque | `EstoqueService.registrarEntrada`, `Produto.repor` | `EstoqueServiceTest.entradaSomaAoEstoque` | Pendente |
| UC01 Registrar venda | US09, US14 | RN10 Venda registra quem vendeu, itens e forma de pagamento | `Venda.confirmar`, `MeioPagamento.processar` | `VendaServiceTest.vendaConfirmadaGuardaOperadorItensEForma`, `MeioPagamentoTest` | Pendente |
| Fora do MVP | US18 | RN05 Cancelamento com estorno | | | Não implementado nesta versão |

## Requisitos não funcionais

| Requisito | Como é atendido | Verificação | Evidência |
|---|---|---|---|
| RNF01 Desktop | Aplicação Java executada no computador do caixa | Execução com `mvn compile exec:java` | Pendente |
| RNF02 Telas em até 2 s | Console e consultas locais em SQLite | Medição manual no roteiro de demonstração | Pendente |
| RNF03 Dual Core e 4 GB | Sem servidor de banco; SQLite em arquivo ([ADR 002](adr/002-persistencia-sqlite.md)) | Execução numa máquina com essa configuração | Pendente |
| RNF04 Erros compreensíveis | `RegraNegocioException` com causa e solução, exibida pelo console (US04) | Testes de serviço conferem a mensagem; roteiro de demonstração | Pendente |
| RNF05 Venda rápida | Venda de 3 itens com pagamento em poucos comandos | Roteiro de demonstração cronometrado | Pendente |

## Padrões de projeto

| Padrão | Classes | Teste |
|---|---|---|
| Strategy | `MeioPagamento`, `PagamentoPix`, `PagamentoDinheiro`, `PagamentoCartao` | `MeioPagamentoTest` |
| Observer | `EstoqueService`, `AlertaEstoqueListener`, `AlertaConsole` | `EstoqueServiceTest.alertaAoAtingirMinimo` |
| Repository (DAO) | `*Repository`, `Sqlite*Repository` | Testes de serviço com repositórios em memória |
| Singleton | `ConexaoSQLite` | `ConexaoSQLiteTest.devolveSempreAMesmaInstancia` |
