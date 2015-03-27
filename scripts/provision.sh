#!/usr/bin/env bash


#sudo apt-get update
#sudo apt-get install wget maven git openjdk-7-jdk -y
#wget -qO- https://get.docker.com/ | sh
#sudo usermod -aG docker vagrant


sudo docker stop $(sudo docker ps -a -q)
sudo docker rm $(sudo docker ps -a -q)

#####################################
sudo docker run -d -p 8083:8083 -p 8086:8086 \
--expose 8090 --expose 8099 \
tutum/influxdb

#####################################
#sudo docker run -d -p 9200:9200 -p 9300:9300 \
#-v /vagrant:/data \
#dockerfile/elasticsearch \
# /elasticsearch/bin/elasticsearch -Des.config=/data/elasticsearch.yml

######################################
sudo docker run -d -p 80:80 \
-e INFLUXDB_HOST=localhost \
-e INFLUXDB_PORT=8083 \
-e INFLUXDB_NAME=IN4391 \
-e INFLUXDB_USER=root \
-e INFLUXDB_PASS=root \
-e INFLUXDB_IS_GRAFANADB=true \
-e HTTP_USER=admin \
-e HTTP_PASS=admin \
tutum/grafana

#sudo bash -c "'StrictHostKeyChecking no' >> /etc/ssh/ssh_config"
#eval "$(ssh-agent -s)"
#ssh-add /vagrant/config/IN4391_rsa
#cp /vagrant/config/IN4391_rsa ~/.ssh/id_rsa
#cp /vagrant/config/IN4391_rsa.pub ~/.ssh/id_rsa.pub

# git clone https://github.com/kamon-io/docker-grafana-influxdb.git
# cd docker-grafana-influxdb/
# docker run -d -p 80:80 -p 8083:8083 -p 8084:8084 -p 8086:8086 --name grafana-influxdb_con grafana_influxdb


#echo "Setting up service for IN4391-server"
#sudo cp /vagrant/scripts/IN4391-server.conf /etc/init/IN4391-server.conf

#echo "Doing git stuff..."
#cd /opt/
#pwd
# git clone git@github.com:erwinvaneyk/IN4391.git
#cd /opt/IN4391/
#echo "Building server from source..."
#mvn install
#echo "Starting IN4391-server service..."
#sudo start IN4391-server


#echo "Running autodeploy in the background.."
#sudo cp /vagrant/scripts/autodeploy.sh /tmp/autodeploy.sh
#nohup /tmp/autodeploy.sh > /tmp/autodeploy.log 2>&1 &
