# IN4391
Distributed Systems - game

### TODO
- [x] Split up the code of the Core into Client and Server (replace/remove threads)
- [ ] Implement distributed logging
- [ ] Implement two message-queues (IncomingQueue and CurrentQueue)*
- [ ] Implement synchronization (1-phase, use timestamp checks)
- [ ] Implement functionality for server to take tasks from the queue and send result to client
- [ ] Implement message buffering in both server and client
- [ ] Implement functionality for disconnecting/reconnecting clients and servers

### Implementation details
##### Queues:
- Only have 1 message of a player at any time
- If it receives a message from a player, who already has a message in the queue, it will throw out the oldest message (based on the timestamp)

##### Round procedure:
- move IncomingQueue to CurrentQueue
- empty IncomingQueue, accept new messages
- servers perform actions of the CurrentQueue
	- servers with a task x will wait until all tasks in the currentQueue before task x are executed.
- If CurrentQueue is empty, send synchronize-messages to everyone.

##### Server Acknowledgement
- Each server has an independent API (in this case a RMI registry).
- A new server to the cluster, calls the *acknowledge*-method of the server it contacts.
- The receiving server adds a binding for the new server (and its clients).

### Requirement check:
- [ ] **System operation requirements**: use data from a real workload trace taken from the Game Trace Archive
- [ ] **Fault tolerance**: client and server nodes may crash and restart.  all game and system events must be logged in the order in which they occur, on at least two server nodes.
- [ ] **Scalability requirements**: contains 100 players, 20 dragons, and 5 server nodes, and when it runs until there is only one class of remaining participants

### Additional requirements:
- [ ] **Advanced fault-tolerance**: through tolerance for and analysis of the impact of multiple failures or other failure models than fail-stop on the DAS system
- [ ] **Multi-tenancy**: support multiple users running simulations for the DAS system, through a queue of simulation jobs and adequate scheduling policies.
- [ ] **Repeatability**: making sure that the outcome of the simulations is always the same for the same input, through the use of priority numbers for concurrent simulated events
- [ ] **Benchmarking**: by running service workloads designed to stress particular elements of the system.
- [ ] **Security**: by implementing access lists, security groups, and a byzantine fault-tolerance algorithm, etc.
- [ ] **Portfolio scheduling**: by adapting the concept of portfolio scheduling to the Dragon Arena System.
