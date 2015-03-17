#!/usr/bin/env bash
# sudo apt-get update
# sudo apt-get install maven git openjdk-7-jdk -y

sudo bash -c "'StrictHostKeyChecking no' >> /etc/ssh/ssh_config"
# eval "$(ssh-agent -s)"
cp /vagrant/config/IN4391_rsa ~/.ssh/id_rsa
cp /vagrant/config/IN4391_rsa.pub ~/.ssh/id_rsa.pub
# ssh-add /vagrant/config/IN4391_rsa

echo "Setting up service for IN4391-server"
sudo cp /vagrant/scripts/IN4391-server.conf /etc/init/IN4391-server.conf

echo "Doing git stuff..."
cd /opt/
pwd
# git clone git@github.com:erwinvaneyk/IN4391.git
cd /opt/IN4391/
echo "Building server from source..."
pwd
mvn install
echo "Starting IN4391-server service..."
sudo start IN4391-server


echo "Running autodeploy in the background.."
sudo cp /vagrant/scripts/autodeploy.sh /tmp/autodeploy.sh
nohup /tmp/autodeploy.sh > /tmp/autodeploy.log 2>&1 &
