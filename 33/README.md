# Distributed Algorithms - Project 1 - Team 33

## Description
Project realised during the Autommn semester of 2018-2019, for the class of Distributed Algorithms (taught by M.Gerraoui) during our Master at EPFL.
    
The goal is to implement a distributed payment system through reliable FIFO transmission channels over UDP. This will lay ground for the implementation of a blockchain on top of these reliable channels.

The way that we implement it is by spawning a process for each node of our simulated distributed network. This host will communicate its messages with the rest of the network using a 'Best Effort Broadcast' logic, and deliver the messages of the rest of the network using a 'FIFO delivery logic'.

The programming language used is java.

## Folders and structure 

- /check_fifo.py : helper used to check that the processes indeed work with a FIFO broadcast logic
- /check_output.sh : helper used to check the correctness (according to FIFO logic) of the .out log files, generated after each run of the simulation by each process.
- /check_correctness_java.sh : script simulating the execution of a scenario within a network. Running this scripts spawns processes and implement a scenario of unperfect execution (e.g crashes or stops random processes) to analyse the resilience of our network.
- /check_performance_java.sh : script simulating the execution of a scenario with perfect links with the network (no loss, no crash event, no delay). Running this scripts spawns 10 processes attempting to each send 1000 messages in 4 seconds. The average message delivered for each process give us an idea of our performance.
- /Makefile : run 'make' to compile all java classes. Run 'make clean' to clean the generated files.
- /membership : text file containing the topology of the network (process_ids, ip addresses and ports of each process simulating a distributed host). One can modify this file to simulate a new topology.


## Running the code
- Run the Makefile at the root of the project to compile the java classes
- Then you can spawn a new process through the command 'java Da_proc {process_id} membership {nr_messages_to_broadcast}':
    - {nr_messages_to_broadcast} : amount of messages to broadcast during this run
    - {process_id} : the processes spawned should have the {process_id} as described in the membership file found at the root of the project.
- After the simulation, each process will generate a log of its activity at the root of the project as 'da_proc_{process_id}.out'.

## Repository Collaborators

  - Caviggia Guillaume
  - Beer Carolin
  - Pase Francesco
