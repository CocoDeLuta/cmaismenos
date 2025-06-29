# cmaismenos

## Descrição
O projeto **cmaismenos** é um compilador simples desenvolvido para a linguagem fictícia C+-, com o objetivo de fins didáticos e experimentação em construção de compiladores. Ele inclui análise léxica, sintática e geração de árvore sintática, além de uma interface gráfica para visualização da árvore.

## Estrutura do Projeto

- `src/`: Código-fonte Java do compilador, analisadores e interface gráfica.
- `bin/`: Arquivos compilados (.class) e gramáticas.
- `recovery/`: Implementação de mecanismos de recuperação de erros.
- `gui/`: Interface gráfica para visualização da árvore sintática.

## Como Compilar

1. Certifique-se de ter o [Java JDK 8+](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html) instalado e configurado no PATH.
2. No terminal, navegue até a pasta do projeto e execute:
   ```powershell
   javac -d bin src/module-info.java src/compilador/*.java src/gui/*.java src/recovery/*.java
   ```

## Como Executar

Para rodar a interface gráfica do compilador:

```powershell
java -cp bin gui.CompiladorGUI
```

## Funcionalidades

- Análise léxica e sintática da linguagem C+-
- Geração e visualização da árvore sintática
- Recuperação de erros sintáticos
- Interface gráfica simples

## Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests.

## Licença

Este projeto é distribuído sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.