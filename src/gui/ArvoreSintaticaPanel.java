package gui;

import compilador.SimpleNode;
import java.awt.*;
import javax.swing.*;

// Painel Swing para desenhar a árvore sintática de forma gráfica
public class ArvoreSintaticaPanel extends JPanel 
{
    // Nó raiz da árvore sintática
    private SimpleNode raiz;
    // Raio dos nós desenhados
    private final int nodeRadius = 24;
    // Espaçamento vertical entre níveis
    private final int vGap = 56;
    // Margem horizontal do painel
    private final int hGap = 40;
    // Espaçamento mínimo entre irmãos
    private final int minSiblingGap = 36;

    // Estrutura auxiliar para armazenar largura e posição dos nós
    // Estrutura auxiliar para armazenar informações de cada nó durante o cálculo de layout
    private static class NodeInfo 
    {
        int x, y, width;
        SimpleNode node;
        NodeInfo[] children;
        NodeInfo(SimpleNode node, int y) 
        {
            this.node = node;
            this.y = y;
        }
    }

    // Define a raiz da árvore e atualiza o painel
    public void setRaiz(SimpleNode raiz) 
    {
        this.raiz = raiz;
        revalidate();
        repaint();
    }

    // Calcula o tamanho preferido do painel para caber toda a árvore
    @Override
    public Dimension getPreferredSize() 
    {
        if (raiz == null) return new Dimension(320, 240);
        NodeInfo info = buildTreeInfo(raiz, 0);
        int width = info.width + 2 * hGap;
        int height = getTreeHeight(raiz) * vGap + 2 * nodeRadius;
        return new Dimension(width, height);
    }

    // Desenha a árvore sintática a partir da raiz
    @Override
    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        if (raiz != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            NodeInfo info = buildTreeInfo(raiz, 0);
            int startX = hGap + info.width / 2;
            drawNode(g2, info, startX, vGap);
        }
    }

    // Calcula largura de cada subárvore e armazena as infos
    // Calcula largura de cada subárvore recursivamente
    private NodeInfo buildTreeInfo(SimpleNode node, int depth) 
    {
        NodeInfo info = new NodeInfo(node, (depth+1) * vGap);
        int n = node.jjtGetNumChildren();
        info.children = new NodeInfo[n];
        int totalWidth = 0;
        int[] childWidths = new int[n];
        if (n > 0) 
        {
            for (int i = 0; i < n; i++) 
            {
                info.children[i] = buildTreeInfo((SimpleNode) node.jjtGetChild(i), depth+1);
                childWidths[i] = info.children[i].width;
            }
            // Soma as larguras dos filhos e adiciona espaçamento mínimo entre irmãos
            totalWidth = childWidths[0];
            for (int i = 1; i < n; i++) 
            {
                totalWidth += minSiblingGap + childWidths[i];
            }
        }

        String label = node.toString();
        int labelWidth = getFontMetrics(getFont()).stringWidth(label) + 12;
        info.width = Math.max(labelWidth + hGap, totalWidth > 0 ? totalWidth : nodeRadius + hGap);
        return info;
    }

    // Desenha recursivamente usando as infos calculadas
    // Desenha recursivamente cada nó e suas conexões
    private void drawNode(Graphics2D g2, NodeInfo info, int x, int y) 
    {
        String label = info.node.toString();
        int nodeW = getFontMetrics(getFont()).stringWidth(label) + 12;
        int nodeH = nodeRadius;
        // Desenha o nó
        g2.setColor(new Color(200, 220, 255));
        g2.fillRoundRect(x - nodeW/2, y - nodeH/2, nodeW, nodeH, 12, 12);
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(x - nodeW/2, y - nodeH/2, nodeW, nodeH, 12, 12);
        g2.drawString(label, x - getFontMetrics(getFont()).stringWidth(label)/2, y + 5);

        // Desenha filhos
        int n = info.children.length;
        if (n > 0) 
        {
            int totalWidth = 0;
            for (int i = 0; i < n; i++) totalWidth += info.children[i].width;
            totalWidth += (n - 1) * minSiblingGap;
            int childX = x - totalWidth/2;
            for (int i = 0; i < n; i++) 
            {
                int cx = childX + info.children[i].width/2;
                int cy = y + vGap;
                // Linha até o filho
                g2.drawLine(x, y + nodeH/2, cx, cy - nodeH/2);
                drawNode(g2, info.children[i], cx, cy);
                childX += info.children[i].width + minSiblingGap;
            }
        }
    }

    // Calcula altura da árvore
    // Calcula a altura (profundidade) da árvore
    private int getTreeHeight(SimpleNode node) 
    {
        int n = node.jjtGetNumChildren();
        int max = 0;
        for (int i = 0; i < n; i++) 
        {
            max = Math.max(max, getTreeHeight((SimpleNode) node.jjtGetChild(i)));
        }
        return 1 + max;
    }
}

