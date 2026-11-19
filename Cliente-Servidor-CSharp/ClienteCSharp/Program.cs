using System.Net.Sockets;
using System.Text;

// Uso: dotnet run [host] [puerto] [mensaje_unico_opcional]
string host = args.Length > 0 ? args[0] : "127.0.0.1";
int port = args.Length > 1 && int.TryParse(args[1], out int p) ? p : 5000;
string? singleMessage = args.Length > 2 ? string.Join(' ', args.Skip(2)) : null;

// Codificación UTF-8 sin BOM para garantizar interoperabilidad con Java
var utf8NoBom = new UTF8Encoding(encoderShouldEmitUTF8Identifier: false);

try
{
    using var client = new TcpClient();

    Console.WriteLine($"[Cliente C#] Conectando a {host}:{port}...");
    using (var connectCts = new CancellationTokenSource(TimeSpan.FromSeconds(5)))
    {
        await client.ConnectAsync(host, port, connectCts.Token);
    }

    Console.WriteLine($"[Cliente C#] Conectado exitosamente a {host}:{port}.");

    await using NetworkStream stream = client.GetStream();
    using var reader = new StreamReader(stream, utf8NoBom, leaveOpen: true);
    await using var writer = new StreamWriter(stream, utf8NoBom, leaveOpen: true) { AutoFlush = true };

    if (singleMessage is not null)
    {
        await SendAndReceiveAsync(writer, reader, singleMessage);
    }
    else
    {
        Console.WriteLine("[Cliente C#] Escribe un mensaje y presiona Enter (escribe 'salir' para terminar):");
        while (true)
        {
            Console.Write("> ");
            string? input = Console.ReadLine();

            if (input is null)
            {
                break;
            }

            if (string.IsNullOrWhiteSpace(input))
            {
                continue;
            }

            await SendAndReceiveAsync(writer, reader, input);

            if (input.Equals("salir", StringComparison.OrdinalIgnoreCase))
            {
                break;
            }
        }
    }

    // Cierre ordenado del flujo de salida (envía FIN TCP)
    try
    {
        if (client.Connected)
        {
            client.Client.Shutdown(SocketShutdown.Send);
        }
    }
    catch (SocketException)
    {
        // El servidor ya cerró el socket previamente
    }
    Console.WriteLine("[Cliente C#] Conexión cerrada correctamente.");
}
catch (OperationCanceledException)
{
    Console.Error.WriteLine($"[Cliente C#] Error: Tiempo de espera agotado al intentar conectar o esperar respuesta de {host}:{port}.");
}
catch (SocketException ex) when (ex.SocketErrorCode == SocketError.ConnectionRefused)
{
    Console.Error.WriteLine($"[Cliente C#] Error: Conexión rechazada. Verifica que el servidor esté ejecutándose en {host}:{port}.");
}
catch (SocketException ex)
{
    Console.Error.WriteLine($"[Cliente C#] Error de red ({ex.SocketErrorCode}): {ex.Message}");
}
catch (IOException ex)
{
    Console.Error.WriteLine($"[Cliente C#] El servidor cerró la conexión de forma inesperada: {ex.Message}");
}

static async Task SendAndReceiveAsync(StreamWriter writer, StreamReader reader, string message)
{
    await writer.WriteLineAsync(message);

    using var readCts = new CancellationTokenSource(TimeSpan.FromSeconds(5));
    string? response = await reader.ReadLineAsync(readCts.Token);

    if (response is null)
    {
        Console.WriteLine("[Cliente C#] El servidor cerró la conexión sin enviar respuesta.");
        return;
    }

    Console.WriteLine($"[Cliente C#] Respuesta: {response}");
}
