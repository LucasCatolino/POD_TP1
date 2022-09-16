# TPE1- Tickets de vuelo

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
    ./build.sh
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
1. Ubicar una consola en /server/target/tpe1-g12-server-1.0-SNAPSHOT y ejecutar el siguiente comando:
    ```
    ./run-registry.sh
    ```
2. Ubicar otra consola en /server/target/tpe1-g12-server-1.0-SNAPSHOT y ejecutar el siguiente comando:
    ```
    ./run-server.sh
    ```
    Al finalizar este paso se debería indicar "Server online" en la consola

## Corriendo los clientes
El proyecto cuenta con 4 servicios cliente, donde cada uno coincide con una interfaz remota. Los scripts para ejecutarlos se podrán encontrar en /client/target/tpe1-g12-client-1.0-SNAPSHOT. Para ejecutarlos, ubicar una nueva consola (manteniendo las dos del servidor funcionando), y ejecutar cualquiera de los siguientes comandos:
- FlightManagementClient
    ```
    ./run-admin -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName[ -DinPath=filename |  -Dflight=flightCode ]
    ```
    Donde:
    - xx.xx.xx.xx:yyyy: IP y puerto donde está publicado el servicio de administración de vuelos
    - actionName:
        - models: agrega un lote de modelos de aviones
        - flights: agrega un lote de vuelos
        - status: consulta el estado de vuelo con *flightCode*
        - cancel: cancela el vuelo con *flightCode*
        - reticketing: fuerza el cambio de tickets de vuelos cancelados por tickets de vuelos alternativos
- FlightNotificationClient
    ```
    ./run-notifcations -DserverAddress=xx.xx.xx.xx:yyyy-Dflight=flightCode -Dpassenger=name
    ```
     Donde:
    - xx.xx.xx.xx:yyyy: IP y puerto donde está publicado el servicio de notificaciones del vuelo
    - flightCode: código del vuelo
    - name: nombre del pasajero
- SeatAssignmentClient
    ```
    ./run-seatAssign -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName-Dflight=flightCode [ -Dpassenger=name | -Drow=num | -Dcol=L | -DoriginalFlight=originFlightCode ]
    ```
    Donde:
    - xx.xx.xx.xx:yyyy: IP y puerto donde está publicado el servicio de asignación de asientos
    - actionName:
        - status: imprime en pantalla si el asiento de fila *num* y columna *L* del vuelo *flightCode* está libre u ocupado
        - assign: asigna al pasajero *name* al asiento libre en la fila *num*, columna *L* del vuelo *flightCode*
        - move: mueve al pasajero *name* de un asiento en el vuelo *flightCode* a un asiento libre en el mismo vuelo, ubicado en la fila *num*, columna *L*
        - alternatives: lista los vuelos alternativos al vuelo *flightCode* para el pasajero *name*. Para cada categoría de asiento en cada vuelo alternativo lista:
            - Código de aeropuerto destino
            - Código de vuelo
            - Cantidad de asientos asignables de la categoría
            - Categoría de los asientos asignables
        - changeTicket: cambia el ticket del pasajero *name* de un vuelo con código *originFlightCode* a otro vuelo alternativo de código *flightCode*
- SeatMapConsultationClient
    ```
    ./run-seatMap -DserverAddress=xx.xx.xx.xx:yyyy -Dflight=flightCode [-Dcategory=catName | -Drow=rowNumber ] -DoutPath=output.csv
    ```
     Donde:
    - xx.xx.xx.xx:yyyy: IP y puerto donde está publicado el servicio de consulta del mapa de asientos
    - Si no se indica *-Dcategory* ni *-Drow* se resuelve la consulta de asientos del vuelo
    - Si se indica *-Dcategory*, *catName* es el nombre de la categoría de asiento elegido para resolver la consulta de asientos del vuelo por categoría indicada
    - Si se indica *-Drow*, *rowNumber* es el número de la fila de asientos para resolver la consulta de asientos del vuelo por fila indicada
    - Si no se indica *-Dflight*, la consulta falla
    - Si se indican *-Dcategory* y *-Drow*, la consulta falla
    - *output.csv* es el path del archivo de salida con los resultados de la consulta elegida