#!/bin/bash

java -jar \
-Djava.security.policy=../my.policy \
-Djava.security.manager \
-Djava.security.debug=access,failure \
../target/IN4391-server.jar \
4444 \
