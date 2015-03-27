## Getting Influxdb and Grafana
In order to collect awesome amounts of data and turn them in useful metrics, this system uses [InfluxDB](http://influxdb.com/) and [Grafana](http://grafana.org/).
InfluxDB is a time-series database that you can perform standard functions on like min, max, sum, and more.
Grafana is metrics dashboard and graph editor for, among others, InfluxDB.

### Installation
1. Download [Docker](https://www.docker.com/), as we use docker for an easy setup and deployment of the system.
2. Open up your favorite console and navigate to the folder `<project root>/docker/`.
2. Create a docker image from the source: `docker build -t in4391/influxdb-grafana docker-influxdb-grafana`.
3. Run `docker images` to see if the image `in4391/influxdb-grafana` is actually added.

### Running Influxdb and Grafana Docker image
- First time: `docker run -d -p 80:80 -p 8083:8083 -p 8084:8084 -p 8086:8086 in4391/influxdb-grafana`. The `run` command **destroys** any previous process of this image and creates a new one. Note: that this will erase all data of any previous sesion.
- To stop the docker container: `docker stop <container-id>`. Get the container-id of the container by running `docker ps`
- To restart: `docker start <container-id>`
- To destroy: `docker rm <container-id>`

### Usage
- For the InfluxDB dashboard: [http://localhost:8083](http://localhost:8083) (User management and database creation; Nothing interesting)
- For the Grafana dashboard: [http://localhost:80](http://localhost:80) - Note: Grafana requires two databases to be present, namely `grafana` and `data`. You can either create them by hand on the InfluxDB dashboard or run the game. The game will automatically add the databases if they are not created yet.

Enjoy!

![Measure ALL the things](http://www.nedpoulter.com/wp-content/uploads/2014/04/picard-data-meme.png?b9f682)