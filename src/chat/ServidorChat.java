package chat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

// Punto de entrada del servidor y registro de usuarios.
public class ServidorChat {
    private final HashMap<String, AtencionCliente> usuarios = new HashMap<String, AtencionCliente>();

    public static void main(String[] args) throws IOException {
        ServidorChat servidor = new ServidorChat();
        InetAddress ip = InetAddress.getByName("127.0.0.1");
        ServerSocket socketServidor = new ServerSocket(59420, 50, ip);
        System.out.println("Servidor iniciado en 127.0.0.1:59420");

        while (true) {
            Socket socketCliente = socketServidor.accept();
            AtencionCliente atencion = new AtencionCliente(socketCliente, servidor);
            atencion.start();
        }
    }

    // synchronized evita que dos hilos cambien el registro al mismo tiempo.
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

    public synchronized void retirarUsuario(String nombre, AtencionCliente sesion) {
        if (nombre != null && usuarios.get(nombre) == sesion) {
            usuarios.remove(nombre);
            System.out.println("El usuario \"" + nombre + "\" abandono el chat");
            actualizarUsuarios();
        }
    }

    public synchronized void enviarUsuarios(AtencionCliente sesion) {
        List<String> nombres = new ArrayList<String>(usuarios.keySet());
        Collections.sort(nombres);
        sesion.enviarLinea("USERS\t" + String.join(",", nombres));
    }

    private void actualizarUsuarios() {
        System.out.println("Usuarios conectados: " + usuarios.keySet());
        for (AtencionCliente sesion : usuarios.values()) {
            enviarUsuarios(sesion);
        }
    }

    public synchronized boolean enviarMensaje(String emisor, String destinatario, String texto) {
        AtencionCliente destino = usuarios.get(destinatario);
        if (destino == null) {
            return false;
        }
        return destino.enviarLinea("FROM\t" + emisor + "\t" + texto);
    }
}
