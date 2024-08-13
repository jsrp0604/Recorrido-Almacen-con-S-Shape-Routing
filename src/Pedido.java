import java.util.List;

public class Pedido {
    List<Item> items;
    int numPedido;

    public Pedido(List<Item> items, int numPedido) {
        this.items = items;
        this.numPedido = numPedido;
    }
    
    public int getNumPedido() {
        return numPedido;
    }
}
