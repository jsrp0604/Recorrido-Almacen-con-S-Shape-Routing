import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Clase AlmacenPanel extiende JPanel y se encarga de representar gráficamente el almacén.
 * Dibuja el almacén, los ítems del pedido, el punto inicial, y el recorrido en el panel.
 */
public class AlmacenPanel extends JPanel {
    private Almacen almacen; // Referencia al objeto Almacen que contiene la disposición de ítems
    private List<Item> itemsPedido; // Lista de ítems que forman parte del pedido actual
    private List<Point> recorrido; // Lista de puntos que representan el recorrido en el almacén
    private Point puntoInicial; // Punto inicial desde donde comienza el recorrido

    /**
     * Constructor de la clase AlmacenPanel.
     * Inicializa el panel con el almacén, el pedido, y el recorrido.
     * 
     * @param almacen Objeto Almacen que representa la disposición de los ítems.
     */
    public AlmacenPanel(Almacen almacen) {
        this.almacen = almacen;
        this.itemsPedido = new ArrayList<>();
        this.recorrido = new ArrayList<>();
        this.puntoInicial = new Point(1, 9); // Ajusta la ubicación inicial según sea necesario
        setPreferredSize(new Dimension(800, 600)); // Define el tamaño preferido del panel
    }

    /**
     * Método para establecer la lista de ítems del pedido.
     * Limpia el recorrido previo y solicita la redibujado del panel.
     * 
     * @param itemsPedido Lista de ítems que forman el pedido.
     */
    public void setItemsPedido(List<Item> itemsPedido) {
        this.itemsPedido = itemsPedido;
        this.recorrido.clear(); // Limpia el recorrido previo
        repaint(); // Solicita la redibujado del panel
    }

    /**
     * Método para establecer el recorrido a ser dibujado en el panel.
     * Solicita la redibujado del panel.
     * 
     * @param recorrido Lista de puntos que representan el recorrido en el almacén.
     */
    public void setRecorrido(List<Point> recorrido) {
        this.recorrido = recorrido;
        repaint(); // Solicita la redibujado del panel
    }

    /**
     * Método para establecer el punto inicial del recorrido.
     * Solicita la redibujado del panel.
     * 
     * @param puntoInicial Punto inicial desde donde comienza el recorrido.
     */
    public void setPuntoInicial(Point puntoInicial) {
        this.puntoInicial = puntoInicial;
        repaint(); // Solicita la redibujado del panel
    }

    /**
     * Método sobrescrito de JPanel para pintar el componente.
     * Este método dibuja el almacén, los ítems del pedido, el punto inicial y el recorrido.
     * 
     * @param g Objeto Graphics que se utiliza para pintar en el componente.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g; // Convierte el objeto Graphics a Graphics2D para obtener más funcionalidades
        int cellWidth = getWidth() / almacen.cols;  // Calcula el ancho de cada celda
        int cellHeight = getHeight() / almacen.rows; // Calcula la altura de cada celda

        // Dibujar el almacén, cada celda representa una ubicación
        for (int r = 0; r < almacen.rows; r++) {
            for (int c = 0; c < almacen.cols; c++) {
                int x = c * cellWidth;
                int y = r * cellHeight;
                if (almacen.ubicaciones[r][c] != 0) { // Si hay un ítem en la ubicación
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.fillRect(x, y, cellWidth, cellHeight); // Pinta la celda en gris claro
                    g2d.setColor(Color.BLACK);
                }
                g2d.drawRect(x, y, cellWidth, cellHeight); // Dibuja los bordes de la celda
                int itemId = almacen.ubicaciones[r][c];
                if (itemId != 0) { // Si hay un ítem en la ubicación, dibuja su ID
                    g2d.drawString(String.valueOf(itemId), x + cellWidth / 2 - 5, y + cellHeight / 2 + 5);
                }
            }
        }

        // Marcar ítems del pedido en color naranja
        for (Item item : itemsPedido) {
            int x = item.col * cellWidth;
            int y = item.row * cellHeight;
            g2d.setColor(Color.ORANGE);
            g2d.fillRect(x, y, cellWidth, cellHeight); // Pinta la celda del ítem en naranja
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, cellWidth, cellHeight); // Dibuja los bordes de la celda
            g2d.drawString(String.valueOf(item.id), x + cellWidth / 2 - 5, y + cellHeight / 2 + 5);
        }

        // Dibujar el punto inicial en verde, ocupando dos cuadros
        g2d.setColor(Color.GREEN);
        int initialX = puntoInicial.x * cellWidth;
        int initialY = puntoInicial.y * cellHeight;
        g2d.fillRect(initialX, initialY, cellWidth * 2, cellHeight * 2); // Pinta un área 2x2 en verde
        g2d.setColor(Color.BLACK);
        g2d.drawRect(initialX, initialY, cellWidth * 2, cellHeight * 2); // Dibuja los bordes del área

        // Dibujar el recorrido en rojo, conectando los puntos en orden
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2)); // Define el grosor de la línea
        for (int i = 0; i < recorrido.size() - 1; i++) {
            Point p1 = recorrido.get(i);
            Point p2 = recorrido.get(i + 1);
            g2d.drawLine(p1.x * cellWidth + cellWidth, p1.y * cellHeight + cellHeight / 2,
                       p2.x * cellWidth + cellWidth, p2.y * cellHeight + cellHeight / 2);
        }
    }
}
