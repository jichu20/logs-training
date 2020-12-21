# Construcción y distribución de microservicios

Desde hace mucho tiempo, tengo claro que la mejor forma de distribuir una microservicio es empaquetarlo en un contenedor, o por lo menos, este es el mecanismo que mas posibilidades ofrece a un desarollador.

Esto es posiblo gracias a herramientas como docker, que nos permiten generar una imágen en muy poco tiempo y con un mecanismo muy sencillo. 

Hoy en día la mayoría de disrtibuidores, ofrecen su software empaquetado en contenedores (bases de datos, servidores, so, ...).

La gran duda que nos surge ahora, es ¿cual es el mejor camino para llegar a generar nuestros propias imagenes?. pues bien en un ambiente de microservicios desarrollados con spring boot y maven, disponemos de varias posibilidades.

## Maven + Docker

Por un lado disponemos de los pipelines de construcción utilizados de forma habitual en los cuales podemos disponer (por ejemplo) de un primer paso que se encarga de compilar y empaquetar el software (`mvn deploy`) en un jar y un segundo paso que se encarga de construir una imagen Docker con dicho jar a través de los comandos básicos de docker (`docker build .... `).

## Maven + plugin docker de maven

Tambien tenemos la posibilidad de incluir en nuestro archivo de configuración de maven (pom.xml) un plugin que se encargue de realizar la construccion de la imagen de docker, en este punto existen al menos dos plugins bastante conocidos, como son `Spotify Maven Docker plugin` y `Fabric8 Maven Docker plugin`. Estos pulugins son tan potentes que incluso permiten construir una imagen de docker sin disponer de un archivo Dockerfile con la definición del mismo, sustituyendo este por configuración incluida en el archivo pom.xml (desde mi punto de vista, esta caracteristica complica a definición de las imégenes).

Posiblemente, esta sea una de las opciones mas utilizadas en la industria ya que una configuración en un archivo pom padre, evita tener que replicar dicha configuración al resto de microservicios que forman parte del proyecto consiguiendo así que todos los proyectos se compilen de la mismo forma y con las mismas propiedades.

Quizas, esta sea la opción mas acomplada, ya que dichos plugins dependen enormemente del api de doker y por tanto son susceptibles de cambios en dicho api.

## Una tercera opción.

Si consideramos que docker es una de las herramientas mas potentes del mercado y nos aprovechamos de ella en tiempo de desarrollo para replicar bases de datos u otros sistemas, ¿por que no aprovecharnos de ella en fase de construcción?. Os propongo el uso de un archivo dockerfile como el siguiente:

```Dockerfile
FROM maven:3.5.2-jdk-8-alpine AS MAVEN_TOOL_CHAIN
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn package

FROM openjdk:8u252-jre-slim
COPY --from=MAVEN_TOOL_CHAIN /tmp/target/deploy-training-0.0.1-SNAPSHOT.jar /app/app.jar

CMD ["java", "-jar", "/app/app.jar"]
```

tal y como podeis ver, utilizaremos la imagen de docker `maven:3.5.2-jdk-8-alpine` para compilar nuestro proyecto, y una vez generado, podremos utilizar el resultado de la primera etapa (archivo jar) para generar nuestra imagen en una segunda etapa.

### Ventajas

Las mayores ventajas de este mecanismo son:

- No necesitaremos disponer de diferentes sclavos en nuestros sistemas de CI/CD con complejas configuración de versiones o instalación de productos.
- Si queremos cambiar la versión de maven, solo necesitaremos modificar la primera line para indicar la nueva versión de maven a utilizar.
- Si en lugar de maven, queremos utilizar Gradle, solo necesitaremos modificar la primera línea y la linea numero 6.
- Sobre todo, no tendremos integración con el api del demonio de docker.
- Proceso de compilación basado en Docker
- El código fuente, las dependencias de Maven  y todo lo que se encuentra en la carpeta de destino de Maven NO se incluyen en la imagen final.

