package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

// Un hilo atiende una conexion sin bloquear a los demas usuarios.
public class AtencionCliente extends Thread {
    private final Socket socketCliente;
    private final ServidorChat servidor;
    private PrintWriter salida;
    private String nombre;

    public AtencionCliente(Socket socketCliente, ServidorChat servidor) {
        this.socketCliente = socketCliente;
        this.servidor = servidor;
    }

    @Override
    public void run() {
        try {
            InputStreamReader lector = new InputStreamReader(
                    socketCliente.getInputStream(), StandardCharsets.UTF_8);
            BufferedReader entrada = new BufferedReader(lector);
            salida = new PrintWriter(socketCliente.getOutputStream(), true, StandardCharsets.UTF_8);

            String login = entrada.readLine();
            if (login == null) {
                return;
            }
            String[] campos = login.split("\t", -1);
            if (campos.length != 2 || !campos[0].equals("LOGIN")) {
                enviarLinea("ERROR\tNombre invalido o falta LOGIN");
                return;
            }
            nombre = campos[1];
            if (nombre.isEmpty() || nombre.length() > 24
                    || nombre.contains(" ") || nombre.contains(",")) {
                enviarLinea("ERROR\tUse de 1 a 24 caracteres, sin espacios ni comas");
                return;
            }
            if (!servidor.registrarUsuario(nombre, this)) {
                return;
            }

            String linea = entrada.readLine();
            while (linea != null) {
                if (linea.equals("QUIT")) {
                    enviarLinea("BYE");
                    break;
                } else if (linea.equals("LIST")) {
                    servidor.enviarUsuarios(this);
                } else {
                    campos = linea.split("\t", -1);
                    if (campos.length != 3 || !campos[0].equals("MSG")) {
                        enviarLinea("ERROR\tMensaje invalido");
                    } else if (!servidor.enviarMensaje(nombre, campos[1], campos[2])) {
                        enviarLinea("ERROR\tDestinatario no disponible");
                    }
                }
                linea = entrada.readLine();
            }
        } catch (IOException error) {
            System.out.println("Conexion interrumpida: " + error.getMessage());
        } finally {
            servidor.retirarUsuario(nombre, this);
            try {
                socketCliente.close();
            } catch (IOException error) {
                System.err.println("Error al cerrar: " + error.getMessage());
            }
        }
    }

    // println envia una linea completa; true activa el vaciado automatico.
    public synchronized boolean enviarLinea(String linea) {
        salida.println(linea);
        return !salida.checkError();
    }
}
