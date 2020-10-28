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
    private static String response;
    private static String flag;

    private static final Charset ENCODING = StandardCharsets.US_ASCII;
    private static final String USERNAME = "bilkentstu";
    private static final String PASSWORD = "cs421f2020";
    private static final String POSTFIX = "\r\n";

    private static void startConnection(String ip, int port) throws IOException {
        //intiliazing Socket
        clientSocket = new Socket(ip, port);

        // intiliazing Buffers
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());
    }

    private static void stopConnection() throws IOException {
        // closing socket and buffers
        String command = "EXIT " + USERNAME + POSTFIX;
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
            System.out.println("Error while sending command to server.\n" + e);
            return false;
        }
        return true;
    }


    public static void main (String[] args) throws IOException {

        int port = Integer.parseInt(args[1]);
        startConnection(args[0], port);

        // Sends the username for authentication
        String command = "USER " + USERNAME + POSTFIX;
        sendCommand(command);



        System.out.println("FROM SERVER: " + response );

        //Sends the password for authentication
        command = "PASS " + PASSWORD + POSTFIX ;
        sendCommand(command);

        System.out.println("FROM SERVER: " + response );


        stopConnection();
    }

}