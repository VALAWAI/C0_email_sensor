# C0_email_sensor

The C0 e-mail sensor component extracts information from e-mails and propagates it
to all the infrastructure. Thus, this component fetches the e-mails of a user
account in an email server, normalizes them and publishes them in the channel
**valawai/c0/email_sensor/data/e_mail**. Also, it provides the possibility to change
the component parameters if you send a message to the queue 
**valawai/c0/email_sensor/control/change_parameters**. You can read more about
this service and the payload of the message on the [aysncapi](asyncapi.yaml)
or on the [component documentation](https://valawai.github.io/docs/components/C0/email_sensor).


## Summary

 - Type: C0
 - Name: E-mail sensor
 - Version: 1.2.0 (October 16,2024)
 - API: [1.0.0 (March 16, 2024)](https://raw.githubusercontent.com/VALAWAI/C0_email_sensor/ASYNCAPI_1.0.0/asyncapi.yml)
 - VALAWAI API: [1.2.0 (March 9, 2024)](https://raw.githubusercontent.com/valawai/MOV/ASYNCAPI_1.2.0/asyncapi.yml)
 - Developed By: [IIIA-CSIC](https://www.iiia.csic.es)
 - License: [GPL 3](LICENSE)
 
 
## Generate Docker image

The easy way to create the docker image of this component is to execute
the next script.
 
 ```
./buildDockerImages.sh
```

At the end you must have the docker image **valawai/c0_email_sensor:Z.Y.Z**
where **X.Y.Z** will be the version of the component. If you want to have
the image with another tag, for example **latest**, you must call the script
with this tag as a parameter, for example:

```
./buildDockerImages.sh latest
```

And you will obtain the container **valawai/c0_email_sensor:latest**.


### Docker environment variables

The most useful environment variables on the docker image are:

 - **RABBITMQ_HOST** is the host where the RabbitMQ is available.
  The default value is ___mov-mq___.
 - **RABBITMQ_PORT** defines the port of the RabbitMQ.
  The default value is ___5672___.
 - **RABBITMQ_USERNAME** contains the name of the user that can access the RabbitMQ.
  The default value is ___mov___.
 - **RABBITMQ_PASSWORD** is the password to authenticate the user that can access the RabbitMQ.
  The default value is ___password___.
 - **MAIL_PROTOCOL** defines the protocol to connect to the e-mail server to fetch the e-mails.
  It can be **pop3**, **pop3s**, **imap** or **imap2**. The default value is ___imaps___.
 - **MAIL_HOST** is the host to the e-mail server. The default value is ___mail___.
 - **MAIL_PORT** defines the port of the e-mail server. The default value is ___993___.
 - **MAIL_SECURED** this is **true** is the connection uses the secured TLS.
  The default value is ___true___.
 - **MAIL_USERNAME** contains the name of the user that can access the e-mail server.
  The default value is ___user___.
 - **MAIL_PASSWORD** defines the credential to authenticate the user that can access the e-mail server.
  The default value is ___password__.
 - **C0_EMAIL_SENSOR_FETCHING_INTERVAL** contains the seconds that have to pass between fetching e-mail
  intervals. The default value is ___60__.
 - **LOG_LEVEL** defines the level of the log messages to be stored.
  The default value is ___INFO__.
 - **QUARKUS_HTTP_HOST** contains the server host that will expose the REST health endpoints.
 The default value is __0.0.0.0__.
 - **QUARKUS_HTTP_PORT** defines the server port that will expose the REST health endpoints.
 The default value is __8080__.

The component is developed using [Quarkus](https://quarkus.io/), so you can change any environment
variable [defined on it](https://quarkus.io/guides/all-config).


### Docker health check

This component exposes the following REST endpoints to check their health status.

 - **/q/health/live** can be used to check if the component is running.
 - **/q/health/ready** can be used to check if the component can process the messages
  from the VALAWAI infrastructure.
 - **/q/health/started** can be used to check if the component has started.
 - **/q/health** can be used to obtain all the previous check procedures in the component.
 
All of them will return a JSON which will have the **status** of the state (**UP** or **DOWN**)
and the list of **checks** that have been evaluated. It looks like the following example was obtained
from doing a **GET** over the **/q/health** endpoint.

 
 ```json
 {
    "status": "UP",
    "checks": [
        {
            "name": "SmallRye Reactive Messaging - liveness check",
            "status": "UP",
            "data": {
                "registered": "[OK]",
                "change_parameters": "[OK]",
                "send_log": "[OK]",
                "send_unregister_component": "[OK]",
                "send_register_component": "[OK]",
                "send_email": "[OK]"
            }
        },
        {
            "name": "Registered C0 email sensor",
            "status": "UP"
        },
        {
            "name": "SmallRye Reactive Messaging - readiness check",
            "status": "UP",
            "data": {
                "registered": "[OK]",
                "change_parameters": "[OK]",
                "send_log": "[OK]",
                "send_unregister_component": "[OK]",
                "send_register_component": "[OK]",
                "send_email": "[OK]"
            }
        },
        {
            "name": "SmallRye Reactive Messaging - startup check",
            "status": "UP"
        }
    ]
}
 ```
 
An alternative is to see the state of the component using the health user interface that
is exposed at [/q/health-ui/](http://localhost:8080/q/health-ui/).
 
These endpoints are useful to do the **healthcheck** in a **docker compose**. Thus, you can add
the following section into the service of the component.

```
    healthcheck:
      test: ["CMD-SHELL", "curl -s http://localhost:8080/q/health | grep -m 1 -P \"^[\\s|\\{|\\\"]+status[\\s|\\:|\\\"]+.+\\\"\" |grep -q \"\\\"UP\\\"\""]
      interval: 1m
      timeout: 10s
      retries: 5
      start_period: 1m
      start_interval: 5s
```

Finally, remember that the  docker environment variables **QUARKUS_HTTP_HOST** and **QUARKUS_HTTP_PORT**
can be used to configure where the REST health endpoints will be exposed by the component.


## Deploy

On the file [docker-compose.yml](docker-compose.yml), you can see how the docker image
of this component can be deployed on a valawai environment. On this file are defined
the profiles **mov** and **mail**. The first one is to launch
the [Master Of Valawai (MOV)](https://github.com/VALAWAI/MOV) and the second one is to start
a [mocked e-mail server](https://github.com/dbck/docker-mailtrap). You can use the next
command to start this component with the MOV and the mail server.

```
COMPOSE_PROFILES=mov,mail docker compose up -d
```

After that, if you open a browser and go to [http://localhost:8080](http://localhost:8080)
you can view the MOV user interface. Also, you can access the RabbitMQ user interface
at [http://localhost:8081](http://localhost:8081) with the credentials **mov:password**.
Finally, you can access the mail server user interface at [http://localhost:8082](http://localhost:8082)
with the credentials **user:password**.

The docker compose defines some variables that can be modified by creating a file named
[**.env**](https://docs.docker.com/compose/environment-variables/env-file/) where 
you write the name of the variable plus equals plus the value.  As you can see in
the next example.

```
MQ_HOST=rabbitmq.valawai.eu
MQ_USERNAME=c0_email_sensor
MQ_PASSWORD=lkjagb_ro82t¿134
```

The defined variables are:


 - **C0_EMAIL_SENSOR_TAG** is the tag of the C0 email sensor docker image to use.
  The default value is ___latest___.
 - **MQ_HOST** is the hostname of the message queue broker that is available.
  The default value is ___mq___.
 - **MQ_PORT** is the port of the message queue broker is available.
  The default value is ___5672___.
 - **MQ_UI_PORT** is the port of the message queue broker user interface is available.
  The default value is ___8081___.
 - **MQ_USER** is the name of the user that can access the message queue broker.
  The default value is ___mov___.
 - **MQ_PASSWORD** is the password to authenticate the user that can access the message queue broker.
  The default value is ___password___.
 - **MAIL_PROTOCOL** defines the protocol to connect to the e-mail server to fetch the e-mails.
  It can be **pop3**, **pop3s**, **imap** or **imap2**. The default value is ___imaps___.
 - **MAIL_HOST** is the host to the e-mail server. The default value is ___mail___.
 - **MAIL_PORT** defines the port of the e-mail server. The default value is ___993___.
 - **MAIL_SECURED** this is **true** is the connection uses the secured TLS.
  The default value is ___true___.
 - **MAIL_USERNAME** contains the name of the user that can access the e-mail server.
  The default value is ___user___.
 - **MAIL_PASSWORD** defines the credential to authenticate the user that can access the e-mail server.
  The default value is ___password__.
 - **MAILTRAP_TAG** is the tag of the [email server](https://github.com/dbck/docker-mailtrap) docker image to use.
  The default value is ___latest___.
 - **RABBITMQ_TAG** is the tag of the RabbitMQ docker image to use.
  The default value is ___management___.
 - **MONGODB_TAG** is the tag of the MongoDB docker image to use.
  The default value is ___latest___.
 - **MONGO_PORT** is the port where MongoDB is available.
  The default value is ___27017___.
 - **MONGO_ROOT_USER** is the name of the root user for the MongoDB.
  The default value is ___root___.
 - **MONGO_ROOT_PASSWORD** is the password of the root user for the MongoDB.
  The default value is ___password___.
 - **MONGO_LOCAL_DATA** is the local directory where the MongoDB will be stored.
  The default value is ___~/mongo_data/movDB___.
 - **DB_NAME** is the name of the database used by the MOV.
  The default value is ___movDB___.
 - **DB_USER_NAME** is the name of the user used by the MOV to access the database.
  The default value is ___mov___.
 - **DB_USER_PASSWORD** is the password of the user used by the MOV to access the database.
  The default value is ___password___.
 - **MOV_TAG** is the tag of the MOV docker image to use.
  The default value is ___latest___.
 - **MOV_UI_PORT** is the port where the MOV user interface is available.
  The default value is ___8080___.

The database is only created the first time where script is called. So, if you modify
any of the database parameters you must create again the database. For this, you must
remove the directory defined by the parameter **MONGO_LOCAL_DATA** and start again
the **docker compose**.

You can stop all the started containers with the command:

```
COMPOSE_PROFILES=mov,mail docker compose down
```
  
## Development

You can start the development environment with the script:

```shell script
./startDevelopmentEnvironment.sh
```

After that, you have a bash shell where you can interact with
the Quarkus development environment. You can start the development
server with the command:

```shell script
startServer
```

Alternatively, to run the test using the started Quarkus client, you can use Maven.

 * __mvn test__  to run all the tests
 * __mvnd test__  to run all the tests on debugging mode.
 * __mvn -DuseDevMOV=true test__  to run all the tests using the started Master of VALAWAI,
 	instead of an independent container.

Also, this starts the tools:

 * **RabbitMQ**  the server to manage the messages to interchange with the components.
 The management web interface can be opened at [http://localhost:8081](http://localhost:8081) with the credential
 **mov**:**password**.
 * **MongoDB**  the database to store the data used by the MOV. The database is named as **movDB** and the user credentials **mov:password**.
 * **Mongo express**  the web interface to interact with the MongoDB. The web interface
  can be opened at [http://localhost:8082](http://localhost:8082) with the credential
 **mov**:**password**.
 * **Mail catcher**  the component that capture the sent mails.
  The web interface can be opened at [http://localhost:8083](http://localhost:8083).
 * **Master of VALAWAI**  the component that mantains the topology connections between components.
  The web interface can be opened at [http://localhost:8084](http://localhost:8084).


## Links

 - [C0 E-mail sensor documentation](https://valawai.github.io/docs/components/C0/email_sensor)
 - [Master Of VALAWAI tutorial](https://valawai.github.io/docs/tutorials/mov)
 - [VALWAI documentation](https://valawai.github.io/docs/)
 - [VALAWAI project web site](https://valawai.eu/)
 - [Twitter](https://twitter.com/ValawaiEU)
 - [GitHub](https://github.com/VALAWAI)