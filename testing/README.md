# Local Testing

This `docker-compose` setup starts containers for Greenmail (mail server), MinIO (storage), and a
Flink cluster. It then configures Flink's SQL client to connect against that cluster.

## Prerequisites

Make sure you have the following tools installed:

* `docker`
* `docker-compose`
* `mailutils`

## Usage

Running the following script will build the project, copy the JAR to the correct location, build and
run the Docker images and start the SQL client:

```
./build_and_run.sh
```

You can also access a web interface for some services while the Docker containers are running:

1. MinIO: http://localhost:9000/
2. Flink UI: http://localhost:8081/

Make sure to run `docker-compose down` to shut down all containers when you're done.

## Sending Mails

You can send an email using

```
# bob -> alice
echo "Message" | mailx -Sv15-compat -Smta=smtp://bob:bob@localhost:3025 -s"Subject" alice@acme.org
```
