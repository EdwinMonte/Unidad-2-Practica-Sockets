import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClienteJava {
    private static final int TIMEOUT_CONEXION_MS = 5_000;
    private static final int TIMEOUT_RESPUESTA_MS = 5_000;

    public static void main(String[] args) {
        // Uso: java ClienteJava [host] [puerto] [mensaje_unico_opcional]
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = 5000;
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("[Cliente Java] Puerto inválido, usando puerto por defecto 5000.");
            }
        }

        String singleMessage = null;
        if (args.length > 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (i > 2) sb.append(' ');
                sb.append(args[i]);
            }
            singleMessage = sb.toString();
        }

        try (Socket socket = new Socket()) {
            System.out.println("[Cliente Java] Conectando a " + host + ":" + port + "...");
            socket.connect(new InetSocketAddress(host, port), TIMEOUT_CONEXION_MS);
            socket.setSoTimeout(TIMEOUT_RESPUESTA_MS);
            System.out.println("[Cliente Java] Conectado exitosamente a " + host + ":" + port + ".");

            try (PrintWriter out = new PrintWriter(
                         new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                if (singleMessage != null) {
                    sendAndReceive(out, in, singleMessage);
                } else {
                    System.out.println("[Cliente Java] Escribe un mensaje y presiona Enter (escribe 'salir' para terminar):");
                    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
                    while (true) {
                        System.out.print("> ");
                        if (!scanner.hasNextLine()) {
                            break;
                        }
                        String input = scanner.nextLine();
                        if (input.isBlank()) {
                            continue;
                        }

                        sendAndReceive(out, in, input);

                        if (input.equalsIgnoreCase("salir")) {
                            break;
                        }
                    }
                }

                // Cierre ordenado del flujo de salida (envía FIN TCP)
                if (!socket.isClosed() && !socket.isOutputShutdown()) {
                    socket.shutdownOutput();
                }
                System.out.println("[Cliente Java] Conexión cerrada correctamente.");
            }
        } catch (SocketTimeoutException e) {
            System.err.println("[Cliente Java] Error: Tiempo de espera agotado al conectar o esperar respuesta de " + host + ":" + port + ".");
        } catch (ConnectException e) {
            System.err.println("[Cliente Java] Error: Conexión rechazada. Verifica que el servidor esté ejecutándose en " + host + ":" + port + ".");
        } catch (SocketException e) {
            System.err.println("[Cliente Java] Error de red: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[Cliente Java] Error de E/S o caída imprevista del servidor: " + e.getMessage());
        }
    }

    private static void sendAndReceive(PrintWriter out, BufferedReader in, String message) throws IOException {
        out.println(message);
        String response = in.readLine();
        if (response == null) {
            System.out.println("[Cliente Java] El servidor cerró la conexión sin enviar respuesta.");
            return;
        }
        System.out.println("[Cliente Java] Respuesta: " + response);
    }
}
