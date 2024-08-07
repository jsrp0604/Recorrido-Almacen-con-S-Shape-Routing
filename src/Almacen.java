import java.util.HashMap;
import java.util.Map;

public class Almacen {
    int rows = 10; // Añadimos 2 filas adicionales arriba y abajo
    int cols = 20; // Total de columnas incluyendo pasillos
    int[][] ubicaciones;
    Map<Integer, Item> itemsMap;

    public Almacen() {
        ubicaciones = new int[rows][cols];
        itemsMap = new HashMap<>();
        int id = 1;
        for (int c = 0; c < cols; c++) {
            if (c == 0 || c == 3 || c == 4 || c == 7 || c == 8 || c == 11 || c == 12 || c == 15 || c == 16 || c == 19 || c == 20) { // Columnas donde se colocan los items
                for (int r = rows-3; r >= 2; r--) { // Llenamos desde la fila 2 hasta la fila 7 (dejando las primeras 2 y las últimas 2 filas vacías)
                    if (id <= 60) {
                        ubicaciones[r][c] = id;
                        itemsMap.put(id, new Item(id, r, c));
                        id++;
                    }
                }
            }
        }
    }

    public Item getItem(int id) {
        return itemsMap.get(id);
    }
}