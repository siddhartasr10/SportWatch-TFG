# 🏆 SportWatch-TFG

Plataforma de **streaming de deportes** que permite a diferentes proveedores unificarse para ofrecer planes completos y **ofertas conjuntas** a los usuarios.

![Presentacion-ezgif com-optimize](https://github.com/user-attachments/assets/bdc47d7a-68ea-49e5-9833-96e7a9c3704a)

---

## 🍔 Como Uber Eats, pero con deportes

SportWatch se lleva el **0% de lo que generen los proveedores** y les ofrece una plataforma para coordinarse y mejorar su servicio a los usuarios.

📊 [Modelo de datos del proyecto (Diagrama ER)](https://dbdesigner.page.link/XkXt13BZTccx8t896)

---

# 🔍 Análisis del Proyecto

## 1️⃣ Descripción General

- **🎯 Objetivo Principal**:  
  Crear una plataforma de streaming de vídeo que permita a los proveedores de deportes externalizar la creación de una plataforma web internacional, enfocándose solo en la **gestión de su contenido y canales**.

- **👥 Público Objetivo**:  
  Canales de deportes de pago que:
  - No tengan plataforma OTT.  
  - Prefieran ahorrar costes manteniendo una.  
  - La tengan restringida a un país.  
  - Su plataforma sea de baja calidad (lenta o poco usable).  

  Ejemplos: **ESPN, FoxSports (solo en EE. UU.), Skysports (solo UK), Diema sport, sport.ro**.

- **⚔️ Competidores**:  
  Parcialmente **DAZN** y **HBO/Max (Max Sports)**.

---

## 2️⃣ Funcionalidades Clave

1. ✅ **Autenticación de usuarios**  
2. ✅ **Streaming de vídeo**: subir contenido en directo y verlo desde el canal del autor.  
3. ✅ **Seguimiento y suscripción**: seguir o suscribirse (de pago) a autores para recibir notificaciones y acceso completo.  
4. ✅ **Feed dinámico de inicio**: muestra directos a usuarios registrados y parte del contenido a visitantes.  
5. ✅ **Comentarios y notificaciones**: sistema de comentarios en cada directo + notificaciones de respuestas y nuevos directos.  

---

## 3️⃣ Funcionalidades Opcionales

1. ✅ **Panel de suscripciones**: ver directos actuales y resubidos de streamers seguidos.  
2. ✅ **Directo resubido**: opción para dejar grabaciones disponibles en el perfil del streamer. (Actualmente todos se resuben por defecto).  
3. ⚪ **Administradores**: usuarios con rol especial de administrador (en MVP no requerido, se puede gestionar con Adminer).  
4. ⚪ **Plataforma de pago**: integración con **Stripe**.  

---

## 4️⃣ Requerimientos Técnicos

- **🖥️ Frontend**: Angular, HTML, CSS, [HLS.js](https://github.com/video-dev/hls.js)  
- **⚙️ Backend**: Java Spring Boot (Webflux), PostgreSQL, Adminer  
- **☁️ Integraciones**: AWS Streaming (SQS, IVS, S3)  
- **📦 Despliegue**: Docker (base de datos containerizada; backend puede empaquetarse en `.jar` y contenerizarse fácilmente).  

---

# 🛠️ Compilación del Frontend

El frontend de **SportWatch** se compila desde el `package.json` con el siguiente comando:

```json
"build": "find src/app/shared/services -maxdepth 3 -type f -name \\*.service.ts -exec sed -i 's/4200/8080/' {} \\; && ng build --configuration production --aot && find src/app/shared/services -maxdepth 3 -type f -name \\*.service.ts -exec sed -i 's/8080/4200/' {} \\; && rm -r ../SportWatch/src/main/resources/static/ ; cp -r dist/sport-watch-frontend/browser/ ../SportWatch/src/main/resources/static"
```
> Usando **`npm run build`** desde SportWatch_frontend/
