# SportWatch-TFG
Plataforma de streaming de deportes que le da la posibilidad a diferentes proveedores de unificarse y dar planes completos y ofertas conjuntas.

## Como Uber Eats pero con deportes
SportWatch se lleva el 0% de lo que generen los proveedores y les da una plataforma para coordinarse y ofrecer un mejor servicio a los usuarios.

### [Modelo de datos del proyecto](https://dbdesigner.page.link/XkXt13BZTccx8t896) (Diagrama ER)

# Análisis del proyecto:
## 1. Descripción General
**Objetivo Principal**: Crear un sitio de streaming de vídeo que permita a los proveedores de deportes externalizar la creación de una plataforma web internacional para solo centrarse en la gestión de su contenido y sus canales de televisión.

**Público Objetivo**: Canales de deportes de pago que no tengan plataforma OTT, que la tengan pero prefieran ahorrar costes o que la tengan pero restringida a ese país. O en muchos casos que tengan plataforma pero sea bastante cutre/lenta.
Ej: ESPN, FoxSports (tiene pero unicamente con servidores en EEUU), Skysports (restringido a UK), canales de televisión de cualquier país que tengan los derechos de emisión del deporte pero solo funcionen por ese medio (Ej: Max sport, Diema sport, sport.ro)

**Competidores**: (en parte) DAZN, HBO/Max (Max sports)

## 2. Funcionalidades Clave
1. **Autenticación de usuarios**

2. **Streaming de vídeo**:  Se permite subir contenido en directo y se puede ver desde el canal del autor

3. **Servicio de seguimiento y subscripción**:  Los usuarios pueden seguir o subscribirse (de pago) a los autores para ser notificados de su contenido y tener el acceso total a este.

4. **Feed dinámico de inicio**:  La página de inicio muestra a los usuarios registrados y en parte a los no registrados algunos de los directos.
5. **Comentarios y Notificaciones**:  En cada directo se muestran unos comentarios y los usuarios pueden recibir notificaciones si les contestan un comentario o uno de los streamers a los que siguen empieza un directo.

### (Si da tiempo)

6. **Panel de suscripciones**: Cada usuario puede ver en su panel de suscripciones los directos actuales de cada uno de los streamers a los que siguen.

7. **Directo resubido**: Los streamers pueden decidir si dejar resubido el directo, una vez resubido se vera en su perfil y los usuarios podrán verlo en sus paneles de suscripción.

## 3. Requerimientos Técnicos
**Frontend**: Angular, HTML, CSS, [HLS.js](https://github.com/video-dev/hls.js)

**Backend**: Java Spring-boot (Webflux), PostgreSQL, 

**Integraciones**: AWS Streaming/Cloudflare Streaming

