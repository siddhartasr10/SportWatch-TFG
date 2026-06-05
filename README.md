# 🚀 Guía de Ejecución de SportWatch

Este documento explica cómo ejecutar el servidor **SportWatch** en dos modalidades, junto con la configuración de base de datos y credenciales necesarias.

---

## ⚙️ 1. Ejecución desde el JAR

1. Compilar y generar el JAR si aún no existe:
   ```bash
   mvn clean package
   ```

2. Ejecutar el servidor:
   ```bash
   java -jar target/SportWatch-1.0.jar
   ```

3. Levantar la base de datos (desde el directorio `db/`):
   ```bash
   cd db/
   docker compose up -d
   ```

---

## ⚙️ 2. Ejecución desde el código fuente (`sportwatch/`)

### 🔹 Opción A: Manual con Maven

1. Ejecutar el servidor directamente con Maven:
   ```bash
   mvn clean spring-boot:run
   ```

2. Levantar la base de datos (desde el directorio `db/`):
   ```bash
   cd db/
   docker compose up -d
   ```

### 🔹 Opción B: Con el script `quickstart.sh`

El proyecto incluye un script para facilitar la ejecución automática del servidor y la base de datos.

**Contenido de `quickstart.sh`:**
```bash
#!/usr/bin sh

$(sudo systemctl start docker);
$(gnome-terminal --tab -- sh -c "cd SportWatch; mvn clean spring-boot:run");
$(gnome-terminal --tab -- sh -c "cd db; docker compose up -d");
```
> Esta script esta enfocada en el desarrollo y no funcionará si no tienes la terminal de gnome.

**Ejecución:**
```bash
chmod +x quickstart.sh
./quickstart.sh
```

Este script:
- Inicia el servicio **Docker**.
- Abre una terminal y arranca el servidor con `mvn clean spring-boot:run`.
- Abre otra terminal y levanta la base de datos con `docker compose up -d`.

---

## 🗄️ 3. Configuración de la Base de Datos y Credenciales

La base de datos se configura en el archivo `application.properties`.

### 🔧 Configuración de la base de datos

```properties
spring.r2dbc.url=r2dbc:postgresql://localhost:5432/sportwatch
spring.r2dbc.username=user
spring.r2dbc.password=password
```

### 🔑 Claves necesarias

El servidor necesita las siguientes claves, configuradas también en `application.properties`:

```properties
jwt.public-key-path=secrets/certs/jws-public-key.der
jwt.private-key-path=secrets/certs/jws-private-key.der
aws.credentials.file=secrets/aws.txt
```

### 📂 Formato del archivo `aws.txt`

El archivo `secrets/aws.txt` debe contener:

- **Primera línea** → `accessKey`
- **Segunda línea** → `secretKey`

---
## ☁️ 4. Recursos AWS Utilizados

SportWatch utiliza varios servicios de AWS para gestionar el almacenamiento de contenido, la mensajería interna y las retransmisiones en directo.

Actualmente, los identificadores de estos recursos se encuentran definidos directamente en la clase `UploadService` mediante constantes y no a través de `application.properties`.

```java
private final String RECORDINGCONF = "...";
private final String SQSQUEUE = "...";
private final String QUEUEURL = "...";
private final String BUCKETNAME = "...";
```

### 📦 Amazon S3

La aplicación utiliza el bucket:

```text
streams-ivs
```

Este bucket almacena las grabaciones generadas durante las retransmisiones en directo.

### 📨 Amazon SQS

La aplicación utiliza la cola:

```text
stream-upload-queue
```

para gestionar eventos relacionados con la subida y procesamiento de grabaciones.

### 📺 Amazon IVS

Las retransmisiones se gestionan mediante Amazon IVS y utilizan una configuración de grabación (*Recording Configuration*) específica asociada a la cuenta AWS del proyecto.

---

### ⚠️ Importante si quieres recrear la funcionalidad

Los identificadores de AWS actualmente están ligados a una cuenta concreta y se encuentran hardcodeados en `UploadService`.

Si deseas desplegar SportWatch en tu propia cuenta AWS, deberás:

1. Crear tu propio bucket S3.
2. Crear una cola SQS equivalente.
3. Crear la configuración de grabación de Amazon IVS.
4. Sustituir los valores de las constantes en `UploadService` por los identificadores de tus recursos.
> O sustituirlo por variables en el `application.properties`
