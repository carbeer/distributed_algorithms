# Distributed Algorithms - Project 1 - Team 32

## TODO
- clean_up files and prepare submission
- update Makefile with new structure of files
- comment the code
- benchmark our implementation

## Description
Project realised during the Autommn semester of 2018-2019, for the class of Distributed Algorithms (taught by M.Gerraoui) during our Master at EPFL.
    
The goal is to implement a distributed payment system through reliable FIFO transmission channels over UDP. This will lay ground for the implementation of a blockchain on top of these reliable channels.

The programming language used is java.

## Folders and structure 

- /check_fifo.py : helper used to check that the processes indeed work with a FIFO broadcast logic
- /check_output.sh : helper used to check the correctness (according to FIFO logic) of the .out log files, generated after each run of the simulation by each process.
- /check_correctness_java.sh : script simulating the execution of a scenario within a network. Running this scripts spawns processes and implement a scenario of unperfect execution, crashes random processes to analyse the resilience of our network.
- /Makefile : run 'make' to compile all java classes. Run 'make clean' to clean the generated files.
- /membership : text file containing the topology of the network (process_ids, ip addresses and ports of each process simulating a distributed host).


## Running the code
- Run the Makefile at the root of the project to compile the java classes
- Then you can spawn a new process through the command 'java Da_proc {process_id} membership {nr_messages_to_broadcast}'
The process spawn should be as described in the membership file placed at the root of the project.
- After the simulation, each process will generate a log of its activity at the root of the project as 'da_proc_{process_id}.out'.

## Repository Collaborators

  - Caviggia Guillaume
  - Beer Carolin
  - Francesco Pase
