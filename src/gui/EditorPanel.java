// Painel do editor de código-fonte.
// Possui área de edição com destaque de sintaxe, numeração de linhas e ajuste de fonte.
package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

public class EditorPanel extends JPanel {
    // Tamanho atual da fonte do editor
    private int fontSize = 14;

    // Área principal de edição de código
    private JTextPane editor;

    // Área para exibir a numeração das linhas
    private JTextArea lines;

    // Flag para evitar recursão no highlight
    private boolean isHighlighting = false;

    // Listener para atualizar linhas e sintaxe ao editar
    private javax.swing.event.DocumentListener docListener;

    // Construtor: inicializa editor, linhas e listeners
    public EditorPanel() 
    {
        setLayout(new BorderLayout());
        editor = new JTextPane();
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
        lines = new JTextArea("1\n");
        lines.setEditable(false);
        lines.setBackground(new Color(230,230,230));
        lines.setFont(editor.getFont());
        lines.setMargin(new Insets(0, 8, 0, 8));
        lines.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.GRAY));
        lines.setMinimumSize(new Dimension(40, 0));
        lines.setPreferredSize(new Dimension(48, 0));

        // Listener para atualizar numeração e sintaxe ao editar
        docListener = new javax.swing.event.DocumentListener() 
        {
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) 
            {
                updateLines();
                SwingUtilities.invokeLater(() -> highlightSyntax());
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) 
            {
                updateLines();
                SwingUtilities.invokeLater(() -> highlightSyntax());
            }
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) 
            {
                updateLines();
                SwingUtilities.invokeLater(() -> highlightSyntax());
            }
        };
        editor.getDocument().addDocumentListener(docListener);

        // Adiciona editor e linhas ao painel com barra de rolagem
        JScrollPane scroll = new JScrollPane(editor);
        scroll.setRowHeaderView(lines);
        add(scroll, BorderLayout.CENTER);
        highlightSyntax(); // Aplica highlight inicial
    }

    // Retorna o componente de edição (JTextPane)
    public JTextPane getEditor() { return editor; }
    // Retorna o texto do editor
    public String getText() { return editor.getText(); }
    // Define o texto do editor
    public void setText(String text) { editor.setText(text); }

    // Aplica destaque de sintaxe para palavras-chave e símbolos
    public void highlightSyntax() 
    {
        if (isHighlighting) return;
        isHighlighting = true;
        editor.getDocument().removeDocumentListener(docListener);
        // Lista de palavras-chave da linguagem
        String[] keywords = {
            "nascoxas", "tipoisso", "euacho", "poucacoisa", "falouedisse", "maisoumenos",
            "sepa", "acontece", "vainafe", "acho"
        };

        // Ordena por tamanho decrescente para evitar highlight parcial (ex: 'euacho' antes de 'acho')
        java.util.Arrays.sort(keywords, (a, b) -> Integer.compare(b.length(), a.length()));

        // Lista de símbolos da linguagem
        String[] symbols = 
        {
            ";", ",", "(", ")", "=", "==", "!=", "<=", ">=", "<", ">", "++", "--", "+=", "-=", "+", "-", "*", "/"
        };
        StyledDocument doc = editor.getStyledDocument();
        Style defaultStyle = editor.getStyle(StyleContext.DEFAULT_STYLE);
        Style keywordStyle = doc.getStyle("KEYWORD");
        Style symbolStyle = doc.getStyle("SYMBOL");

        // Cria estilos se ainda não existirem
        if (keywordStyle == null) 
        {
            keywordStyle = doc.addStyle("KEYWORD", null);
            StyleConstants.setForeground(keywordStyle, new Color(0, 0, 200));
            StyleConstants.setBold(keywordStyle, true);
        }
        if (symbolStyle == null) 
        {
            symbolStyle = doc.addStyle("SYMBOL", null);
            StyleConstants.setForeground(symbolStyle, new Color(150, 0, 150));
        }
        int caret = editor.getCaretPosition();
        String text = editor.getText();

        // Aplica estilo padrão em todo o texto
        doc.setCharacterAttributes(0, text.length(), defaultStyle, true);
        // Destaca palavras-chave
        for (String kw : keywords) 
        {
            int idx = 0;
            while ((idx = text.indexOf(kw, idx)) != -1) 
            {
                boolean before = idx == 0 || !Character.isJavaIdentifierPart(text.charAt(idx - 1));
                boolean after = (idx + kw.length() == text.length()) || !Character.isJavaIdentifierPart(text.charAt(idx + kw.length()));
                if (before && after) {
                    doc.setCharacterAttributes(idx, kw.length(), keywordStyle, true);
                }
                idx += kw.length();
            }
        }
        // Destaca símbolos
        for (String sym : symbols) 
        {
            int idx = 0;
            while ((idx = text.indexOf(sym, idx)) != -1) 
            {
                doc.setCharacterAttributes(idx, sym.length(), symbolStyle, true);
                idx += sym.length();
            }
        }

        // Mantém posição do cursor
        editor.setCaretPosition(Math.min(caret, text.length()));
        isHighlighting = false;
        editor.getDocument().addDocumentListener(docListener);
    }

    // Aumenta o tamanho da fonte do editor
    public void aumentarFonte() 
    {
        fontSize = Math.min(fontSize + 2, 72);
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
        lines.setFont(editor.getFont());
        updateLines();
    }

    // Diminui o tamanho da fonte do editor
    public void diminuirFonte() 
    {
        fontSize = Math.max(fontSize - 2, 8);
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
        lines.setFont(editor.getFont());
        updateLines();
    }

    // Atualiza a numeração das linhas conforme o texto do editor
    public void updateLines() 
    {
        int linesCount = editor.getDocument().getDefaultRootElement().getElementCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= linesCount; i++) 
        {
            sb.append(i).append("\n");
        }
        lines.setText(sb.toString());
    }
}
