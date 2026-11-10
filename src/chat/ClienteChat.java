package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Representa a un usuario; su hilo lee el teclado mientras otro recibe mensajes. */
public class ClienteChat extends Thread {
    private Socket socketCliente;
    private BufferedReader consola;
    private PrintWriter salida;

    public static void main(String[] args) throws InterruptedException {
        ClienteChat cliente = new ClienteChat();
        try {
            cliente.iniciarChat();
        } catch (IOException | IllegalArgumentException error) {
            System.err.println("No se pudo conectar o continuar: " + error.getMessage());
            System.exit(1);
        } finally {
            cliente.cerrarConexion();
        }
    }

    /** Solicita los datos, abre la conexión y espera la confirmación del servidor. */
    private void iniciarChat() throws IOException, InterruptedException {
        InputStreamReader lectorConsola = new InputStreamReader(System.in, StandardCharsets.UTF_8);
        consola = new BufferedReader(lectorConsola);
        String ip = pedirDato("IP del servidor:");
        int puerto = Integer.parseInt(pedirDato("Puerto del servidor:"));
        String nombre = pedirDato("Nombre de usuario:");

        socketCliente = new Socket();
        // Los 5000 milisegundos limitan el intento de conexión, no la duración del chat.
        socketCliente.connect(new InetSocketAddress(ip, puerto), 5000);
        InputStreamReader lector = new InputStreamReader(
                socketCliente.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader entrada = new BufferedReader(lector);
        salida = new PrintWriter(socketCliente.getOutputStream(), true, StandardCharsets.UTF_8);
        // LOGIN identifica al usuario; el servidor debe responder OK con el mismo nombre.
        salida.println("LOGIN\t" + nombre);
        String respuesta = entrada.readLine();
        if (!("OK\t" + nombre).equals(respuesta)) {
            throw new IOException("Ingreso rechazado: " + respuesta);
        }
        System.out.println("Conectado como " + nombre);
        System.out.println("Comandos: /usuarios, /con nombre, chao");
        System.out.println("Seleccione un usuario antes de escribir mensajes");

        ReceptorMensajes receptor = new ReceptorMensajes(entrada, this);
        // El hilo del teclado no mantiene vivo el programa si termina el receptor.
        setDaemon(true);
        receptor.start();
        start();
        // El método principal espera al receptor antes de cerrar la conexión.
        receptor.join();
    }

    /** Solicita un dato obligatorio y elimina espacios sobrantes en sus extremos. */
    private String pedirDato(String etiqueta) throws IOException {
        System.out.println(etiqueta);
        String valor = consola.readLine();
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Falta el dato: " + etiqueta);
        }
        return valor.trim();
    }

    /** Interpreta la consola: consulta usuarios, selecciona destinatario o envía texto. */
    @Override
    public void run() {
        String destinatario = null;
        try {
            String texto = consola.readLine();
            while (texto != null) {
                if (texto.trim().equalsIgnoreCase("chao")) {
                    break;
                } else if (texto.equals("/usuarios")) {
                    salida.println("LIST");
                } else if (texto.startsWith("/con ")) {
                    // La selección se guarda aquí; el servidor verifica el destino al enviar.
                    destinatario = texto.substring(5).trim();
                    System.out.println("Conversando con " + destinatario);
                } else if (destinatario == null) {
                    System.out.println("Primero seleccione un usuario con /con nombre");
                } else if (!texto.trim().isEmpty()) {
                    salida.println("MSG\t" + destinatario + "\t" + texto);
                    System.out.println("Yo -> " + destinatario + ": " + texto);
                }
                if (salida.checkError()) {
                    cerrarConexion();
                    return;
                }
                texto = consola.readLine();
            }
            // Se solicita salir tanto al escribir «chao» como al terminar la entrada.
            salida.println("QUIT");
        } catch (IOException error) {
            System.err.println("Error en consola: " + error.getMessage());
            cerrarConexion();
        }
    }

    /** Cierra el socket y sus flujos; puede llamarse desde los dos hilos del cliente. */
    public synchronized void cerrarConexion() {
        if (socketCliente != null) {
            try {
                socketCliente.close();
            } catch (IOException error) {
                System.err.println("Error al cerrar: " + error.getMessage());
            }
        }
    }
}
