# Distributed Algorithms - Project Implementation

## Description
Project realised during Fall term of 2018-2019 for the class of Distributed Algorithms (taught by Prof. Gerraoui) during our Master at EPFL.
    
The goal is to implement a distributed system with reliable FIFO transmission channels over UDP. This will lay ground for the implementation of a blockchain on top of these reliable channels.

The implementations spawns a process for each node of our simulated distributed network. Each node uses a 'Best Effort Broadcast' logic to distirbute their messages to the network. Delivery of messages is managed according to a 'FIFO' and 'Causal Order' logic.

## Folders and structure 

- `src/`: Contains implementations of all p2p link, broadcasting and delivery abstractions. 
- `config/`:
	- `/membership`: Text file containing the topology of the network (process_ids, ip addresses and ports of each process simulating a node). 
- `tests/`:
	- `check_correctness_fifo.sh` and `check_correctness_causal.sh`: Scripts simulating the execution of different scenarios within a network. It spawns processes and simulates crashes and stops random processes to analyse the resilience of our network. 
	- `check_performance_fifo.sh`: Script simulating the execution within a perfect network (no package losses, no crash events, no delays). Running this scripts spawns 10 processes attempting to send 1000 messages each within 4 seconds. The average number of message delivered for each  process during that time serves as a performance parameter.
	- `/check_fifo.py`: Helper used to check that the processes obeys the FIFO broadcast logic
	- `/check_output.sh`: Helper used to check the correctness (according to FIFO logic) of the .out log files that are generated by the processes during the simulation.

## Running the code
  * Run the Makefile by typing `make` at the root of the project to compile the java classes
  * Then you can spawn a new process through the command. 

`java Da_proc <process_id> <location of membership file> <nr_messages_to_broadcast>`, for example `java Da_proc 1 ./config/membership 10`.

Parameters:
  * `nr_messages_to_broadcast`: amount of messages to broadcast during this run
  * `process_id`: the processes spawned should have the `process_id` as described in the membership file found at the root of the project.
  * After the simulation, each process will generate a log of its activity at the root of the project as `da_proc_<process_id>.out`.


`make clean` deletes all generated files.

## Repository Collaborators

  * Caviggia, Guillaume
  * Beer, Carolin
  * Pase, Francesco
