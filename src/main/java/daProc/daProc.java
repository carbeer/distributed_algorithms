package daProc;
import sun.misc.Signal;
import java.io.IOException;

class daProc {
    // FROM C SKELETON (dunno for what it should be good?)
    static int waitForStart = 1;
    //static final Signal USR1 = new Signal("SIG_USR1");

    

    static void start(int signum) {
        
        waitForStart = 0;
        
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
