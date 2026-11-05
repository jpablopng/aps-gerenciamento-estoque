# Gerenciamento de Estoque

**Universidade Federal da Paraíba | Campus IV**
Ciência da Computação e Sistemas de Informação
Disciplina: Análise e Projeto de Sistemas (2026.2)
Professor: Dr. Dorgival Netto

## Identificação da equipe

* **Equipe:** Grupo 01
* **Projeto:** Gerenciamento de Estoque
* **Repositório:** https://github.com/USUARIO/aps-gerenciamento-estoque
* **Branch principal:** `main`

| Nome completo | E-mail institucional | Usuário GitHub | Link do perfil |
|---|---|---|---|
| João Pablo da Silva | joao.pablo@dcx.ufpb.br | @usuario | https://github.com/usuario |
| Luigi Truglio | nome@dcx.ufpb.br | @usuario | https://github.com/usuario |
| Alex Renovato | nome@dcx.ufpb.br | @usuario | https://github.com/usuario |
| Pedro Filho | nome@dcx.ufpb.br | @usuario | https://github.com/usuario |

## Objetivo do sistema

Controlar entrada, reserva e saída de produtos, distinguindo estoque físico e estoque disponível,
com alerta de estoque mínimo e critério de reposição substituível.

## Origem dos requisitos

* Documentação anterior: (link)
* O que foi revisto ou adaptado: (descrever)

## Escopo implementado

* **Casos de uso:** UC01, UC02, UC03, UC04 (descrever)
* **Regras de negócio:** RN01 a RN06 (descrever)
* **Perfis:** (ex.: Operador e Gerente)
* **Limitações conhecidas:** (listar)

## Arquitetura

Arquitetura em camadas, Java 17, Maven.

| Pacote | Responsabilidade |
|---|---|
| `model` | Entidades, objetos de valor, estados e invariantes |
| `service` | Casos de uso, coordenação e autorização |
| `repository` | Contratos e implementação da persistência |
| `controller` | Comandos do console e apresentação dos resultados |

## Padrões de projeto

(listar os padrões, onde estão no código e o teste de cada um)

## Documentação

* [Registros de decisão](docs/adr/)
* [Diagrama de classes](docs/uml/)
* Matriz de rastreabilidade: `docs/rastreabilidade.md`
* Relatório de validação: `docs/validacao.md`
* Uso de IA: `docs/uso-ia.md`

## Como executar

Requisitos: Java 17 ou superior e Maven 3.8 ou superior.

```
mvn test                 # roda todos os testes
mvn compile exec:java    # executa o sistema no console
```

## Dados de demonstração

Ver [dados/demo](dados/demo/).
