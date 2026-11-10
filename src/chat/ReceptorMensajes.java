package chat;

import java.io.BufferedReader;
import java.io.IOException;

/** Escucha al servidor y muestra sus respuestas sin bloquear el teclado. */
public class ReceptorMensajes extends Thread {
    private final BufferedReader entrada;
    private final ClienteChat cliente;

    public ReceptorMensajes(BufferedReader entrada, ClienteChat cliente) {
        this.entrada = entrada;
        this.cliente = cliente;
    }

    /** Recibe listas, mensajes y errores hasta la despedida o el cierre de la conexión. */
    @Override
    public void run() {
        try {
            String linea = entrada.readLine();
            while (linea != null) {
                if (linea.equals("BYE")) {
                    System.out.println("Sesion finalizada: chao");
                    return;
                }
                // Se separan hasta tres campos: operación, emisor y contenido del mensaje.
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
            // Si termina la recepción, también termina la conexión del cliente.
            cliente.cerrarConexion();
        }
    }
}
