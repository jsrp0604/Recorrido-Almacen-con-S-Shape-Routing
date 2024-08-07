import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class AlmacenPanel extends JPanel {
    private Almacen almacen;
    private List<Item> itemsPedido;
    private List<Point> recorrido;
    private Point puntoInicial;

    public AlmacenPanel(Almacen almacen) {
        this.almacen = almacen;
        this.itemsPedido = new ArrayList<>();
        this.recorrido = new ArrayList<>();
        this.puntoInicial = new Point(1, 9); // Ajusta la ubicación inicial según sea necesario
        setPreferredSize(new Dimension(800, 600));
    }

    public void setItemsPedido(List<Item> itemsPedido) {
        this.itemsPedido = itemsPedido;
        this.recorrido.clear();
        repaint();
    }

    public void setRecorrido(List<Point> recorrido) {
        this.recorrido = recorrido;
        repaint();
    }

    public void setPuntoInicial(Point puntoInicial) {
        this.puntoInicial = puntoInicial;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int cellWidth = getWidth() / almacen.cols;  
        int cellHeight = getHeight() / almacen.rows; 

        // Dibujar el almacén
        for (int r = 0; r < almacen.rows; r++) {
            for (int c = 0; c < almacen.cols; c++) {
                int x = c * cellWidth;
                int y = r * cellHeight;
                if (almacen.ubicaciones[r][c] != 0) {
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.fillRect(x, y, cellWidth, cellHeight);
                    g2d.setColor(Color.BLACK);
                }
                g2d.drawRect(x, y, cellWidth, cellHeight);
                int itemId = almacen.ubicaciones[r][c];
                if (itemId != 0) {
                    g2d.drawString(String.valueOf(itemId), x + cellWidth / 2 - 5, y + cellHeight / 2 + 5);
                }
            }
        }

        // Marcar ítems del pedido en otro color
        for (Item item : itemsPedido) {
            int x = item.col * cellWidth;
            int y = item.row * cellHeight;
            g2d.setColor(Color.ORANGE);
            g2d.fillRect(x, y, cellWidth, cellHeight);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, cellWidth, cellHeight);
            g2d.drawString(String.valueOf(item.id), x + cellWidth / 2 - 5, y + cellHeight / 2 + 5);
        }

        // Dibujar punto inicial en verde ocupando dos cuadros
        g2d.setColor(Color.GREEN);
        int initialX = puntoInicial.x * cellWidth;
        int initialY = puntoInicial.y * cellHeight;
        g2d.fillRect(initialX, initialY, cellWidth * 2, cellHeight * 2);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(initialX, initialY, cellWidth * 2, cellHeight * 2);

        // Dibujar recorrido en rojo
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2)); // Grosor de la línea
        for (int i = 0; i < recorrido.size() - 1; i++) {
            Point p1 = recorrido.get(i);
            Point p2 = recorrido.get(i + 1);
            g2d.drawLine(p1.x * cellWidth + cellWidth, p1.y * cellHeight + cellHeight / 2,
                       p2.x * cellWidth + cellWidth, p2.y * cellHeight + cellHeight / 2);
        }
    }
}