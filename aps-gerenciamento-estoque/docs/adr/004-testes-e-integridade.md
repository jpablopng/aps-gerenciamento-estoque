# ADR 004: Testes e integridade dos dados

**Status:** aceita

## Contexto

As queixas do cliente na entrevista são de integridade: produto registrado que não existe no estoque,
quantidade errada, erro na hora de registrar sem dizer de onde veio. O sistema novo precisa garantir
que esses estados não aconteçam e mostrar isso com testes.

## Opções avaliadas

1. **Validar só na tela:** fácil, mas qualquer outro caminho (outro comando, outro serviço) pula a validação.
2. **Validar só no banco (constraints):** protege os dados, mas o erro chega como exceção de SQL, sem mensagem clara para o operador.
3. **Três barreiras:** invariantes no domínio, regras no serviço com mensagem clara, e constraints no banco como última garantia. Testes automatizados em cada nível.

## Decisão

Opção 3.

* **Domínio:** objetos de valor que não aceitam estado inválido. `Quantidade` nunca é negativa (RN03); `Produto` não é criado sem os campos obrigatórios (RN01).
* **Serviço:** as regras que dependem de outros dados (duplicidade RN02, estoque suficiente RN08) são checadas no serviço e lançam `RegraNegocioException` com causa e solução, que o console mostra (US04).
* **Banco:** `UNIQUE` e `CHECK` no esquema e transação na venda ([ADR 002](002-persistencia-sqlite.md)).
* **Testes:** JUnit 5, rodando com `mvn test`.
  * Testes de unidade do domínio, com casos de fronteira (ex.: vender exatamente o que tem em estoque e um a mais).
  * Testes de serviço com repositórios em memória, um teste por regra de negócio do MVP.
  * Testes de integração do repositório SQLite com um banco temporário.
  * Cada teste aparece na [matriz de rastreabilidade](../rastreabilidade.md).

## Consequências

* Um erro de regra sempre tem uma mensagem pensada para o operador, nunca um stack trace.
* Os testes viram a evidência de cada regra na matriz e no relatório de validação.
* A mesma regra pode aparecer em dois lugares (domínio e banco); o grupo aceita a duplicação como proteção extra.

## Evidência no código ou nos testes

* `model/Quantidade.java` e `QuantidadeTest` (já no repositório): rejeita quantidade negativa e testa as fronteiras da subtração.
* Demais testes listados em [rastreabilidade.md](../rastreabilidade.md), a implementar.
