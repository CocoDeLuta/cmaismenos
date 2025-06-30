package gui;

import java.awt.*;
import javax.swing.*;

public class OutputPanel extends JPanel 
{
    private JTextArea outputArea;

    public OutputPanel() 
    {
        setLayout(new BorderLayout());
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
    }

    public void setOutputText(String text) 
    {
        outputArea.setText(text);
    }

    public JTextArea getTextArea() 
    {
        return outputArea;
    }
}
