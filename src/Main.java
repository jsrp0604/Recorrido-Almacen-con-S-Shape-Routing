import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        
        Path fileToDeletePath = Paths.get("resultados.txt"); //Se borra el resultados.txt antes de iniciar un nuevo programa
        try {
            Files.delete(fileToDeletePath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SimuladorAlmacen().setVisible(true);
            }
        });
    }
}
