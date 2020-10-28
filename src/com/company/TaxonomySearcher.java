package com.company;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TaxonomySearcher {

    private static Socket clientSocket;
    private static DataOutputStream out;
    private static DataInputStream in;
    private static String statusText;
    private static String command;

    private static final Charset ENCODING = StandardCharsets.US_ASCII;
    private static final String USERNAME = "bilkentstu";
    private static final String PASSWORD = "cs421f2020";
    private static final String END = "\r\n";

    private static void startConnection(String ip, int port) throws IOException {
        //intiliazing Socket
        clientSocket = new Socket(ip, port);

        // intiliazing Buffers
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());
    }

    private static void stopConnection() throws IOException {
        // closing socket and buffers
        String command = "EXIT " + USERNAME + END;
        sendCommand(command);
    }

    private static boolean sendCommand (String command){
        byte [] commandEncoded = command.getBytes(ENCODING);
        try
        {
            out.write(commandEncoded);
        }
        catch (IOException e)
        {
            System.out.println("Sending command is unsuccessful" + e);
            return false;
        }
        return true;
    }

    private static boolean responseHandler () {

        int size;
        byte [] resultByte = new byte[128];

        String response;

        try {

            size = in.read(resultByte);

            if(size == -1){
                System.out.println("Server returned unsuccessful response.");
                return false;
            }

            response = new String(resultByte, ENCODING);

            if(response.startsWith("OK")){
                System.out.print(response);
                statusText = (response.split("OK")[1]).split(END)[0].trim();
                return true;
            }
            else{
                System.out.println(response);
                statusText = (response.split("INVALID")[1]).split(END)[0].trim();
                return false;
            }

        }
        catch (IOException e) {
            System.out.println("Error while handling response." + e);
            return false;
        }
    }

    private static boolean responseGet () {

        int size;
        byte [] resultByte = new byte[7];

        String statusCodeStr,imageSizeStr;


        try {

            size = in.read(resultByte);

            if(size == -1){
                System.out.println("Invalid response from server.");
                return false;
            }

            statusCodeStr = new String(Arrays.copyOfRange(resultByte,0,4),ENCODING);
            imageSizeStr =  new String(Arrays.copyOfRange(resultByte,4,7),ENCODING);
            System.out.println("Image " + statusCodeStr + "  " + imageSizeStr);

            if(statusCodeStr.startsWith("ISND")){
                System.out.print("ISND " + statusCodeStr);
                return true;
            }
            else{
                System.out.println("Download from the server is unsuccessful");
                return false;
            }

        }
        catch (IOException e) {
            System.out.println("Error while handling response." + e);
            return false;
        }
    }

    private static boolean authentication () {
        // Sends the username for authentication
        command = "USER " + USERNAME + END;
        System.out.println("Sending Username: bilkentstu");
        sendCommand(command);

        if (!responseHandler()){
            // if the username is wrong.
            System.out.println("EXIT: Username is invalid.");
            return false;
        }
        else{
            //Sends the password for authentication
            command = "PASS " + PASSWORD + END ;
            System.out.println("Sending Password: cs421f2020");
            sendCommand(command);

            // If the command is wrong
            if (!responseHandler()){
                System.out.println("EXIT: Password is invalid.");
                return false;
            }
        }
        return true;
    }


    public static void main (String[] args) throws IOException {

        int port = Integer.parseInt(args[1]);
        startConnection(args[0], port);

        if ( authentication() ) {

            System.out.println("Sending: OBJ");
            command = "OBJ" + END;
            sendCommand(command);
            responseHandler();
            String [] targetFiles = statusText.split(" ");


            command = "CWDR " + "plant" + END;
            sendCommand(command);
            command = "CWDR " + "tree" + END;
            sendCommand(command);
            command = "CWDR " + "fruit" + END;
            sendCommand(command);
            command = "CWDR " + "apple" + END;
            sendCommand(command);
            command = "GET " + "apple.jpg" + END;
            sendCommand(command);
            responseGet();







        }





        stopConnection();
    }

}