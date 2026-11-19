# Guía de Pruebas y Captura en Wireshark

Esta guía te ayudará a ejecutar todas las pruebas solicitadas en la práctica y obtener las 3 capturas de pantalla requeridas para tu entrega.

---

## 1. Prueba Local (`localhost`) — Concurrencia e Interoperabilidad

### Paso A: Compilar Java (solo la primera vez)
```powershell
javac Cliente-Servidor-Java/ServidorJava.java Cliente-Servidor-Java/ClienteJava.java
```

### Paso B: Prueba de Concurrencia + Interoperabilidad (C# Servidor + Clientes Java y C#)
Abre **3 terminales** en la carpeta raíz del proyecto:

1. **Terminal 1 (Servidor C#):**
   ```powershell
   dotnet run --project Cliente-Servidor-CSharp/ServidorCSharp
   ```
2. **Terminal 2 (Cliente Java #1):**
   ```powershell
   java -cp Cliente-Servidor-Java ClienteJava
   ```
3. **Terminal 3 (Cliente C# #2):**
   ```powershell
   dotnet run --project Cliente-Servidor-CSharp/ClienteCSharp
   ```

Envía mensajes intercalados desde ambas terminales sin cerrar ninguna. Verás en el servidor cómo atiende a `[Cliente #1]` y `[Cliente #2]` simultáneamente. Toma aquí tu **Captura 1 (Ejecución Local)**.

### Paso C: Prueba Inversa (Servidor Java + Cliente C#)
1. Detén el servidor C# (`Ctrl+C`) y ejecuta el servidor Java:
   ```powershell
   java -cp Cliente-Servidor-Java ServidorJava
   ```
2. Conecta el cliente C#:
   ```powershell
   dotnet run --project Cliente-Servidor-CSharp/ClienteCSharp
   ```

---

## 2. Prueba Remota (Misma LAN o Máquina Virtual)

1. **En la PC Servidor:**
   - Obtén tu dirección IPv4 local ejecutando:
     ```powershell
     ipconfig
     ```
     *(Ejemplo: `192.168.1.75`)*
   - Si el Firewall de Windows bloquea el puerto `5000`, permite la conexión temporalmente ejecutando PowerShell como Administrador:
     ```powershell
     New-NetFirewallRule -DisplayName "Practica Sockets TCP 5000" -Direction Inbound -Protocol TCP -LocalPort 5000 -Action Allow
     ```
   - Inicia cualquiera de los servidores (`ServidorCSharp` o `ServidorJava`).

2. **En la PC Cliente (o Máquina Virtual en modo Puente/NAT hacia el host):**
   - Ejecuta el cliente indicando la IP del servidor:
     ```powershell
     java -cp Cliente-Servidor-Java ClienteJava 192.168.1.75 5000
     ```
     *(o con C#: `dotnet run --project Cliente-Servidor-CSharp/ClienteCSharp -- 192.168.1.75 5000`)*

3. Toma aquí tu **Captura 2 (Ejecución Remota en LAN/VM)**.

*(Al terminar tus pruebas, puedes eliminar la regla de firewall si lo deseas con `Remove-NetFirewallRule -DisplayName "Practica Sockets TCP 5000"`).*

---

## 3. Captura de Paquetes en Wireshark

1. Abre **Wireshark**.
2. Selecciona la interfaz de red adecuada:
   - Si haces la prueba en la misma PC (`127.0.0.1`): selecciona **`Adapter for loopback traffic capture`**.
   - Si haces la prueba en LAN entre dos equipos: selecciona tu adaptador **Wi-Fi** o **Ethernet**.
3. En la barra de filtro superior (*Apply a display filter*), escribe:
   ```text
   tcp.port == 5000
   ```
   y presiona **Enter**.
4. Inicia el servidor y ejecuta un cliente enviando un mensaje (por ejemplo `"Hola Wireshark"`) y luego `"salir"`.
5. Detén la captura en Wireshark (botón cuadrado rojo).
6. Verifica en la lista de paquetes las 3 fases clave para tu **Captura 3**:
   - **Three-Way Handshake:**
     1. Cliente → Servidor: `[SYN]`
     2. Servidor → Cliente: `[SYN, ACK]`
     3. Cliente → Servidor: `[ACK]`
   - **Transferencia de datos:**
     - Paquetes `[PSH, ACK]` donde en el panel inferior (*Packet Bytes*) se observa en texto plano el mensaje enviado y la respuesta `ECO: ...` o `ECO DESDE JAVA: ...`.
   - **Cierre ordenado:**
     - Paquetes `[FIN, ACK]` al escribir `salir`.
