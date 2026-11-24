# Diagrama de classes

Classes implementadas em `src/main/java`. Fonte em Mermaid (`.mmd`) e imagem exportada (`.png`).

## 1. Domínio (`model`)

O `SaldoEstoque` separa o estoque físico do reservado; o disponível é físico menos reservado.
A `Reserva` só pode ser confirmada ou cancelada enquanto está ATIVA. Cada operação gera uma `Movimentacao`.

![Domínio](diagrama-dominio.png)

```mermaid
classDiagram
    direction LR

    class Produto {
        -String codigo
        -String nome
        -Quantidade estoqueMinimo
        +codigo() String
        +estoqueMinimo() Quantidade
    }

    class SaldoEstoque {
        -String produtoCodigo
        -Quantidade fisico
        -Quantidade reservado
        +disponivel() Quantidade
        +comEntrada(Quantidade) SaldoEstoque
        +comReserva(Quantidade) SaldoEstoque
        +semReserva(Quantidade) SaldoEstoque
        +comSaidaDaReserva(Quantidade) SaldoEstoque
        +comSaidaDireta(Quantidade) SaldoEstoque
    }

    class Reserva {
        -long id
        -String produtoCodigo
        -Quantidade quantidade
        -StatusReserva status
        -LocalDateTime criadaEm
        +nova(long, String, Quantidade, LocalDateTime) Reserva$
        +confirmada() Reserva
        +cancelada() Reserva
    }

    class StatusReserva {
        <<enumeration>>
        ATIVA
        CONFIRMADA
        CANCELADA
    }

    class Movimentacao {
        <<record>>
        +long id
        +String produtoCodigo
        +TipoMovimentacao tipo
        +Quantidade quantidade
        +Long reservaId
        +LocalDateTime dataHora
        +Quantidade fisicoApos
        +Quantidade disponivelApos
    }

    class TipoMovimentacao {
        <<enumeration>>
        ENTRADA
        RESERVA
        CANCELAMENTO_RESERVA
        SAIDA
    }

    class Quantidade {
        -int valor
        +de(int) Quantidade$
        +somar(Quantidade) Quantidade
        +subtrair(Quantidade) Quantidade
        +menorQue(Quantidade) boolean
    }

    class RegraNegocioException
    class EstoqueInsuficienteException
    class OperacaoJaConcluidaException

    Produto "1" -- "1" SaldoEstoque : tem
    Produto "1" -- "0..*" Reserva : reservado em
    Produto "1" -- "0..*" Movimentacao : histórico
    Reserva "0..1" -- "0..*" Movimentacao : origina
    Reserva --> StatusReserva
    Movimentacao --> TipoMovimentacao
    SaldoEstoque ..> Quantidade
    SaldoEstoque ..> EstoqueInsuficienteException : lança
    Reserva ..> OperacaoJaConcluidaException : lança
    EstoqueInsuficienteException --|> RegraNegocioException
    OperacaoJaConcluidaException --|> RegraNegocioException
```

## 2. Serviço, repositório e padrões (`service`, `repository`, `controller`)

![Camadas](diagrama-camadas.png)

```mermaid
classDiagram
    direction TB

    class Main {
        +main(String[])$
    }
    class DemonstracaoEstoque {
        -EstoqueService estoque
        +executar()
    }
    class AlertaConsole {
        +estoqueMinimoAtingido(AlertaEstoque)
    }

    class EstoqueService {
        -EstoqueRepository repositorio
        -PoliticaReposicao politicaReposicao
        -List~AlertaEstoqueListener~ ouvintes
        +cadastrarProduto(String, String, int) Produto
        +registrarEntrada(String, int) Movimentacao
        +reservar(String, int) Reserva
        +confirmarReserva(long) Movimentacao
        +cancelarReserva(long) Movimentacao
        +registrarSaida(String, int) Movimentacao
        +trocarPoliticaReposicao(PoliticaReposicao)
        +adicionarOuvinte(AlertaEstoqueListener)
        -verificarEstoqueMinimo(String)
    }

    class PoliticaReposicao {
        <<interface Strategy>>
        +sugerirReposicao(Produto, SaldoEstoque) Quantidade
        +descricao() String
    }
    class ReposicaoAteNivelAlvo {
        -int multiplicador
    }
    class ReposicaoPorLoteFixo {
        -int tamanhoLote
    }

    class AlertaEstoqueListener {
        <<interface Observer>>
        +estoqueMinimoAtingido(AlertaEstoque)
    }
    class AlertaEstoque {
        <<record>>
        +Produto produto
        +SaldoEstoque saldo
        +Quantidade reposicaoSugerida
        +String criterio
    }

    class EstoqueRepository {
        <<interface>>
        +buscarProduto(String) Optional~Produto~
        +buscarSaldo(String) Optional~SaldoEstoque~
        +buscarReserva(long) Optional~Reserva~
        +movimentacoesDo(String) List~Movimentacao~
        +emTransacao(trabalho) T
    }
    class Transacao {
        <<interface>>
        +salvarSaldo(SaldoEstoque)
        +salvarReserva(Reserva)
        +registrarMovimentacao(Movimentacao)
    }
    class EstoqueEmMemoria {
        -Map produtos
        -Map saldos
        -Map reservas
        -List movimentacoes
    }
    class EstoqueRepositoryComFalha {
        <<Decorator>>
        -EstoqueRepository real
        +falharNaProximaMovimentacao()
    }

    Main ..> DemonstracaoEstoque : cria
    DemonstracaoEstoque --> EstoqueService
    EstoqueService "1" --> "1" EstoqueRepository
    EstoqueService "1" --> "1" PoliticaReposicao
    EstoqueService "1" o-- "0..*" AlertaEstoqueListener
    EstoqueService ..> AlertaEstoque : cria
    ReposicaoAteNivelAlvo ..|> PoliticaReposicao
    ReposicaoPorLoteFixo ..|> PoliticaReposicao
    AlertaConsole ..|> AlertaEstoqueListener
    EstoqueEmMemoria ..|> EstoqueRepository
    EstoqueRepositoryComFalha ..|> EstoqueRepository
    EstoqueRepositoryComFalha --> EstoqueRepository : decora
    EstoqueRepository ..> Transacao : usa
```

| Padrão | Classes | Para quê |
|---|---|---|
| Strategy | `PoliticaReposicao`, `ReposicaoAteNivelAlvo`, `ReposicaoPorLoteFixo` | Critério de reposição substituível |
| Observer | `EstoqueService`, `AlertaEstoqueListener`, `AlertaConsole` | Alerta de estoque mínimo |
| Decorator | `EstoqueRepositoryComFalha` | Simular falha para demonstrar que nada fica gravado pela metade |
