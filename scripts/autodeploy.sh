#!/bin/sh
CHANGEDMASTER=""
while [ true ]
do
	cd /opt/IN4391/
	echo "Checking repository for updates.."
	git fetch origin
	CHANGEDMASTER=$(git log HEAD..origin/master --oneline)
	CHANGEDMASTER=TE
	echo $CHANGEDMASTER
	if [ ! -z "$CHANGEDMASTER" ]; then
		echo "server needs to be updated!"
		git checkout master
		git reset master --hard
		git pull
		echo "killing old server"
		sudo stop IN4391-server
		echo "Building new server"
		pwd
		mvn clean install
		echo "Starting new server"
		sudo start IN4391-server
	else
		echo "server up-to-date"
	fi
	echo "sleeping for 30 seconds..."
	sleep 30
done
