package daProc;
import sun.misc.Signal;
import java.io.IOException;
import java.lang.ProcessBuilder;
import java.util.logging.Logger;

class daProc {
    // FROM C SKELETON (dunno for what it should be good?)
    static int waitForStart = 1;
    static ProcessBuilder pb;
    static Process p;
    //static final Signal USR1 = new Signal("SIG_USR1");

    Logger logger = Logger.getLogger(daProc.class.getName());

    static void start(int signum) {
        logger.debug("Inside start method");
        waitForStart = 0;
        try {
            p = pb.start(); // Creates actual process through ProcessBuilder
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void stop(int signum) {
        logger.debug("Inside stop method");
        if (p.isAlive()) {
            // Stop it
        }
        //reset signal handlers to default
        // FROM C SKELETON
        //signal(SIGTERM, SIG_DFL);
        //signal(SIGINT, SIG_DFL);

        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.\n");

        //write/flush output file if necessary
        System.out.println("Writing output.\n");

        //exit directly from signal handler
        //exit(0);
    }


    // int argc, char** argv
    public static void main(String[] args) {

        //set signal handlers
        // FROM C SKELETON
        //signal(SIGUSR1, start);
        //signal(SIGTERM, stop);
        //signal(SIGINT, stop);


        //parse arguments, including membership
        //initialize application
        //start listening for incoming UDP packets
        System.out.println("Initializing.\n");


        //wait until start signal
        // FROM C SKELETON
        /*
        while(waitForStart) {
            struct timespec sleep_time;
            sleep_time.tv_sec = 0;
            sleep_time.tv_nsec = 1000;
            nanosleep(sleep_time, NULL);
        }
        */


        //broadcast messages
        System.out.println("Broadcasting messages.\n");

        //wait until stopped
        // FROM C SKELETON
        /*
        while(1) {
            struct timespec sleep_time;
            sleep_time.tv_sec = 1;
            sleep_time.tv_nsec = 0;
            nanosleep(&sleep_time, NULL);
        }
        */
    }
}
