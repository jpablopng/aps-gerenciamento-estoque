# Declaração de uso de IA

**Ferramenta:** Claude (Anthropic)

## Atividades

- Leitura e resumo das orientações do projeto
- Propostas de arquitetura em camadas
- Auxílio na ideia e na construção inicial do código e dos testes, que depois foram revisados pela equipe
- Rascunhos de documentação, README e slides
- Organização de commits

## Interações mais importantes

1. **Redução do escopo.** A primeira versão tinha mais coisa do que a Parcial 02 pedia. Para deixar o projeto mais enxuto, fizemos uma refatoração com auxílio da IA e ficamos só com o necessário.
2. **Estoque físico e disponível.** A IA propôs guardar físico e reservado no `SaldoEstoque` e calcular o disponível a partir deles. Com isso, reserva e saída acima do disponível ficam bloqueadas num lugar só.
3. **Operação sem registros parciais.** Foi proposta uma transação tudo ou nada no repositório, mais um teste com falha simulada para mostrar que o saldo não muda quando a operação falha.
