package chat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/** Acepta conexiones y dirige los mensajes entre los usuarios del chat. */
public class ServidorChat {
    // Asocia cada nombre de usuario con su sesión activa.
    private final HashMap<String, AtencionCliente> usuarios = new HashMap<String, AtencionCliente>();

    /** Abre el servicio y permanece esperando nuevas conexiones. */
    public static void main(String[] args) throws IOException {
        ServidorChat servidor = new ServidorChat();
        // Esta IP limita la comunicación a programas del mismo computador.
        InetAddress ip = InetAddress.getByName("127.0.0.1");
        // 59420 es el puerto del servicio; 50 es la cola de conexiones pendientes.
        ServerSocket socketServidor = new ServerSocket(59420, 50, ip);
        System.out.println("Servidor iniciado en 127.0.0.1:59420");

        while (true) {
            // accept() espera una conexión; start() la atiende en un hilo independiente.
            Socket socketCliente = socketServidor.accept();
            AtencionCliente atencion = new AtencionCliente(socketCliente, servidor);
            atencion.start();
        }
    }

    /** Registra un nombre único y comunica a todos la lista actualizada. */
    // synchronized evita que dos hilos modifiquen el registro al mismo tiempo.
    public synchronized boolean registrarUsuario(String nombre, AtencionCliente sesion) {
        if (usuarios.containsKey(nombre)) {
            sesion.enviarLinea("ERROR\tNombre de usuario ya conectado");
            return false;
        }
        usuarios.put(nombre, sesion);
        sesion.enviarLinea("OK\t" + nombre);
        System.out.println("Usuario \"" + nombre + "\" conectado");
        actualizarUsuarios();
        return true;
    }

    /** Retira solo la sesión indicada, sin afectar a otro usuario con ese nombre. */
    public synchronized void retirarUsuario(String nombre, AtencionCliente sesion) {
        if (nombre != null && usuarios.get(nombre) == sesion) {
            usuarios.remove(nombre);
            System.out.println("El usuario \"" + nombre + "\" abandono el chat");
            actualizarUsuarios();
        }
    }

    /** Envía a un cliente los nombres conectados, ordenados alfabéticamente. */
    public synchronized void enviarUsuarios(AtencionCliente sesion) {
        List<String> nombres = new ArrayList<String>(usuarios.keySet());
        Collections.sort(nombres);
        sesion.enviarLinea("USERS\t" + String.join(",", nombres));
    }

    /** Notifica a todos los clientes cuando alguien entra o sale. */
    private void actualizarUsuarios() {
        System.out.println("Usuarios conectados: " + usuarios.keySet());
        for (AtencionCliente sesion : usuarios.values()) {
            enviarUsuarios(sesion);
        }
    }

    /** Busca al destinatario y devuelve si la escritura en su conexión tuvo éxito. */
    public synchronized boolean enviarMensaje(String emisor, String destinatario, String texto) {
        AtencionCliente destino = usuarios.get(destinatario);
        if (destino == null) {
            return false;
        }
        return destino.enviarLinea("FROM\t" + emisor + "\t" + texto);
    }
}
