import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SimuladorAlmacen extends JFrame {
    private static Almacen almacen = new Almacen();
    private JTextField itemsField;
    private JTextArea outputArea;
    private List<Pedido> pedidos;
    private AlmacenPanel almacenPanel;
    private Timer timer;
    private List<Point> recorrido;

    public SimuladorAlmacen() {
        setTitle("Simulador de Almacén");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        controlPanel.add(new JLabel("Número de Ítems:"));
        itemsField = new JTextField(5);
        controlPanel.add(itemsField);

        JButton ejecutarButton = new JButton("Ejecutar");
        ejecutarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generarYSimularPedido();
            }
        });
        controlPanel.add(ejecutarButton);

        JButton pararButton = new JButton("Parar");
        controlPanel.add(pararButton);

        JButton finalizarButton = new JButton("Finalizar");
        finalizarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        controlPanel.add(finalizarButton);

        add(controlPanel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setPreferredSize(new Dimension(300, 600));
        add(outputScrollPane, BorderLayout.EAST);

        almacenPanel = new AlmacenPanel(almacen);
        add(almacenPanel, BorderLayout.CENTER);

        pedidos = new ArrayList<>();
    }

    private void generarYSimularPedido() {
        int numItems;
        try {
            numItems = Integer.parseInt(itemsField.getText());
            if (numItems < 5 || numItems > 10) {
                outputArea.setText("El número de ítems debe ser entre 5 y 10.");
                return;
            }
        } catch (NumberFormatException e) {
            outputArea.setText("Número de ítems inválido.");
            return;
        }

        Pedido pedido = generarPedido(numItems);
        pedidos.add(pedido);
        mostrarPedido(pedido);
        actualizarPanel(pedido);
        iniciarRecorrido(pedido);
    }

    private Pedido generarPedido(int numItems) {
        List<Item> items = new ArrayList<>();
        Random random = new Random();
        for (int j = 0; j < numItems; j++) {
            int itemId = 1 + random.nextInt(60);
            items.add(almacen.getItem(itemId));
        }
        return new Pedido(items);
    }

    private void mostrarPedido(Pedido pedido) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("resultados.txt", true))) {
            int pedidoNum = pedidos.size();
            outputArea.append("Pedido p" + pedidoNum + "\n");
            writer.write("pedido p" + pedidoNum);
            writer.newLine();
            for (Item item : pedido.items) {
                outputArea.append("Item " + item.id + "\n");
                writer.write("item " + item.id);
                writer.newLine();
            }
            outputArea.append("\n");
        } catch (IOException e) {
            outputArea.setText("Error al escribir el archivo de resultados.");
        }
    }

    private void actualizarPanel(Pedido pedido) {
        almacenPanel.setItemsPedido(pedido.items);
    }

    private void iniciarRecorrido(Pedido pedido) {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        // Crear el recorrido en forma de S
        List<Point> puntosRecorrido = calcularRecorrido(pedido);

        // Mostrar el recorrido en el panel
        recorrido = new ArrayList<>(puntosRecorrido);
        almacenPanel.setRecorrido(recorrido);

        // Crear un temporizador para animar el recorrido
        timer = new Timer(1000, new ActionListener() {
            int index = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (index < puntosRecorrido.size() - 1) {
                    index++;
                    recorrido = new ArrayList<>(puntosRecorrido.subList(0, index + 1));
                    almacenPanel.setRecorrido(recorrido);
                } else {
                    ((Timer) e.getSource()).stop();
                    mostrarResultadoRecorrido(puntosRecorrido);
                }
            }
        });
        timer.start();
    }

    private List<Point> calcularRecorrido(Pedido pedido) {
        List<Point> puntosRecorrido = new ArrayList<>();
        Point puntoInicial = new Point(1, 9);
        puntosRecorrido.add(puntoInicial);

        // Ordenar los ítems del pedido según la distancia desde el punto inicial
        LinkedList<Item> itemsOrdenados = new LinkedList<>(pedido.items);
        itemsOrdenados.sort((i1, i2) -> {
            return Integer.compare(i1.id, i2.id);
        });

        String[] numsId = new String[10];
        int intId = 0;

        for (Item item : itemsOrdenados) {
            String stringId = "n" + item.id;
            numsId[intId] = stringId;
            intId++;
        }
        System.out.println("Lista Inicial de items ordenados");
        for (String string : numsId) {
            System.out.println(string);
        }
        
        // Agregar los puntos del recorrido en forma de S por los pasillos
        Point currentPoint = puntoInicial;
        boolean arribaPasillo = false;
        Item ultItem;
        int limite = 12;
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algo Arreglo

        // mover simepre un punto hacia arriba
        currentPoint = new Point(currentPoint.x, currentPoint.y - 1);
        puntosRecorrido.add(currentPoint);

        while (itemsOrdenados.size() > 0) {
            ultItem = itemsOrdenados.pop();
            System.out.println("Ult Item n" + ultItem.id +"\tfila n" + ultItem.row + "\tcolumna n" + ultItem.col);

            if (ultItem.id <= limite)    {
                if (itemsOrdenados.size() == 0)
                    itemsOrdenados.add(ultItem);

                if (itemsOrdenados.peek().id > limite)  {
                    // esta en pasillo y no hay mas items
                    
                    System.out.println("No hay más items en pasillo: Variable arribaPasillo " + arribaPasillo);
                    for (int i = 0; i <= 6; i++)  {
                        if (!arribaPasillo) {
                            currentPoint = new Point(currentPoint.x, currentPoint.y - 1);                            
                        }
                        else    {
                            currentPoint = new Point(currentPoint.x, currentPoint.y + 1);
                        }
                        puntosRecorrido.add(currentPoint);        
                    }

                    arribaPasillo = !arribaPasillo;
                }

                else if (itemsOrdenados.getLast().id <= limite)    {
                    // todos los items restantes se encuentran dentro del pasillo
                    
                    if (arribaPasillo)  {
                        System.out.println("Ultimo pasillo: movimiento hacia abajo\n");
                        for (int i = 0; i <= 7; i++)    {
                            currentPoint = new Point(currentPoint.x, currentPoint.y + 1);
                            puntosRecorrido.add(currentPoint);
                        }   
                        
                        itemsOrdenados.clear();      
                    }

                    else    {
                        System.out.println("Ultimo pasillo: movimiento hacia arriba\n");
                        int filaUltItem = 8; // el eje y incrementa su valor hacia abajo
                        
                        itemsOrdenados.addFirst(ultItem);
                        for (Item item : itemsOrdenados) {
                            System.out.println("Busqueda de Item con fila mayor");
                            System.out.println("Item n" + item.id +"\tfila n" + item.row);
                            if (item.row < filaUltItem)
                                filaUltItem = item.row;
                        }

                        System.out.println("Fila mayor: " + filaUltItem);

                        for (int i = 8; i > filaUltItem; i--) {
                            currentPoint = new Point(currentPoint.x, currentPoint.y - 1);
                            puntosRecorrido.add(currentPoint);
                        }

                        // se mueve una unidad a la derecha para hacer giro en u
                        currentPoint = new Point(currentPoint.x + 1, currentPoint.y);
                        puntosRecorrido.add(currentPoint);

                        while (currentPoint.y != puntoInicial.y) {
                            currentPoint = new Point(currentPoint.x, currentPoint.y + 1);
                            puntosRecorrido.add(currentPoint);
                        }

                        itemsOrdenados.clear();
                    }

                    // falta hacer giro en u

                    while (currentPoint.x != puntoInicial.x) {
                        currentPoint = new Point(currentPoint.x - 1, currentPoint.y);
                        puntosRecorrido.add(currentPoint);
                    }
                }
            }

            // movimiento derecha
            else if (ultItem.id > limite) {
                System.out.println("Se movió a la derecha");
                for (int i = 0; i <= 3; i++)    {
                    currentPoint = new Point(currentPoint.x + 1, currentPoint.y);
                    puntosRecorrido.add(currentPoint);
                }

                itemsOrdenados.addFirst(ultItem);
                limite = limite + 12;
            }

        }

        return puntosRecorrido;
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    private void mostrarResultadoRecorrido(List<Point> puntosRecorrido) {
        int distanciaTotal = puntosRecorrido.size() - 1;
        int tiempoTotal = distanciaTotal;  // dado que la velocidad es 1 m/s

        int pedidoNum = pedidos.size();
        outputArea.append("Resultado del recorrido para pedido p" + pedidoNum + ":\n");
        outputArea.append("Distancia total: " + distanciaTotal + " cuadros\n");
        outputArea.append("Tiempo total: " + tiempoTotal + " segundos\n\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SimuladorAlmacen().setVisible(true);
            }
        });
    }
}