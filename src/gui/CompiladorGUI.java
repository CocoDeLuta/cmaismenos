package gui;

import compilador.SimpleNode;
import java.awt.*;
import java.io.*;
import javax.swing.*;

// Janela principal da aplicação do compilador.
// Responsável por montar a interface gráfica, integrar editor, saída, árvore sintática e tokens.
public class CompiladorGUI extends JFrame 
{
    // Painel de saída para mensagens do console (erros, avisos, aceitação)
    private OutputPanel outputPanel;
    
    // Painel de texto para exibir a árvore sintática em formato textual
    private ArvoreTextoPanel arvoreTextoPanel;

    // Painel para exibir tokens reconhecidos e possíveis
    private TokensPanel tokensPanel;

    // Painel gráfico para desenhar a árvore sintática
    private ArvoreSintaticaPanel arvorePanel;

    // Arquivo atualmente aberto no editor
    private File arquivoAtual = null;

    // Instância do parser (analisador sintático)
    private compilador.CMaisMenos parser = null;
    
    // ComboBox para alternar entre visualização gráfica, textual e tokens
    private JComboBox<String> seletorArvore;
    private EditorPanel editorPanel;

    // Construtor: inicializa a interface gráfica e todos os painéis/componentes
    public CompiladorGUI() 
    {
        setTitle("Compilador - Editor e Visualizador de Árvore");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Cria painel do editor de código (com destaque de sintaxe e numeração de linhas)
        editorPanel = new EditorPanel();

        // Cria painel de saída (console de mensagens)
        outputPanel = new OutputPanel();

        // Cria painel gráfico da árvore sintática
        arvorePanel = new ArvoreSintaticaPanel();
        arvorePanel.setPreferredSize(new Dimension(320, 0));
        arvorePanel.setBackground(Color.WHITE);

        

        // Cria painel de texto para árvore sintática textual
        arvoreTextoPanel = new ArvoreTextoPanel();

        // Cria painel de tokens
        tokensPanel = new TokensPanel();

        // ComboBox para alternar visualização (gráfico, texto, tokens)
        seletorArvore = new JComboBox<>(new String[] {"Desenho", "Texto", "Tokens"});
        seletorArvore.addActionListener(e -> alternarVisualizacaoArvore());


        // Botão para compilar o código
        JButton compileButton = new JButton("Compilar");
        compileButton.addActionListener(e -> compilar());

        // Botão para aumentar o tamanho da fonte do editor
        JButton aumentarFonteButton = new JButton("A+");
        aumentarFonteButton.setToolTipText("Aumentar fonte do editor");
        aumentarFonteButton.addActionListener(e -> editorPanel.aumentarFonte());

        // Botão para diminuir o tamanho da fonte do editor
        JButton diminuirFonteButton = new JButton("A-");
        diminuirFonteButton.setToolTipText("Diminuir fonte do editor");
        diminuirFonteButton.addActionListener(e -> editorPanel.diminuirFonte());


        // Painel lateral direito: contém árvore sintática (gráfico/texto) e tokens
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(seletorArvore, BorderLayout.NORTH);
        JPanel cardPanel = new JPanel(new CardLayout());
        cardPanel.add(new JScrollPane(arvorePanel), "Desenho");
        cardPanel.add(arvoreTextoPanel, "Texto");
        cardPanel.add(tokensPanel, "Tokens");
        rightPanel.add(cardPanel, BorderLayout.CENTER);

        // Split vertical: editor acima, console abaixo
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPanel, outputPanel);
        verticalSplit.setDividerLocation(300);

