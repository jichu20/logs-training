# logs-training

## NEXUS

Para arrancar el contenedor de nexus

```sh
docker run --rm -d -p 8882:8081 --name nexus sonatype/nexus3
```

Para parar el contenedor de nexus

```sh
docker stop --time=120 nexus
```

Para obtener la contraseña

```sh
docker exec -it nexus cat /nexus-data/admin.password
```

## BUILD IMAGE

-v /Users/borja.sanchez/.m2:/root/.m2

docker build -t jichu20/logger-server2 -v /Users/borja.sanchez/.m2:/root/.m2 -f src/main/docker/dockerfile2 .
