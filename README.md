# Práctica de Laboratorio: Sockets TCP/IP

**Materia:** Programación en Ambiente Cliente/Servidor  
**Carrera:** Licenciatura / Ingeniería en Informática (TecNM)  
**Entornos:** .NET 10 (C#) y Java 21 LTS  

Implementación sencilla, concurrente e interoperable de comunicación cliente/servidor mediante sockets TCP/IP, con manejo de errores de red, cierre ordenado y documentación técnica.

---

## 📁 Estructura del Repositorio

```text
.
├── Cliente-Servidor-CSharp/
│   ├── ServidorCSharp/          # Servidor TCP asíncrono (.NET 10)
│   └── ClienteCSharp/           # Cliente TCP (.NET 10)
├── Cliente-Servidor-Java/
│   ├── ServidorJava.java        # Servidor TCP con Virtual Threads (Java 21)
│   └── ClienteJava.java         # Cliente TCP (Java 21)
├── docs/
│   ├── REPORTE_TECNICO.md       # Reporte técnico completo (Portada TecNM, UML, excepciones, conclusiones)
│   └── GUIA_PRUEBAS_Y_WIRESHARK.md  # Guía paso a paso para evidencias en localhost, LAN y Wireshark
├── .gitignore
└── README.md
```

---

## ⚙️ Requisitos

- [.NET 10 SDK](https://dotnet.microsoft.com/) (`dotnet --version`)
- [Java JDK 21+](https://adoptium.net/) (`java --version` y `javac --version`)

---

## 🚀 Cómo Compilar y Ejecutar

### 1. Opción C# (.NET 10)

En una terminal (Servidor):
```powershell
dotnet run --project Cliente-Servidor-CSharp/ServidorCSharp
```

En otra terminal (Cliente interactivo):
```powershell
dotnet run --project Cliente-Servidor-CSharp/ClienteCSharp
```

También puedes pasar `[host] [puerto] [mensaje_unico_opcional]`:
```powershell
dotnet run --project Cliente-Servidor-CSharp/ClienteCSharp -- 127.0.0.1 5000 "Hola desde C#"
```

### 2. Opción Java (Virtual Threads)

Compilar ambos archivos:
```powershell
javac Cliente-Servidor-Java/ServidorJava.java Cliente-Servidor-Java/ClienteJava.java
```

En una terminal (Servidor):
```powershell
java -cp Cliente-Servidor-Java ServidorJava
```

En otra terminal (Cliente interactivo):
```powershell
java -cp Cliente-Servidor-Java ClienteJava
```

Con mensaje único por argumentos:
```powershell
java -cp Cliente-Servidor-Java ClienteJava 127.0.0.1 5000 "Hola desde Java"
```

---

## 🧪 Escenarios de la Práctica

1. **Flujo Básico Eco:** Ejecuta un servidor y un cliente en el mismo lenguaje, envía mensajes y escribe `salir` para realizar el cierre ordenado.
2. **Concurrencia Multicliente:** Mantén 1 servidor ejecutándose y abre 2 o más clientes al mismo tiempo en terminales distintas; observa los logs `[Cliente #1]`, `[Cliente #2]` atendiéndose en paralelo sin bloqueo.
3. **Interoperabilidad Heterogénea:**
   - Inicia `ServidorCSharp` y conéctate usando `ClienteJava`.
   - Inicia `ServidorJava` y conéctate usando `ClienteCSharp`.
4. **Manejo de Errores:**
   - Intenta levantar dos servidores en el mismo puerto `5000` (detección de puerto ocupado).
   - Ejecuta un cliente sin servidor activo (conexión rechazada / timeout).
   - Cierra bruscamente una terminal cliente con `Ctrl+C` y comprueba que el servidor detecta la caída sin detenerse.

---

## 📄 Documentación

- [Reporte Técnico (TecNM)](docs/REPORTE_TECNICO.md)
- [Guía de Pruebas y Captura en Wireshark](docs/GUIA_PRUEBAS_Y_WIRESHARK.md)
