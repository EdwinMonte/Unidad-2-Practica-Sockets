using System.Net;
using System.Net.Sockets;
using System.Text;

// Puerto por defecto 5000, configurable por argumento de línea de comandos
int port = args.Length > 0 && int.TryParse(args[0], out int p) ? p : 5000;
int clientCounter = 0;

// Codificación UTF-8 sin BOM para garantizar interoperabilidad con Java
var utf8NoBom = new UTF8Encoding(encoderShouldEmitUTF8Identifier: false);

TcpListener? listener = null;

try
{
    listener = new TcpListener(IPAddress.Any, port);
    listener.Start();
    Console.WriteLine($"[Servidor C#] Escuchando en el puerto {port} (0.0.0.0)...");
    Console.WriteLine("[Servidor C#] Esperando conexiones. Presione Ctrl+C para detener.");

    while (true)
    {
        TcpClient client = await listener.AcceptTcpClientAsync();
        int clientId = Interlocked.Increment(ref clientCounter);

        // Atiende cada cliente en una tarea independiente sin bloquear el Accept
        _ = Task.Run(() => HandleClientAsync(client, clientId, utf8NoBom));
    }
}
catch (SocketException ex) when (ex.SocketErrorCode == SocketError.AddressAlreadyInUse)
{
    Console.Error.WriteLine($"[Servidor C#] Error: El puerto {port} ya está ocupado por otro proceso.");
}
catch (SocketException ex)
{
    Console.Error.WriteLine($"[Servidor C#] Error de red al iniciar el servidor: {ex.Message} ({ex.SocketErrorCode})");
}
finally
{
    listener?.Stop();
}

static async Task HandleClientAsync(TcpClient client, int clientId, Encoding encoding)
{
    string remoteEndPoint = client.Client.RemoteEndPoint?.ToString() ?? "desconocido";
    Console.WriteLine($"[Servidor C#] Cliente #{clientId} conectado desde {remoteEndPoint}.");

    try
    {
        using (client)
        await using (NetworkStream stream = client.GetStream())
        using (var reader = new StreamReader(stream, encoding, leaveOpen: true))
        await using (var writer = new StreamWriter(stream, encoding, leaveOpen: true) { AutoFlush = true })
        {
            while (true)
            {
                // Tiempo de espera máximo de 60 segundos por inactividad del cliente
                using var readTimeoutCts = new CancellationTokenSource(TimeSpan.FromSeconds(60));
                string? message = await reader.ReadLineAsync(readTimeoutCts.Token);

                // null indica cierre ordenado (FIN) por parte del cliente
                if (message is null)
                {
                    Console.WriteLine($"[Servidor C#] Cliente #{clientId} cerró la conexión ordenadamente.");
                    break;
                }

                Console.WriteLine($"[Servidor C#] [Cliente #{clientId}] Mensaje recibido: {message}");

                if (message.Equals("salir", StringComparison.OrdinalIgnoreCase))
                {
                    await writer.WriteLineAsync("ECO: Conexión cerrada por el servidor. ¡Hasta luego!");
                    Console.WriteLine($"[Servidor C#] Cliente #{clientId} solicitó desconexión.");
                    break;
                }

                await writer.WriteLineAsync($"ECO: {message}");
            }
        }
    }
    catch (OperationCanceledException)
    {
        Console.WriteLine($"[Servidor C#] Cliente #{clientId} desconectado por tiempo de espera agotado (60s).");
    }
    catch (IOException ex)
    {
        Console.Error.WriteLine($"[Servidor C#] Caída imprevista o error de E/S con Cliente #{clientId}: {ex.Message}");
    }
    catch (SocketException ex)
    {
        Console.Error.WriteLine($"[Servidor C#] Error de socket con Cliente #{clientId}: {ex.Message}");
    }
    finally
    {
        Console.WriteLine($"[Servidor C#] Sesión de Cliente #{clientId} finalizada.");
    }
}
