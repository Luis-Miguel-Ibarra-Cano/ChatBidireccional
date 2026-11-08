package chat;

import java.io.BufferedReader;
import java.io.IOException;

// Recibe mensajes mientras el usuario escribe en la consola.
public class ReceptorMensajes extends Thread {
    private final BufferedReader entrada;
    private final ClienteChat cliente;

    public ReceptorMensajes(BufferedReader entrada, ClienteChat cliente) {
        this.entrada = entrada;
        this.cliente = cliente;
    }

    @Override
    public void run() {
        try {
            String linea = entrada.readLine();
            while (linea != null) {
                if (linea.equals("BYE")) {
                    System.out.println("Sesion finalizada: chao");
                    return;
                }
                String[] campos = linea.split("\t", 3);
                if (campos[0].equals("USERS") && campos.length > 1) {
                    System.out.println("Usuarios conectados: [" + campos[1] + "]");
                } else if (campos[0].equals("FROM") && campos.length == 3) {
                    System.out.println(campos[1] + " -> " + campos[2]);
                } else if (campos[0].equals("ERROR") && campos.length > 1) {
                    System.out.println("Error: " + campos[1]);
                }
                linea = entrada.readLine();
            }
            System.out.println("El servidor cerro la conexion");
        } catch (IOException error) {
            System.out.println("Conexion terminada: " + error.getMessage());
        } finally {
            cliente.cerrarConexion();
        }
    }
}