        // Split horizontal: editor/console à esquerda, árvore/tokens à direita
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, verticalSplit, rightPanel);
        mainSplit.setDividerLocation(480);


        // Painel de botões na parte inferior (compilar, aumentar/diminuir fonte)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(compileButton);
        bottomPanel.add(aumentarFonteButton);
        bottomPanel.add(diminuirFonteButton);

        add(mainSplit, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Menu superior: opções de arquivo (Novo, Abrir, Salvar)
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArquivo = new JMenu("Arquivo");
        JMenuItem novoItem = new JMenuItem("Novo");
        JMenuItem abrirItem = new JMenuItem("Abrir...");
        JMenuItem salvarItem = new JMenuItem("Salvar");

        novoItem.addActionListener(e -> novoArquivo());
        abrirItem.addActionListener(e -> abrirArquivo());
        salvarItem.addActionListener(e -> salvarArquivo());

        menuArquivo.add(novoItem);
        menuArquivo.add(abrirItem);
        menuArquivo.add(salvarItem);
        menuBar.add(menuArquivo);
        setJMenuBar(menuBar);
    }


    // Compila o código-fonte, exibe árvore sintática, tokens e mensagens no console
    private void compilar() 
    {
        // Executa a compilação do código-fonte, atualiza árvore, tokens e saída
        outputPanel.setOutputText("");
        //editorPanel.getEditor().getHighlighter().removeAllHighlights();
        //editorPanel.setErrorLine(0); // Removido destaque de erro
        arvorePanel.setRaiz(null);
        arvoreTextoPanel.setArvoreText("");
        tokensPanel.setTokensText("");

        if (arquivoAtual == null) 
        {
            JOptionPane.showMessageDialog(this, "Nenhum arquivo aberto. Use Arquivo > Novo ou Abrir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        salvarArquivo();
        try 
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            System.setErr(ps);

            try 
            {
                compilador.CMaisMenos.eof = false;
                FileInputStream fis = new FileInputStream(arquivoAtual);
                if (parser == null) {
                    parser = new compilador.CMaisMenos(fis);
                } else {
                    parser.ReInit(fis);
                }
                compilador.CMaisMenos.firstErrorToken = null;
                SimpleNode node = compilador.CMaisMenos.main();

                // Exibe árvore textual capturando System.out
                ByteArrayOutputStream arvoreOut = new ByteArrayOutputStream();
                PrintStream arvorePs = new PrintStream(arvoreOut);
                PrintStream oldSysOut = System.out;
                System.setOut(arvorePs);
                node.dump(" -> ");
                arvorePs.flush();
                System.setOut(oldSysOut);
                arvoreTextoPanel.setArvoreText(arvoreOut.toString());
                arvorePanel.setRaiz(node);

                // Exibe todos os tokens possíveis
                StringBuilder tokensStr = new StringBuilder();
                tokensStr.append("=== TOKENS POSSÍVEIS DA LINGUAGEM ===\n");
                String[] tokenImage = compilador.CMaisMenosConstants.tokenImage;
                for (int i = 0; i < tokenImage.length; i++) 
                {
                    String nome = compilador.CMaisMenos.im(i);
                    tokensStr.append(String.format("[%d] %s\n", i, nome));
                }
                tokensStr.append("\n=== TOKENS IDENTIFICADOS NO CÓDIGO ===\n");

                // Exibe tokens identificados no código
                FileInputStream fisTokens = new FileInputStream(arquivoAtual);
                compilador.CMaisMenos.ReInit(fisTokens);
                compilador.Token t;
                do 
                {
                    t = compilador.CMaisMenos.getNextToken();
                    tokensStr.append("[").append(t.image).append("] ")
                        .append("(tipo: ").append(compilador.CMaisMenos.im(t.kind)).append(")\n");
                } while (t.kind != 0); // 0 = EOF
                fisTokens.close();
                tokensPanel.setTokensText(tokensStr.toString());

                // Mensagem de aceitação ao final da saída
                outputPanel.setOutputText(baos.toString() + "\nPode ser");
                fis.close();
            }  
            catch (Exception ex) 
            {
                // Mensagem de erro ao final da saída
                outputPanel.setOutputText(baos.toString() + "\nAcho que nao\n" + ex.getMessage());
                arvorePanel.setRaiz(null);
                arvoreTextoPanel.setArvoreText("");
                tokensPanel.setTokensText("");
            } 
            finally 
            {
                System.setOut(oldOut);
                System.setErr(oldOut);
            }
        } 
        catch (Exception ex) 
        {
            outputPanel.setOutputText("Acho que nao\n" + ex.getMessage());
            arvorePanel.setRaiz(null);
            arvoreTextoPanel.setArvoreText("");
        }
        alternarVisualizacaoArvore();
    }





    // Alterna entre visualização gráfica e textual da árvore no painel lateral
    private void alternarVisualizacaoArvore() 
    {
        // Alterna entre visualização gráfica, textual e tokens no painel lateral
        CardLayout cl = (CardLayout)((JPanel)((JPanel)seletorArvore.getParent()).getComponent(1)).getLayout();
        String modo = (String) seletorArvore.getSelectedItem();
        cl.show(((JPanel)((JPanel)seletorArvore.getParent()).getComponent(1)), modo);
    }

    // Cria um novo arquivo
    private void novoArquivo() 
    {
        // Cria um novo arquivo e limpa o editor
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Criar novo arquivo");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) 
        {
            arquivoAtual = chooser.getSelectedFile();
            editorPanel.setText("");
            setTitle("Compilador - " + arquivoAtual.getName());
            salvarArquivo();
        }
    }

    // Abre um arquivo existente
    private void abrirArquivo() 
    {
        // Abre um arquivo existente e carrega no editor
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Abrir arquivo");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) 
        {
            arquivoAtual = chooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(arquivoAtual))) 
            {
                StringBuilder sb = new StringBuilder();
                String linha;
                while ((linha = br.readLine()) != null) 
                {
                    sb.append(linha).append("\n");
                }
                editorPanel.setText(sb.toString());
                setTitle("Compilador - " + arquivoAtual.getName());
            } 
            catch (IOException ex) 
            {
                JOptionPane.showMessageDialog(this, "Erro ao abrir arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Salva o arquivo atual
    private void salvarArquivo() 
    {
        // Salva o conteúdo atual do editor no arquivo aberto
        if (arquivoAtual == null) return;
        try (FileWriter fw = new FileWriter(arquivoAtual)) 
        {
            fw.write(editorPanel.getText());
        } 
        catch (IOException ex) 
        {
            JOptionPane.showMessageDialog(this, "Erro ao salvar arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Ponto de entrada da aplicação
    public static void main(String[] args) 
    {
        // Ponto de entrada da aplicação: exibe a janela principal
        SwingUtilities.invokeLater(() -> new CompiladorGUI().setVisible(true));
    }
}