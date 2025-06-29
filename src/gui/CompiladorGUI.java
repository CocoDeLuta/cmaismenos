package gui;

import compilador.ParseException;
import compilador.SimpleNode;
import java.awt.*;
import java.io.*;
import javax.swing.*;

// Janela principal da aplicação do compilador, com editor, saída e visualização da árvore sintática
public class CompiladorGUI extends JFrame {
    // Área de edição do código-fonte
    private JTextArea editor;
    // Área para exibir a numeração de linhas
    private JTextArea lines;
    // Área de saída para mensagens do console
    private JTextArea output;
    // Área de texto para exibir a árvore sintática textual
    private JTextArea arvoreTextoArea;
    // Arquivo atualmente aberto
    private File arquivoAtual = null;
    // Instância do parser
    private compilador.CMaisMenos parser = null;
    // Painel gráfico da árvore sintática
    private ArvoreSintaticaPanel arvorePanel;
    // Seletor para alternar visualização da árvore/tokens
    private JComboBox<String> seletorArvore;
    // ...

    // Inicializa a interface gráfica e os componentes
    public CompiladorGUI() {
        setTitle("Compilador - Editor e Visualizador de Árvore");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Cria área de edição
        editor = new JTextArea();
        // Cria área de numeração de linhas
        lines = new JTextArea("1\n");
        lines.setEditable(false);
        lines.setBackground(new Color(230,230,230));
        lines.setFont(editor.getFont());
        lines.setMargin(new Insets(0, 8, 0, 8)); // margem interna para afastar os números
        lines.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.GRAY));
        lines.setMinimumSize(new Dimension(40, 0));
        lines.setPreferredSize(new Dimension(48, 0));

        editor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateLines(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateLines(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateLines(); }
        });

        JScrollPane scroll = new JScrollPane(editor);
        scroll.setRowHeaderView(lines);

        // Cria área de saída (console)
        output = new JTextArea();
        output.setEditable(false);

        // Cria painel gráfico da árvore sintática
        arvorePanel = new ArvoreSintaticaPanel();
        arvorePanel.setPreferredSize(new Dimension(320, 0));
        arvorePanel.setBackground(Color.WHITE);

        // Cria área de texto para árvore sintática textual
        arvoreTextoArea = new JTextArea();
        arvoreTextoArea.setEditable(false);
        arvoreTextoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));



        // Seletor para alternar visualização
        seletorArvore = new JComboBox<>(new String[] {"Desenho", "Texto"});
        seletorArvore.addActionListener(e -> alternarVisualizacaoArvore());

        // Botão de compilação
        JButton compileButton = new JButton("Compilar");
        compileButton.addActionListener(e -> compilar());

        // Painel lateral direito para árvore sintática (com seletor)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(seletorArvore, BorderLayout.NORTH);
        JPanel cardPanel = new JPanel(new CardLayout());
        cardPanel.add(new JScrollPane(arvorePanel), "Desenho");
        cardPanel.add(new JScrollPane(arvoreTextoArea), "Texto");
        rightPanel.add(cardPanel, BorderLayout.CENTER);

        // Split entre editor e console (output)
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, new JScrollPane(output));
        verticalSplit.setDividerLocation(300);

        // Split principal: editor/console à esquerda, painel da árvore à direita
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, verticalSplit, rightPanel);
        mainSplit.setDividerLocation(480);

        add(mainSplit, BorderLayout.CENTER);
        add(compileButton, BorderLayout.SOUTH);

        // Menu
        // Menu Arquivo (Novo, Abrir, Salvar)
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

    // Atualiza a numeração de linhas do editor
    private void updateLines() {
        int linesCount = editor.getLineCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= linesCount; i++) {
            sb.append(i).append("\n");
        }
        lines.setText(sb.toString());
    }



    // Compila o código-fonte, exibe árvore sintática, tokens e mensagens no console
    private void compilar() {
        output.setText("");
        editor.getHighlighter().removeAllHighlights();
        arvorePanel.setRaiz(null);
        arvoreTextoArea.setText("");

        if (arquivoAtual == null) {
            JOptionPane.showMessageDialog(this, "Nenhum arquivo aberto. Use Arquivo > Novo ou Abrir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        salvarArquivo();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            System.setErr(ps);

            try {
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
                arvoreTextoArea.setText(arvoreOut.toString());
                arvorePanel.setRaiz(node);
                // Mensagem de aceitação
                output.setText("Pode ser\n" + baos.toString());
                fis.close();
            } catch (ParseException ex) {
                output.setText("Acho que nao\n" + ex.getMessage() + "\n" + baos.toString());
                arvorePanel.setRaiz(null);
                arvoreTextoArea.setText("");
            } catch (Exception ex) {
                output.setText("Acho que nao\n" + ex.getMessage() + "\n" + baos.toString());
                arvorePanel.setRaiz(null);
                arvoreTextoArea.setText("");
            } finally {
                System.setOut(oldOut);
                System.setErr(oldOut);
            }
        } catch (Exception ex) {
            output.setText("Acho que nao\n" + ex.getMessage());
            arvorePanel.setRaiz(null);
            arvoreTextoArea.setText("");
        }
        alternarVisualizacaoArvore();
    }





    // Alterna entre visualização gráfica e textual da árvore no painel lateral
    private void alternarVisualizacaoArvore() {
        CardLayout cl = (CardLayout)((JPanel)((JPanel)seletorArvore.getParent()).getComponent(1)).getLayout();
        String modo = (String) seletorArvore.getSelectedItem();
        cl.show(((JPanel)((JPanel)seletorArvore.getParent()).getComponent(1)), modo);
    }

    // Cria um novo arquivo
    private void novoArquivo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Criar novo arquivo");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            arquivoAtual = chooser.getSelectedFile();
            editor.setText("");
            setTitle("Compilador - " + arquivoAtual.getName());
            salvarArquivo();
        }
    }

    // Abre um arquivo existente
    private void abrirArquivo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Abrir arquivo");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            arquivoAtual = chooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(arquivoAtual))) {
                StringBuilder sb = new StringBuilder();
                String linha;
                while ((linha = br.readLine()) != null) {
                    sb.append(linha).append("\n");
                }
                editor.setText(sb.toString());
                setTitle("Compilador - " + arquivoAtual.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao abrir arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Salva o arquivo atual
    private void salvarArquivo() {
        if (arquivoAtual == null) return;
        try (FileWriter fw = new FileWriter(arquivoAtual)) {
            fw.write(editor.getText());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Ponto de entrada da aplicação
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CompiladorGUI().setVisible(true));
    }
}