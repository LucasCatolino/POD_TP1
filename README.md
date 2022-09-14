# POD_TP1

## Contenidos
- [Objetivo](#objetivo)
- [Prerrequisitos](#prerrequisitos)
- [Primeros pasos](#primeros-pasos)
- [Levantando el servidor](#levantando-el-servidor)
- [Corriendo los clientes](#corriendo-los-clientes)

## Objetivo
Implementar en grupos un sistema remoto thread-safe para la administración de asientos de vuelo de una aerolínea, a partir de la existencia de uno o más modelos de avión y los tickets de vuelo, permitiendo notificar a los pasajeros de los eventos y ofreciendo reportes de los mapas de asientos hasta el momento.

## Prerrequisitos
Para poder correr el proyecto se deberá tener instalado y funcional:
- [Maven](https://maven.apache.org/install.html)
- [Java 11](https://www.java.com/en/download/help/download_options.html)

## Primeros pasos
1. Clonar el repositorio del proyecto. Para esto se podrá utilizar la consola con el comando:
    ```
    git clone URL
    ```
2. Una vez clonado el proyecto, dar permisos de ejecución al script `build.sh` ubicado en la raíz del proyecto. Para eso se podrá utilizar la consola con el comando:
    ```
    chmod +x build.sh
    ```
    Este script es opcional. En caso de error se podrán correr los siguientes comandos a mano:
    I. Ubicar la consola en la raíz del proyecto y ejecutar el siguiente comando:
    ```
    mvn clean install
    ```
    II. Ubicar la consola en /client/target y ejecutar los siguientes comandos:
    ```
    tar xvzf tpe1-g12-client-1.0-SNAPSHOT-bin.tar.gz
    cd tpe1-g12-client-1.0-SNAPSHOT
    chmod -R +x *.sh
    ```
    III. Ubicar la consola en /server/target y ejecutar los siguientes comandos:
    ```
    tar xvzf tpe1-g12-server-1.0-SNAPSHOT-bin.tar.gz
    cd tpe1-g12-server-1.0-SNAPSHOT
    chmod -R +x *.sh
    ```
    Al finalizar este paso se tendrá el proyecto compilado y con los scripts listos para ejecutar el servidor

## Levantando el servidor
1. Ubicar una consola en /server/target y ejecutar el siguiente comando:
    ```
    ./run-registry.sh
    ```
2. Ubicar otra consola en /server/target y ejecutar el siguiente comando:
    ```
    ./run-server.sh
    ```
    Al finalizar este paso se debería indicar "Server online"

## Corriendo los clientes
El proyecto cuenta con 4 clientes, donde cada uno coincide con una interfaz remota:
- FlightManagementClient
- FlightNotificationClient
- SeatAssignmentClient
- SeatMapConsultationClient