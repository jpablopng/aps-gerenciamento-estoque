# ADR 001: Arquitetura em camadas

**Status:** aceita

## Contexto

O sistema tem regras que precisam valer sempre, venha a ação de onde vier: só vender com estoque (RN08),
baixar o estoque ao confirmar a venda (RN04) e atualizar o faturamento a cada venda confirmada (RN06).
O cliente reclamou de erros sem explicação no sistema atual (entrevista), então cada erro precisa
chegar ao usuário com causa clara (US04, RNF04). O computador da loja é fraco (RNF03, RNF04) e o grupo
tem quatro pessoas trabalhando em paralelo.

## Opções avaliadas

1. **Tudo junto no Main ou na tela:** mais rápido no começo, mas as regras ficam espalhadas e não dá para testar sem a interface.
2. **MVC simples (tela e modelo):** separa a tela, mas as regras acabam no Controller.
3. **Camadas: controller, service, repository e model:** cada camada tem uma responsabilidade e só chama a de baixo.

## Decisão

Opção 3. As dependências seguem uma direção só:

```
controller  ->  service  ->  repository (interfaces)  <-  repository.sqlite (implementação)
     \             |               |
      \----------> model <---------/
```

* `controller` lê os comandos do console, chama um serviço e mostra o resultado ou a mensagem de erro. Não tem regra.
* `service` executa os casos de uso UC01 a UC04 e aplica as regras. Quando uma regra é violada, lança `RegraNegocioException` com a causa e o que fazer.
* `repository` define as interfaces de persistência; `repository.sqlite` as implementa com JDBC.
* `model` tem as entidades e as invariantes que não dependem de banco nem de tela (ex.: `Quantidade` nunca negativa, RN03).
* `Main` só cria os objetos e injeta as dependências pelo construtor.

**Interface:** console nesta versão, como prevê a estrutura do projeto. O Documento de Requisitos tem um
protótipo de telas no Figma e a RNF01 fala em executável instalado no computador do caixa. Como as regras
ficam no `service`, uma interface gráfica (JavaFX ou Swing) pode substituir o console depois mexendo só no `controller`.

## Consequências

* As regras podem ser testadas sem console e sem banco, usando repositórios em memória.
* Trocar a interface ou o banco não muda o `service`.
* Mais classes e interfaces do que numa solução direta; o grupo aceita esse custo pela testabilidade.
* O trabalho pode ser dividido por camada entre os membros.
* Ponto em aberto: confirmar com o professor se a interface em console atende a RNF01 nesta entrega.

## Evidência no código ou nos testes

* Pacotes `model`, `service`, `repository` e `controller` em `src/main/java/br/ufpb/aps/estoque/`.
* `Main.java` sem regra de negócio, só montagem dos componentes.
* `model/Quantidade.java` e `QuantidadeTest` mostram a RN03 garantida no domínio e testada sem banco nem tela.
