package gui;

import compilador.ParseException;
import compilador.SimpleNode;
import java.awt.*;
import java.io.*;
import javax.swing.*;
// IMPORTAÇÕES PARA JTEXTPANE E HIGHLIGHTING:
import javax.swing.text.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

// Janela principal da aplicação do compilador, com editor, saída e visualização da árvore sintática
public class CompiladorGUI extends JFrame {
    // Área de edição do código-fonte (ALTERADO PARA JTEXTPANE)
    private JTextPane editor;
    // Área para exibir a numeração de linhas
    private JTextArea lines;
    // Área de saída para mensagens do console
    private JTextArea output;
    // Área de texto para exibir a árvore sintática textual
    private JTextArea arvoreTextoArea;
    // NOVA: Área para exibir palavras-chave identificadas
    private JTextArea palavrasChaveArea;
    // Arquivo atualmente aberto
    private File arquivoAtual = null;
    // Instância do parser
    private compilador.CMaisMenos parser = null;
    // Painel gráfico da árvore sintática
    private ArvoreSintaticaPanel arvorePanel;
    // Seletor para alternar visualização da árvore/tokens
    private JComboBox<String> seletorArvore;
    
    // ATRIBUTOS PARA HIGHLIGHTING:
    // Palavras-chave da linguagem C+-
    private static final String[] KEYWORDS = {
    		"nascoxas","tipoisso","euacho","poucacoisa","falouedisse","maisoumenos","sepa","acontece","vainafe"
        /*"int", "float", "char", "string", "bool", "void", "if", "else", 
        "while", "for", "return", "break", "continue", "true", "false",
        "main", "print", "read"*/
    };
    
    // Operadores
    private static final String[] OPERATORS = {
        "\\+", "-", "\\*", "/", "=", "==", "!=", "<", ">", "<=", ">=", 
        "&&", "\\|\\|", "!", "\\(", "\\)", "\\{", "\\}", "\\[", "\\]", ";", ","
    };
    
    // Estilos para syntax highlighting
    private SimpleAttributeSet keywordStyle;
    private SimpleAttributeSet operatorStyle;
    private SimpleAttributeSet numberStyle;
    private SimpleAttributeSet stringStyle;
    private SimpleAttributeSet commentStyle;
    private SimpleAttributeSet normalStyle;
    
    // Para marcar linhas com erro
    private Set<Integer> errorLines = new HashSet<>();
    private DefaultHighlighter.DefaultHighlightPainter errorHighlighter;
    
    // NOVOS ATRIBUTOS PARA CONTROLE DE HIGHLIGHTING:
    private boolean updatingHighlight = false;
    private Timer highlightTimer;
    private int lastCaretPosition = 0;

