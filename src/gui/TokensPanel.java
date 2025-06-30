package gui;

import java.awt.*;
import javax.swing.*;

public class TokensPanel extends JPanel 
{
    private JTextArea tokensArea;

    public TokensPanel() 
    {
        setLayout(new BorderLayout());
        tokensArea = new JTextArea();
        tokensArea.setEditable(false);
        tokensArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(tokensArea), BorderLayout.CENTER);
    }

    public void setTokensText(String text) 
    {
        tokensArea.setText(text);
    }

    public JTextArea getTextArea() 
    {
        return tokensArea;
    }
}
