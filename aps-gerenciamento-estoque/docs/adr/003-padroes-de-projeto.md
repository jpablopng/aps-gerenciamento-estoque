# ADR 003: Padrões de projeto

**Status:** aceita

## Contexto

Três pontos do sistema tendem a mudar e merecem um padrão:

* **Formas de pagamento:** Pix, dinheiro, débito e crédito, cada uma com um comportamento. O Pix ainda vai evoluir: hoje o cliente quer o QR Code com o valor da compra; o Documento de Requisitos prevê confirmação automática via API no futuro (UC01, fluxo alternativo do passo 7).
* **Alerta de estoque mínimo (RN07):** hoje aparece no console, amanhã pode ir para outra tela ou notificação.
* **Acesso ao banco:** pode trocar de tecnologia ou ser substituído por uma versão em memória nos testes.

A conexão com o arquivo SQLite também precisa ser única.

## Opções avaliadas

| Problema | Solução simples | Padrão |
|---|---|---|
| Comportamento por forma de pagamento | `switch` sobre o enum em cada lugar | Strategy |
| Avisar sobre estoque mínimo | `System.out.println` dentro do serviço | Observer |
| Serviço dependendo do SQLite | SQL direto dentro do serviço | Repository (DAO) |
| Conexão com o banco | Abrir conexão em cada método | Singleton |

## Decisão

* **Strategy:** a interface `MeioPagamento` tem `processar(Venda)`.
  * `PagamentoPix` gera o código Pix "copia e cola" (padrão BR Code do Banco Central) já com o valor da venda, exibido como QR Code. O operador confirma o recebimento.
  * `PagamentoDinheiro` recebe o valor entregue e calcula o troco.
  * `PagamentoCartao` só registra débito ou crédito, já que o cliente passa o cartão na maquininha.
  * A futura integração com API de Pix será uma nova estratégia (`PagamentoPixApi`), sem mexer na `VendaService`.
* **Observer:** `EstoqueService` mantém uma lista de `AlertaEstoqueListener` e chama `estoqueMinimoAtingido(Produto)` quando a baixa deixa o produto no mínimo ou abaixo (RN07). O console se registra como ouvinte.
* **Repository (DAO):** `ProdutoRepository`, `VendaRepository`, `EntradaRepository` e `UsuarioRepository` são interfaces; `Sqlite*Repository` as implementam. Os serviços recebem as interfaces pelo construtor.
* **Singleton:** `ConexaoSQLite.getInstancia()` devolve sempre a mesma conexão.

## Consequências

* Uma forma de pagamento nova é uma classe nova; a `VendaService` não muda.
* O alerta pode ganhar outros ouvintes sem alterar o `EstoqueService`.
* O Singleton cria estado global, o que atrapalha testes; por isso só a camada `repository.sqlite` o usa, e os serviços nunca o acessam direto.
* O Pix desta versão depende do operador conferir o recebimento; isso está registrado como limitação no README.

## Evidência no código ou nos testes

A implementar. Testes previstos:

* `MeioPagamentoTest`: troco no dinheiro; valor da venda presente no código Pix; cartão registra débito ou crédito.
* `EstoqueServiceTest.alertaAoAtingirMinimo` e `naoAlertaAcimaDoMinimo`, com um ouvinte falso que registra as chamadas.
* `ConexaoSQLiteTest.devolveSempreAMesmaInstancia`.
