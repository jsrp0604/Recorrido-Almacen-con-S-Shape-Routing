import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimuladorAlmacen extends JFrame {
    private static Almacen almacen = new Almacen();
    private JTextField pedidosField; // Campo de texto para el número de pedidos
    private JTextArea outputArea; // Área de texto para mostrar resultados
    private List<Pedido> pedidos; // Lista de pedidos
    private AlmacenPanel almacenPanel; // Panel gráfico del almacén
    private Timer timer; // Temporizador para animar el recorrido
    private List<Point> recorrido; // Lista de puntos del recorrido
    private Lock lock; // Lock para sincronización de hilos
    private ReentrantLock recorridoLock = new ReentrantLock();
    private boolean paused = false;


    public SimuladorAlmacen() {
        // Configuración inicial del JFrame
        setTitle("Simulador de Almacén");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel de control superior con los botones y campo de texto
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        // Etiqueta y campo de texto para ingresar el número de pedidos
        controlPanel.add(new JLabel("Número de Pedidos:"));
        pedidosField = new JTextField(5);
        controlPanel.add(pedidosField);

        // Botón "Ejecutar" para generar y simular los pedidos
        JButton ejecutarButton = new JButton("Ejecutar");
        ejecutarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generarYSimularPedido();
            }
        });
        controlPanel.add(ejecutarButton);

        // Botón "Parar" 
        JButton pararButton = new JButton("Parar");
        pararButton.addActionListener(e -> {
            if (paused) {
                // Si está pausado, reanudar
                paused = false;
                pararButton.setText("Parar");
                resumeTimer(); 
            } else {
                // Si está en ejecución, pausar
                paused = true;
                pararButton.setText("Reanudar");
                pauseTimer(); 
            }
        });
        controlPanel.add(pararButton);

        // Botón "Finalizar" para cerrar el simulador
        JButton finalizarButton = new JButton("Finalizar");
        finalizarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        controlPanel.add(finalizarButton);

        // Añadir el panel de control en la parte superior del JFrame
        add(controlPanel, BorderLayout.NORTH);

        // Área de texto para mostrar la salida de los resultados
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setPreferredSize(new Dimension(300, 600));
        add(outputScrollPane, BorderLayout.EAST);

        // Panel del almacén donde se muestra gráficamente el almacén y el recorrido
        almacenPanel = new AlmacenPanel(almacen);
        add(almacenPanel, BorderLayout.CENTER);

        pedidos = new ArrayList<>(); // Inicialización de la lista de pedidos
    }

    // Métodos para pausar y reanudar el temporizador
    private void pauseTimer() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

    private void resumeTimer() {
        if (timer != null && !timer.isRunning()) {
            timer.start();
        }
    }

    /**
     * Función para generar y simular un pedido.
     * Se muestran los detalles e items del pedido y se ejecuta la funcion iniciar recorrido
     */
    private void generarYSimularPedido() {
        int numPedidos;
        try {
            numPedidos = Integer.parseInt(pedidosField.getText());
        } catch (NumberFormatException e) {
            outputArea.setText("Número de pedidos inválido.");
            return;
        }

        pedidos = generarPedidos(numPedidos);

        procesarPedidosSecuencialmente(pedidos);
    }

    private void procesarPedidosSecuencialmente(List<Pedido> pedidos) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                for (Pedido pedido : pedidos) {
                    // Actualizar la GUI desde el hilo de eventos del swing
                    try {
                        // Se inician los métodos del pedido pero se mantienen en espera a que terminen los anteriores
                        SwingUtilities.invokeAndWait(() -> { 
                            // Se muestran los detalles del pedido en el área de texto
                            outputArea.append("Pedido p" + pedido.getNumPedido() + "\n");
                            for (Item item : pedido.items) {
                                outputArea.append("Item " + item.id + "\n");
                            }
                            outputArea.append("\n"); 
                            almacenPanel.setItemsPedido(pedido.items);  // Se muestran los items del pedido en la gráfica del almacen
                        });
                    } catch (InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
    
                    // Se inicia el recorrido en segundo plano
                    CountDownLatch latch = new CountDownLatch(1);
                    SwingUtilities.invokeLater(() -> iniciarRecorrido(pedido, latch)); //
    
                    try {
                        latch.await(); // Espera a que la animación del recorrido termine con el latch
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
    
            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("Todos los pedidos han sido procesados.\n");
                });
            }
        }.execute();
    }

    /**
     * Función para generar un pedido con un número aleatorio de ítems.
     * @param numItems Número de ítems en el pedido.
     * @return Pedido generado.
     */
    private List<Pedido> generarPedidos(int numPedidos) {
        List<Pedido> pedidos = new ArrayList<>();
        List<Thread> hilos = new ArrayList<>();
        lock = new ReentrantLock(); // Bloqueo para concurrencia
        
        for (int i = 1; i <= numPedidos; i++) {
            final int pedidoNum = i;
            Thread hilo = new Thread(() -> {
                List<Item> items = new ArrayList<>();
                Random random = new Random();
                int numItems = 5 + random.nextInt(6);  // entre 5 y 10 ítems
                for (int j = 0; j < numItems; j++) {
                    int itemId = 1 + random.nextInt(60);
                    items.add(almacen.getItem(itemId));
                }
    
                Pedido pedido = new Pedido(items, pedidoNum);
    
                // Sincronizar el acceso a la lista de pedidos
                lock.lock();
                try {
                    pedidos.add(pedido);
                } finally {
                    lock.unlock(); 
                }
            });
            hilos.add(hilo);
            hilo.start();
        }
    
        // Esperar a que todos los hilos terminen
        for (Thread hilo : hilos) {
            try {
                hilo.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    
        return pedidos;
    }

    /**
     * Función para iniciar el recorrido del almacén basado en el pedido.
     * Se anima el recorrido utilizando un temporizador.
     * @param pedido Pedido para el cual se calculará y mostrará el recorrido.
     */
    private void iniciarRecorrido(Pedido pedido, CountDownLatch latch) {
        recorridoLock.lock(); // Bloqueo para el calculo e inicio del recorrido
        
        try {
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
                    if (!paused) {
                        if (index < puntosRecorrido.size() - 1) {
                            index++;
                            recorrido = new ArrayList<>(puntosRecorrido.subList(0, index + 1));
                            almacenPanel.setRecorrido(recorrido);
                        } else {
                            ((Timer) e.getSource()).stop();
                            mostrarResultadoRecorrido(pedido, puntosRecorrido);
                            latch.countDown(); // Señalizar que el recorrido ha terminado
                        }
                    }    
                }
            });
            timer.start();

        } finally {
            recorridoLock.unlock(); // Se libera el bloqueo
        }
        
    }

    /**
     * Función para calcular el recorrido en forma de S por el almacén
     * basado en los ítems del pedido.
     * @param pedido Pedido para el cual se calculará el recorrido.
     * @return Lista de puntos que forman el recorrido.
     */
    private List<Point> calcularRecorrido(Pedido pedido) {
        List<Point> puntosRecorrido = new ArrayList<>();
        Point puntoInicial = new Point(1, 9); // Punto de partida del recorrido
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
        
        // Agregar los puntos del recorrido en forma de S por los pasillos
        Point currentPoint = puntoInicial;
        boolean arribaPasillo = false;
        Item ultItem;
        int limite = 12;
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Comienzo del algoritmo

        // Se mueve un punto hacia arriba desde el punto inicial
        currentPoint = new Point(currentPoint.x, currentPoint.y - 1);
        puntosRecorrido.add(currentPoint);

        while (itemsOrdenados.size() > 0) {
            ultItem = itemsOrdenados.pop();

            if (ultItem.id <= limite)    {
                if (itemsOrdenados.size() == 0) // Previene un null pointer exception
                    itemsOrdenados.add(ultItem);

                if (itemsOrdenados.peek().id > limite)  {
                    // Está en pasillo y no hay más items

                    // Se recorre arriba/abajo según el valor del boolean arribaPasillo
                    for (int i = 0; i <= 6; i++)  {
                        if (!arribaPasillo) {
                            currentPoint = new Point(currentPoint.x, currentPoint.y - 1);                            
                        }
                        else    {
                            currentPoint = new Point(currentPoint.x, currentPoint.y + 1);
                        }
                        puntosRecorrido.add(currentPoint);        
                    }

                    arribaPasillo = !arribaPasillo; // Se cambia el valor del boolean una vez se subió/bajó
                }

                else if (itemsOrdenados.getLast().id <= limite)    {
                    // Todos los items restantes se encuentran dentro del pasillo
                    
                    // Se llega al último pasillo desde arriba
                    // Simplemente se recorre hasta abajo
                    if (arribaPasillo)  {
                        for (int i = 0; i <= 7; i++)    {
                            currentPoint = new Point(currentPoint.x, currentPoint.y + 1);
                            puntosRecorrido.add(currentPoint);
                        }   
                        
                        itemsOrdenados.clear();      
                    }

                    // Se llega al último pasillo desde abajo
                    // Se sube hasta el item más lejano y se regresa hacia abajo
                    else    {
                        int filaUltItem = 8; // el eje y incrementa su valor hacia abajo
                        
                        // Se busca el item mas lejano de abajo hacia arriba
                        itemsOrdenados.addFirst(ultItem);
                        for (Item item : itemsOrdenados) {
                            if (item.row < filaUltItem)
                                filaUltItem = item.row;
                        }

                        // Se recorre hacia dicho item
                        for (int i = 8; i > filaUltItem; i--) {
                            currentPoint = new Point(currentPoint.x, currentPoint.y - 1);
                            puntosRecorrido.add(currentPoint);
                        }

                        // Se mueve una unidad a la derecha para hacer giro en u y se regresa
                        currentPoint = new Point(currentPoint.x + 1, currentPoint.y);
                        puntosRecorrido.add(currentPoint);

                        while (currentPoint.y != puntoInicial.y) {
                            currentPoint = new Point(currentPoint.x, currentPoint.y + 1);
                            puntosRecorrido.add(currentPoint);
                        }

                        itemsOrdenados.clear(); //Se borra la lista de items para salir del while
                    }

                    // Regreso al punto de partida
                    while (currentPoint.x != puntoInicial.x) {
                        currentPoint = new Point(currentPoint.x - 1, currentPoint.y);
                        puntosRecorrido.add(currentPoint);
                    }
                }
            }

            // Movimiento a la derecha
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
    }

    /**
     * Función para mostrar el resultado del recorrido (distancia y tiempo)
     * en el área de texto.
     * @param puntosRecorrido Lista de puntos que forman el recorrido.
     */
    private void mostrarResultadoRecorrido(Pedido pedido, List<Point> puntosRecorrido) {
        int distanciaTotal = puntosRecorrido.size() - 1;
        int tiempoTotal = distanciaTotal;  // Dado que la velocidad es 1 m/s
        int pedidoNum = pedidos.size();

        // Se muestran los resultados del recorrido por pantalla
        outputArea.append("Resultado del recorrido para pedido p" + pedidoNum + ":\n");
        outputArea.append("Distancia total: " + distanciaTotal + " cuadros\n");
        outputArea.append("Tiempo total: " + tiempoTotal + " segundos\n\n");

        // Escritura del archivo resultados.txt con el pedido, sus items, la distancia y el tiempo total del recorrido
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("resultados.txt", true))) {
            writer.write("Pedido p" + pedido.getNumPedido() + ", " + distanciaTotal + ", " + tiempoTotal);
            writer.newLine();
            for (Item item : pedido.items) {
                writer.write("item " + item.id);
                writer.newLine();
            }
            outputArea.append("\n");
        } catch (IOException e) {
            outputArea.setText("Error al escribir el archivo de resultados.");
        }  
    }
}
