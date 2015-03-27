### Installation of InfluxDB
wget http://s3.amazonaws.com/influxdb/influxdb_latest_amd64.deb
sudo dpkg -i influxdb_latest_amd64.deb

### Running influx
sudo /etc/init.d/influxdb start


https://registry.hub.docker.com/u/tutum/grafana/

#####################################
sudo docker run -d -p 8083:8083 -p 8086:8086 \
--expose 8090 --expose 8099 \
tutum/influxdb

#####################################
sudo docker run -d -p 9200:9200 -p 9300:9300 \
-v /vagrant:/data \
dockerfile/elasticsearch \
 /elasticsearch/bin/elasticsearch -Des.config=/data/elasticsearch.yml

<data-dir>/elasticsearch.yml
path:
  logs: /data/log
  data: /data/data

######################################
sudo docker run -d -p 80:80 \
-e INFLUXDB_HOST=localhost \
-e INFLUXDB_PORT=8083 \
-e INFLUXDB_NAME=testdb \
-e INFLUXDB_USER=root \
-e INFLUXDB_PASS=root \
-e INFLUXDB_IS_GRAFANADB=true \
-e HTTP_USER=admin \
-e HTTP_PASS=admin \
tutum/grafana

admin:Fzs6AXIzwCaj

docker save -o <save image to path> <image name>
docker load -i <path to image tar file>

sudo docker run -d -p 80:80 -p 8083:8083 -p 8084:8084 -p 8086:8086 erwinvaneyk/version2
