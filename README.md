# 🚀 Guía de Ejecución del Servidor SportWatch

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

1. Ejecutar el servidor directamente con Maven:
   ```bash
   mvn clean spring-boot:run
   ```

2. Levantar la base de datos (desde el directorio `db/`):
   ```bash
   cd db/
   docker compose up -d
   ```

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

✅ Con estos pasos y configuraciones, el servidor SportWatch debería funcionar correctamente.
