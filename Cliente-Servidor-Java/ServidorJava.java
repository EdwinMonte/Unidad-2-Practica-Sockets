import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class ServidorJava {
    private static final int TIMEOUT_INACTIVIDAD_MS = 60_000;
    private static final AtomicInteger clientCounter = new AtomicInteger(0);

    public static void main(String[] args) {
        int port = 5000;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("[Servidor Java] Puerto inválido, usando puerto por defecto 5000.");
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Servidor Java] Escuchando en el puerto " + port + " (0.0.0.0)...");
            System.out.println("[Servidor Java] Esperando conexiones. Presione Ctrl+C para detener.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientId = clientCounter.incrementAndGet();

                // Virtual Thread por cliente para concurrencia ligera sin bloquear el accept
                Thread.ofVirtual().start(() -> handleClient(clientSocket, clientId));
            }
        } catch (BindException e) {
            System.err.println("[Servidor Java] Error: El puerto " + port + " ya está ocupado por otro proceso.");
        } catch (IOException e) {
            System.err.println("[Servidor Java] Error de red al iniciar el servidor: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket, int clientId) {
        String remoteAddress = socket.getRemoteSocketAddress().toString();
        System.out.println("[Servidor Java] Cliente #" + clientId + " conectado desde " + remoteAddress + ".");

        try (socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            socket.setSoTimeout(TIMEOUT_INACTIVIDAD_MS);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("[Servidor Java] [Cliente #" + clientId + "] Recibido: " + inputLine);

                if (inputLine.equalsIgnoreCase("salir")) {
                    out.println("ECO DESDE JAVA: Conexión cerrada por el servidor. ¡Hasta luego!");
                    System.out.println("[Servidor Java] Cliente #" + clientId + " solicitó desconexión.");
                    return;
                }

                out.println("ECO DESDE JAVA: " + inputLine);
            }

            System.out.println("[Servidor Java] Cliente #" + clientId + " cerró la conexión ordenadamente.");
        } catch (SocketTimeoutException e) {
            System.out.println("[Servidor Java] Cliente #" + clientId + " desconectado por tiempo de espera agotado (60s).");
        } catch (SocketException e) {
            System.err.println("[Servidor Java] Caída imprevista o reinicio de conexión con Cliente #" + clientId + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[Servidor Java] Error de E/S procesando Cliente #" + clientId + ": " + e.getMessage());
        } finally {
            System.out.println("[Servidor Java] Sesión de Cliente #" + clientId + " finalizada.");
        }
    }
}
