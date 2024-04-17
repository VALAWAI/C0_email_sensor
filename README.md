# C0_email_sensor

This C0 VALAWAI component extracts information from e-mails and propagate
them to all the infrastructure.

## Summary

 - Type: C0
 - Name: E-mail sensor
 - API: [1.0.0 (April 15, 2024)](https://raw.githubusercontent.com/VALAWAI/C0_email_sensor/ASYNCAPI_1.0.0/asyncapi.yml)
 - VALAWAI API: [1.1.0](https://raw.githubusercontent.com/VALAWAI/MOV/ASYNCAPI_1.1.0/asyncapi.yml)
 - Developed By: [IIIA-CSIC](https://www.iiia.csic.es)
 - License: [GPL 3](LICENSE)
 
## Generate Docker image
 
 
## Deploy

 
 
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

 * __RabbitMQ__  the server to manage the messages to interchange with the components.
 The management web interface can be opened at **http://localhost:8081** with the credential
 **mov**:**password**.
 * __MongoDB__  the database to store the data used by the MOV. The database is named as **movDB** and the user credentials **mov:password**.
 The management web interface can be opened at **http://localhost:8081** with the credential
 **mov**:**password**.
 * __Mongo express__  the web interface to interact with the MongoDB. The web interface
  can be opened at **http://localhost:8082**.
 * __Mail trapper__  the component that simulates a mail server.
  The web interface can be opened at **http://localhost:8083**.
 * __Master of VALAWAI__  the component that mantains the topology connections between components.
  The web interface can be opened at **http://localhost:8084**.