    // Inicializa a interface gráfica e os componentes
    public CompiladorGUI() {
        setTitle("Compilador - Editor e Visualizador de Árvore");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Cria área de edição COM JTEXTPANE:
        editor = new JTextPane();
        editor.setFont(new Font("Consolas", Font.PLAIN, 14));
        
        // Inicializar estilos para highlighting
        initializeStyles();
        
        // Timer para highlighting (aumentado o delay para evitar highlighting excessivo)
        highlightTimer = new Timer(800, e -> {
            if (!updatingHighlight) {
                applySyntaxHighlightingWithCursor();
                updatePalavrasChave(); // NOVO: Atualizar palavras-chave quando aplicar highlighting
            }
        });
        highlightTimer.setRepeats(false);
        
        // Cria área de numeração de linhas
        lines = new JTextArea("1\n");
        lines.setEditable(false);
        lines.setBackground(new Color(230,230,230));
        lines.setFont(editor.getFont());
        lines.setMargin(new Insets(0, 8, 0, 8));
        lines.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.GRAY));
        lines.setMinimumSize(new Dimension(40, 0));
        lines.setPreferredSize(new Dimension(48, 0));

        // LISTENER MELHORADO para evitar highlighting excessivo:
        editor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { 
                if (!updatingHighlight) {
                    updateLines();
                    scheduleHighlighting();
                }
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { 
                if (!updatingHighlight) {
                    updateLines();
                    scheduleHighlighting();
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { 
                if (!updatingHighlight) {
                    updateLines();
                    scheduleHighlighting();
                }
            }
        });

        // Listener para capturar mudanças no cursor
        editor.addCaretListener(e -> {
            lastCaretPosition = e.getDot();
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

        // NOVA: Cria área para exibir palavras-chave identificadas
        palavrasChaveArea = new JTextArea();
        palavrasChaveArea.setEditable(false);
        palavrasChaveArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        palavrasChaveArea.setBorder(BorderFactory.createTitledBorder("Palavras-chave encontradas"));

        // Seletor para alternar visualização (ALTERADO para incluir nova opção)
        seletorArvore = new JComboBox<>(new String[] {"Desenho", "Texto", "Palavras-chave"});
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
        cardPanel.add(new JScrollPane(palavrasChaveArea), "Palavras-chave"); // NOVA aba
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

    // MÉTODOS IMPLEMENTADOS PARA CORRIGIR OS ERROS:

    // Inicializa os estilos para syntax highlighting
    private void initializeStyles() {
        keywordStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(keywordStyle, new Color(0, 0, 255)); // Azul
        StyleConstants.setBold(keywordStyle, true);

        operatorStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(operatorStyle, new Color(128, 0, 128)); // Roxo

        numberStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(numberStyle, new Color(255, 0, 0)); // Vermelho

        stringStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(stringStyle, new Color(0, 128, 0)); // Verde

        commentStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(commentStyle, new Color(128, 128, 128)); // Cinza
        StyleConstants.setItalic(commentStyle, true);

        normalStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(normalStyle, Color.BLACK);

        // Inicializa o highlighter de erro
        errorHighlighter = new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 200, 200));
    }

    // Agenda o highlighting para evitar execução excessiva
    private void scheduleHighlighting() {
        if (highlightTimer.isRunning()) {
            highlightTimer.restart();
        } else {
            highlightTimer.start();
        }
    }

    // Aplica syntax highlighting preservando a posição do cursor
    private void applySyntaxHighlightingWithCursor() {
        updatingHighlight = true;
        int caretPos = editor.getCaretPosition();
        
        try {
            StyledDocument doc = editor.getStyledDocument();
            String text = doc.getText(0, doc.getLength());
            
            // Remove todos os estilos
            doc.setCharacterAttributes(0, doc.getLength(), normalStyle, true);
            
            // Aplica highlighting para palavras-chave
            for (String keyword : KEYWORDS) {
                Pattern pattern = Pattern.compile("\\b" + keyword + "\\b");
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), keywordStyle, false);
                }
            }
            
            // Aplica highlighting para números
            Pattern numberPattern = Pattern.compile("\\b\\d+(\\.\\d+)?\\b");
            Matcher numberMatcher = numberPattern.matcher(text);
            while (numberMatcher.find()) {
                doc.setCharacterAttributes(numberMatcher.start(), numberMatcher.end() - numberMatcher.start(), numberStyle, false);
            }
            
            // Aplica highlighting para strings
            Pattern stringPattern = Pattern.compile("\"[^\"]*\"");
            Matcher stringMatcher = stringPattern.matcher(text);
            while (stringMatcher.find()) {
                doc.setCharacterAttributes(stringMatcher.start(), stringMatcher.end() - stringMatcher.start(), stringStyle, false);
            }
            
            // Aplica highlighting para comentários
            Pattern commentPattern = Pattern.compile("//.*|/\\*.*?\\*/");
            Matcher commentMatcher = commentPattern.matcher(text);
            while (commentMatcher.find()) {
                doc.setCharacterAttributes(commentMatcher.start(), commentMatcher.end() - commentMatcher.start(), commentStyle, false);
            }
            
            // Aplica highlighting para operadores
            for (String operator : OPERATORS) {
                Pattern pattern = Pattern.compile(operator);
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), operatorStyle, false);
                }
            }
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        } finally {
            // Restaura a posição do cursor
            try {
                editor.setCaretPosition(Math.min(caretPos, editor.getDocument().getLength()));
            } catch (Exception e) {
                // Ignora erros de posição do cursor
            }
            updatingHighlight = false;
        }
    }

    // Atualiza a numeração de linhas
    private void updateLines() {
        SwingUtilities.invokeLater(() -> {
            try {
                String text = editor.getText();
                int lineCount = text.split("\n", -1).length;
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= lineCount; i++) {
                    sb.append(i).append("\n");
                }
                lines.setText(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Atualiza a lista de palavras-chave encontradas
    private void updatePalavrasChave() {
        try {
            String text = editor.getText();
            Set<String> encontradas = new HashSet<>();
            
            for (String keyword : KEYWORDS) {
                Pattern pattern = Pattern.compile("\\b" + keyword + "\\b");
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    encontradas.add(keyword);
                }
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Palavras-chave encontradas no código:\n\n");
            encontradas.stream().sorted().forEach(palavra -> sb.append("• ").append(palavra).append("\n"));
            
            if (encontradas.isEmpty()) {
                sb.append("Nenhuma palavra-chave encontrada.");
            }
            
            palavrasChaveArea.setText(sb.toString());
        } catch (Exception e) {
            palavrasChaveArea.setText("Erro ao analisar palavras-chave: " + e.getMessage());
        }
    }

    // Alterna entre as visualizações da árvore sintática
    private void alternarVisualizacaoArvore() {
        JPanel parent = (JPanel) seletorArvore.getParent().getComponent(1);
        CardLayout layout = (CardLayout) parent.getLayout();
        String selected = (String) seletorArvore.getSelectedItem();
        layout.show(parent, selected);
        
        // Se selecionou palavras-chave, atualiza a visualização
        if ("Palavras-chave".equals(selected)) {
            updatePalavrasChave();
        }
    }

    // Compila o código fonte
    private void compilar() {
        output.setText("Compilando...\n");
        
        try {
            String codigo = editor.getText();
            if (codigo.trim().isEmpty()) {
                output.append("Erro: Código fonte vazio!\n");
                return;
            }
            
            // Aqui você implementaria a lógica de compilação específica
            // Por exemplo, usando o parser:
            if (parser != null) {
                // Lógica de compilação usando o parser
                output.append("Compilação bem-sucedida!\n");
            } else {
                output.append("Parser não inicializado!\n");
            }
            
        } catch (Exception e) {
            output.append("Erro de compilação: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    // Cria um novo arquivo
    private void novoArquivo() {
        if (confirmarSalvarAntes()) {
            editor.setText("");
            arquivoAtual = null;
            setTitle("Compilador - Editor e Visualizador de Árvore");
            output.setText("Novo arquivo criado.\n");
        }
    }

    // Abre um arquivo existente
    private void abrirArquivo() {
        if (confirmarSalvarAntes()) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Arquivos C+- (*.cm)", "cm"));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File arquivo = fileChooser.getSelectedFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
                    StringBuilder sb = new StringBuilder();
                    String linha;
                    while ((linha = reader.readLine()) != null) {
                        sb.append(linha).append("\n");
                    }
                    editor.setText(sb.toString());
                    arquivoAtual = arquivo;
                    setTitle("Compilador - " + arquivo.getName());
                    output.setText("Arquivo aberto: " + arquivo.getName() + "\n");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, 
                        "Erro ao abrir arquivo: " + e.getMessage(), 
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Salva o arquivo atual ou abre dialog para salvar como
    private void salvarArquivo() {
        if (arquivoAtual != null) {
            salvarArquivoEm(arquivoAtual);
        } else {
            salvarArquivoComo();
        }
    }

    // Abre dialog para salvar como
    private void salvarArquivoComo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Arquivos C+- (*.cm)", "cm"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            if (!arquivo.getName().endsWith(".cm")) {
                arquivo = new File(arquivo.getAbsolutePath() + ".cm");
            }
            salvarArquivoEm(arquivo);
        }
    }

    // Salva o conteúdo no arquivo especificado
    private void salvarArquivoEm(File arquivo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(arquivo))) {
            writer.print(editor.getText());
            arquivoAtual = arquivo;
            setTitle("Compilador - " + arquivo.getName());
            output.setText("Arquivo salvo: " + arquivo.getName() + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao salvar arquivo: " + e.getMessage(), 
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Confirma se deve salvar antes de executar uma ação
    private boolean confirmarSalvarAntes() {
        // Implementação simplificada - sempre retorna true
        // Você pode implementar uma lógica mais sofisticada aqui
        return true;
    }

    // Classe interna para o painel da árvore sintática (stub)
    private class ArvoreSintaticaPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.drawString("Visualização da árvore sintática", 10, 20);
            g.drawString("(Implementar desenho da árvore)", 10, 40);
        }
    }

    // Método main para executar a aplicação
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CompiladorGUI().setVisible(true);
        });
    }
}