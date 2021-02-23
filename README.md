# DDC Data Viewer

## Description

Web UI to view data in DDC (encrypted or decrypted).

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Environment variables

|Variable|Description|Default value|
|---|---|---|
|DDC_BOOTNODE|DDC node to get network topology|`http://ddc-bootnode:8080`|

## Requirements for development

- JDK 11 or higher
- Docker and Docker Compose

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./gradlew quarkusDev
```

## Run tests

```shell script
./gradlew test
```

## Build JVM image

```shell script
./gradlew quarkusBuild
```

## Build native image

```shell script
./gradlew buildNative
```
