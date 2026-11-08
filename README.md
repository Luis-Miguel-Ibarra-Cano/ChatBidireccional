# ChatBidireccional

Chat de consola para Java 11, organizado como un proyecto Eclipse. Solo utiliza la biblioteca estandar de Java.

## Abrir en Eclipse

1. Ir a `File > Import > General > Existing Projects into Workspace`.
2. Seleccionar esta carpeta, `ChatBidireccional`, y finalizar.
3. En `Window > Preferences > Java > Installed JREs`, registrar el JDK instalado en `C:\Users\libarra\.jdks\temurin-11.0.32.1`.
4. Asociar ese JDK al entorno `JavaSE-11` en `Java > Installed JREs > Execution Environments`.

## Ejecutar los launchers en Eclipse

Los archivos `ServidorChat.launch` y `ClienteChat.launch` guardan configuraciones de ejecucion. Despues de importar el proyecto, se seleccionan desde la ventana `Run Configurations`. El orden es: servidor, primer cliente y segundo cliente.

### 1. Iniciar el servidor

1. En la barra de menus superior, hacer clic en **Run**.
2. Hacer clic en **Run Configurations...**.
3. En la lista izquierda, desplegar **Java Application** con la flecha situada a su lado.
4. Hacer clic en **ServidorChat**. Este nombre corresponde a `ServidorChat.launch`.
5. En la pestana **Main**, comprobar los campos: **Project: ChatBidireccional** y **Main class: chat.ServidorChat**.
6. Pulsar el boton **Run**, ubicado abajo a la derecha.
7. En la vista **Console**, comprobar que aparece `Servidor iniciado en 127.0.0.1:59420`. Dejar ese proceso activo.

### 2. Iniciar el primer cliente

1. Volver al menu **Run > Run Configurations...**.
2. Desplegar **Java Application** y seleccionar **ClienteChat**. Corresponde a `ClienteChat.launch`.
3. En **Main**, comprobar: **Project: ChatBidireccional** y **Main class: chat.ClienteChat**.
4. Pulsar **Run**, abajo a la derecha.
5. Hacer clic dentro de la vista **Console** del cliente para poder escribir.
6. Cuando pida **IP del servidor**, escribir `127.0.0.1` y pulsar **Enter**.
7. Cuando pida **Puerto del servidor**, escribir `59420` y pulsar **Enter**.
8. Cuando pida **Nombre de usuario**, escribir `poli01` y pulsar **Enter**.
9. Comprobar el mensaje `Conectado como poli01`.

### 3. Iniciar el segundo cliente

1. Sin detener el servidor ni el primer cliente, abrir otra vez **Run > Run Configurations...**.
2. En **Java Application**, seleccionar de nuevo **ClienteChat**.
3. Pulsar **Run**, abajo a la derecha. Se inicia otro proceso del mismo programa.
4. En su nueva consola, introducir cada dato seguido de **Enter**: `127.0.0.1`, `59420` y `poli02`.
5. Comprobar el mensaje `Conectado como poli02`.

Al terminar estos pasos deben estar activos tres procesos: un servidor y dos clientes. No se necesita crear otro archivo `.launch` para el segundo usuario.

### 4. Cambiar de consola y conversar

1. En la barra de herramientas de **Console**, buscar el boton **Display Selected Console**. Al dejar el mouse sobre el icono aparece su nombre.
2. Pulsar la flecha junto a ese boton y elegir la consola de uno de los procesos **ClienteChat**.
3. Revisar su contenido: el mensaje `Conectado como poli01` o `Conectado como poli02` permite identificar al usuario de esa consola.
4. En la consola de `poli01`, escribir `/con poli02` y pulsar **Enter**.
5. Cambiar a la consola de `poli02`, escribir `/con poli01` y pulsar **Enter**.
6. Escribir mensajes y pulsar **Enter** para enviarlos. Cambiar de consola para verlos y responder.

Si la vista **Console** no aparece, abrir **Window > Show View > Other...**, seleccionar **Debug > Console** y pulsar **Open**.

### 5. Finalizar

1. Para cerrar un cliente, seleccionar su consola, escribir `chao` y pulsar **Enter**.
2. Para detener el servidor, seleccionar su consola y pulsar **Terminate**, el boton con un cuadrado rojo.

### Si los nombres no aparecen en Run Configurations

1. En **Package Explorer**, seleccionar el proyecto y pulsar **F5** para actualizarlo. Volver a revisar **Run > Run Configurations... > Java Application**.
2. Como alternativa, desplegar **ChatBidireccional > src > chat**, hacer clic derecho en **ServidorChat.java** y elegir **Run As > Java Application**. Para los clientes, repetir sobre **ClienteChat.java**, dos veces.
3. Si se crean configuraciones nuevas con esa alternativa, abrir **Run > Run Configurations...**, seleccionar cada configuracion y poner `-Dfile.encoding=UTF-8` en **Arguments > VM arguments** y **UTF-8** en **Common > Encoding**. Pulsar **Apply**. Los archivos `.launch` incluidos ya contienen esos valores.

Referencia de los menus: [configuraciones de ejecucion de Eclipse](https://help.eclipse.org/latest/topic/org.eclipse.jdt.doc.user/tasks/tasks-java-local-configuration.htm).

## Comandos

| Entrada | Accion |
|---|---|
| `/usuarios` | Consultar participantes. |
| `/con nombre` | Elegir el destinatario de los mensajes siguientes. |
| Texto normal | Enviar al destinatario elegido. |
| `chao` | Salir del chat. |

El servidor informa si el destinatario no esta conectado. Los nombres admiten de 1 a 24 caracteres, sin espacios, comas ni tabuladores, y no pueden repetirse. Los mensajes son de una linea sin tabuladores y admiten caracteres UTF-8. `Yo -> usuario` muestra el envio, no una confirmacion de lectura.

## Archivos Java

- `ServidorChat`: escucha y registro de usuarios.
- `ClienteChat`: conexion y consola.
- `AtencionCliente`: un hilo por conexion en el servidor.
- `ReceptorMensajes`: recepcion independiente en el cliente.

Son dos programas y dos clases auxiliares pequenas. No requiere base de datos, frameworks ni dependencias externas. Escucha solamente en el mismo computador, en `127.0.0.1:59420`.

El registro de usuarios es un `HashMap`. Los metodos que lo utilizan llevan `synchronized` para que los hilos no modifiquen el registro al mismo tiempo. El cliente y las dos clases auxiliares utilizan `Thread`, sin lambdas, interfaces propias ni expresiones regulares.

El protocolo usa una linea UTF-8 por trama, con campos separados por tabuladores: `LOGIN` identifica, `LIST` consulta usuarios, `MSG` envia un mensaje y `QUIT` solicita la salida. El servidor responde con `OK`, `USERS`, `FROM`, `BYE` o `ERROR`.
