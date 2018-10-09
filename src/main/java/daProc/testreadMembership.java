package daProc;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;


public class testreadMembership {
	

    public static void main(String []args) {
    File membershipPath = new File(System.getProperty("user.dir") + "/src/main/java/daProc/membership.txt");
    int id = 2;
    String[] splited = {"error"};
	
	try {

        BufferedReader b = new BufferedReader(new FileReader(membershipPath));

        String readLine = "";

        while ((readLine = b.readLine()) != null) {
        	String line = readLine;
        	String[] tmp = line.split("\\s+");
        	if (Integer.valueOf(tmp[0]) == id && tmp.length == 3) {
        		splited = tmp;
        	     break;
        	}
        }

    } catch (IOException e) {
        e.printStackTrace();
    }
     
	 System.out.println(Arrays.toString(splited));

    }

}
