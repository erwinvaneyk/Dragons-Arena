## Getting Influxdb and Grafana
In order to collect awesome amounts of data and turn them in useful metrics, this system uses [InfluxDB](http://influxdb.com/) and [Grafana](http://grafana.org/).
InfluxDB is a time-series database that you can perform standard functions on like min, max, sum, and more.
Grafana is metrics dashboard and graph editor for, among others, InfluxDB.

### Installation
1. Download [Docker](https://www.docker.com/), as we use docker for an easy setup and deployment of the system.
2. Open up your favorite console and navigate to root of this system.
2. Load the Docker archive into your Docker Images: `docker load -i docker/influxdb-grafana.docker`. Note: that `docker/influxdb-grafana.docker` is the relative location of the Docker archive and may be different on your system.
3. Run `docker images` to see if the image is actually added. Copy the **IMAGE ID**.
4. (Optional) Tag the image `docker tag <IMAGE-ID> in4391/influxdb-grafana` to give it a readable name. Replace `<IMAGE-ID>` with the image id from step 3.

### Running Influxdb and Grafana Docker image
- First time: `docker run -d -p 80:80 -p 8083:8083 -p 8084:8084 -p 8086:8086 in4391/influxdb-grafana`. The `run` command **destroys** any previous process of this image and creates a new one. Note: that this will erase all data of any previous sesion.
- To stop the docker container: `docker stop <container-id>`. Get the container-id of the container by running `docker ps`
- To restart: `docker start <container-id>`
- To destroy: `docker rm <container-id>`

### Usage
- For the InfluxDB dashboard: [http://localhost:8083](http://localhost:8083) (User management and database creation; Nothing interesting)
- For the Grafana dashboard: [http://localhost:80](http://localhost:80) - Note: Grafana requires two databases to be present, namely `grafana` and `data`. You can either create them by hand on the InfluxDB dashboard or run the game. The game will automatically add the databases if they are not created yet.

