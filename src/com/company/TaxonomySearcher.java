package com.company;

import javax.crypto.EncryptedPrivateKeyInfo;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.io.*;
import java.net.*;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TaxonomySearcher {

    private static Socket clientSocket;
    private static DataOutputStream out;
    private static BufferedInputStream in;
    private static InputStream imageStream;
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
        in = new BufferedInputStream(clientSocket.getInputStream());
        imageStream = clientSocket.getInputStream();
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
        byte [] resultByte = new byte[256];

        String response;

        try {

            size = in.read(resultByte);

            if(size == -1){
                System.out.println("End of the file reached. Invalid response.");
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

    private static boolean responseGet (String filename) {

        int size;
        byte [] resultByte = new byte[16];
        byte [] imageArr;
        String statusCodeStr;
        int imageSize;

        try {

            size = in.read(resultByte, 0, 4);
            if(size == -1){
                System.out.println("End of the file reached. Invalid response (Status Code).");
                return false;
            }
            statusCodeStr = new String(resultByte,0,4, ENCODING);

            // controling status code
            if(statusCodeStr.startsWith("ISND")){
                System.out.print("Returned Status Code: " + statusCodeStr);


                // Getting the image size if response is correct
                size = in.read(resultByte, 0,3);
                if(size == -1){
                    System.out.println("End of the file reached. Invalid response (Image Size).");
                    return false;
                }
                ByteBuffer buff = ByteBuffer.wrap(resultByte);
                buff.order(ByteOrder.BIG_ENDIAN);
                imageSize = buff.getInt();
                


                // Getting the image data to image array
                imageArr = new byte[imageSize];
                size = in.read(imageArr,0,imageSize);

                if(size == -1){
                    System.out.println("End of the file reached. Invalid response (Image Size).");
                    return false;
                }

                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageArr));
                ImageIO.write(image, "jpg", new File(filename));

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
    private static void traverse(String[] targetFiles_arr){
        List<String> directories = new ArrayList<String>();
        List<String> targetFiles = Arrays.asList(targetFiles_arr);
        System.out.println("Sending: NLST");
        command = "NLST" + END;
        sendCommand(command);
        responseHandler();
        if ( statusText.equals("")) return;

        String[] directories_arr = statusText.split(" ");
        directories = Arrays.asList(directories_arr);

        boolean no_more_directories = true;

        for(String dir : directories){

            if(!dir.contains(".")){
                no_more_directories = false;
            }
            if (targetFiles.contains(dir))
            {

                System.out.println("Sending: GET " + dir);
                command = "GET" + " " + dir + END ;
                sendCommand(command);
                responseGet(dir);

            }
        }
        if (no_more_directories) return ;


        for(String dir: directories)
        {
            if(!dir.contains(".")) {
                System.out.println("Sending: CWDR " + dir);
                command = "CWDR" + " " + dir + END;
                sendCommand(command);
                responseHandler();
                traverse(targetFiles_arr) ;
                System.out.println("Sending: CDUP");
                command = "CDUP" + END;
                sendCommand(command);
                responseHandler();
            }
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
            traverse(targetFiles);

/*            command = "CWDR " + "plant" + END;
            sendCommand(command);
            responseHandler();

            command = "CWDR " + "tree" + END;
            sendCommand(command);
            responseHandler();

            command = "CWDR " + "fruit" + END;
            sendCommand(command);
            responseHandler();

            command = "CWDR " + "cherry" + END;
            sendCommand(command);
            responseHandler();

            System.out.println("\nAFTER GET ");
            command = "GET " + "cherry.jpg" + END ;
            sendCommand(command);
            responseGet();*/



        }





        stopConnection();
    }

}
