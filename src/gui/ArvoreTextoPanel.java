
// Painel para exibir a árvore sintática em formato textual.
// Mostra a estrutura da árvore como texto puro, útil para depuração e análise.
package gui;

import java.awt.*;
import javax.swing.*;


public class ArvoreTextoPanel extends JPanel {
    // Área de texto onde a árvore sintática será exibida
    private JTextArea arvoreTextoArea;


    // Construtor: inicializa o painel e a área de texto
    public ArvoreTextoPanel() {
        setLayout(new BorderLayout());
        arvoreTextoArea = new JTextArea();
        arvoreTextoArea.setEditable(false); // Somente leitura
        arvoreTextoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); // Fonte monoespaçada para melhor visualização
        add(new JScrollPane(arvoreTextoArea), BorderLayout.CENTER); // Adiciona barra de rolagem
    }


    // Atualiza o texto exibido na área de árvore sintática
    public void setArvoreText(String text) {
        arvoreTextoArea.setText(text);
    }


    // Retorna a referência da área de texto (caso necessário)
    public JTextArea getTextArea() {
        return arvoreTextoArea;
    }
}
