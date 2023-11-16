package sftp_client;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

/*
 * SFTPClient
 */

public class SFTP_Client {

    /**
     * @param ip - Server IP address
     * @param port - Server Port
     * @throws java.lang.Exception
     */


    static String[] command_list_sftp;   //list of valid commands
    static String mode;  //Command

    static String ip;
    static int port;

    static boolean authentication_valid = false;

    static String send_mode = "A";       //A=ASCII, B=Binary, C=Continuous
    static String filename;
    static long fileSize;
    static File ftp = FileSystems.getDefault().getPath("ftp/").toFile().getAbsoluteFile(); //location of ftp folder

    static Socket socket;
    static DataOutputStream ascii_to_server;    //ASCII out
    static BufferedReader ascii_from_server;     //ASCII in
    static DataOutputStream binary_to_server;    //Binary out
    static DataInputStream binary_from_server;   //Binary in

    static boolean running = true;
    static boolean retr = false;    //if RETR is running

    public static void main(String[] args) throws Exception{
        System.out.println("FTP folder: " + ftp.toString());
        new File(ftp.toString()).mkdirs();  //make ftp directory if it does not already exist

        // USER=1, ACCT=2, PASS=3, TYPE=4, LIST=5, CDIR=6, KILL=7, NAME=8, DONE=9, RETR=10, STOR=11
        command_list_sftp = new String[]{"USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL",
                "NAME", "TOBE", "DONE", "RETR", "SEND", "STOP", "STOR"};

        if (args.length == 2){
            ip = args[0];
            port = Integer.parseInt(args[1]);

            try{
                socket = new Socket(ip, port);
                //Set ASCII streams
                ascii_to_server = new DataOutputStream(socket.getOutputStream());
                ascii_from_server = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //Set Binary streams
                binary_to_server = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                binary_from_server = new DataInputStream(new BufferedInputStream(socket.getInputStream()));



                while(running){
                    String[] commandArgs = selectMode();
                    if (commandArgs != null) enterMode(commandArgs);
                }
            } catch (Exception e){
            };
        } else {
            System.out.println("argument error. need 2 args");
        }
    }

    //selectMode reads client commands and checks if it is valid
    public static String[] selectMode() throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = br.readLine();
        String[] commands = input.split(" ");

        for (String sftpCommand : command_list_sftp){
            if (commands[0].equals(sftpCommand)){
                mode = sftpCommand;
                return commands;
            }
        }


        return null;
    }

    //enterMode detects command and routes it to the appropiate function
    public static void enterMode(String[] commandArgs) throws Exception{
        //"USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL", "NAME", "DONE", "RETR", "STOR"
        if (null != mode)

            switch (mode) {
                case "USER":
                    auth("USER",commandArgs);
                    break;
                case "ACCT":
                    auth("ACCT",commandArgs);
                    break;
                case "PASS":
                    auth("PASS",commandArgs);
                    break;
                case "TYPE":
                    type(commandArgs);
                    break;
                case "LIST":
                    list(commandArgs);
                    break;
                case "CDIR":
                    cdir(commandArgs);
                    break;
                case "KILL":
                    kill(commandArgs);
                    break;
                case "NAME":
                    //TODO
                case "TOBE":
                    //TODO
                case "DONE":
                    //TODO
                case "RETR":
                    retr(commandArgs);
                    break;
                case "SEND":
                    send();
                    break;
                case "STOP":
                    //TODO
                case "STOR":
                    stor(commandArgs);
                    break;
                default:
                    break;
            }
    }

    //authentication commands
    public static void auth(String mode, String[] commandArgs) throws Exception{
        if (commandArgs.length != 2){
            String argsError = null;

            switch (mode) {
                case "USER":
                    argsError = "USER user-id";
                    break;
                case "ACCT":
                    argsError = "ACCT account";
                    break;
                case "PASS":
                    argsError = "PASS password";
                    break;
                default:
                    break;
            }

            System.out.println("ARG ERROR: found " );
        } else {
            sendToServer(mode + " " + commandArgs[1]);
            authentication_valid = true;
        }
    }

    // TYPE command
    public static void type(String[] commandArgs) throws Exception{
        //TODO
    }

    //LIST command
    public static void list(String[] commandArgs) throws Exception {
        //TODO
    }

    //CDIR command
    public static void cdir(String[] commandArgs) throws Exception {
        //TODO
    }

    //KILL command
    public static void kill(String[] commandArgs) throws Exception {
        //TODO
    }





    //RETR command
    public static void retr(String[] commandArgs) throws Exception {
        //TODO
    }

    //SEND command
    public static void send(){
        if (!retr){
            System.out.println("Nothing to send.");
            return;
        }
        sendToServer("SEND ");
        try {
            File file = new File(ftp.getPath() + "/" + filename);
            Long timeout = new Date().getTime() + (fileSize/8 )*1000;
            if ("A".equals(send_mode)) {
                try (BufferedOutputStream bufferedStream = new BufferedOutputStream(new FileOutputStream(file, false))) {
                    for (int i = 0; i < fileSize; i++) {
                        if (new Date().getTime() >= timeout){   //close stream if file transfer times out
                            System.out.println("Transfer taking too long. Timed out ");
                            return;
                        }
                        bufferedStream.write(ascii_from_server.read());
                    }
                    bufferedStream.flush();
                    bufferedStream.close();
                    System.out.println("File " + filename + " was saved.");
                    retr = false;
                }
            } else {
                try (FileOutputStream fileStream = new FileOutputStream(file, false)) {
                    int e;
                    int i = 0;
                    byte[] bytes = new byte[(int) fileSize];
                    while (i < fileSize) {
                        e = binary_from_server.read(bytes);
                        if (new Date().getTime() >= timeout){   //close stream if file transfer times out
                            System.out.println("Transfer taking too long. Timed out ");
                            return;
                        }
                        fileStream.write(bytes, 0, e);
                        i+=e;
                    }
                    fileStream.flush();
                    fileStream.close();
                    System.out.println("File " + filename + " was saved.");
                    retr = false;
                }
            }
        } catch (FileNotFoundException n){
        } catch (Exception e) {
        }
    }



    //STOR command
    public static void stor(String[] commandArgs) throws Exception {
        //TODO
    }

    //isBinary compares the amount of ASCII character to non-ASCII characters to determine if the file is binary
    private static boolean isBinary(File file){
        //TODO
        return true;
    }

    //readFromServer reads ASCII messages from server
    private static String readFromServer() {
        String text = "";
        //TODO
        return text;
    }

    //sendToServer sends ASCII messages to server
    private static void sendToServer(String text) {
        try {
            ascii_to_server.writeBytes(text + "\0");
        } catch (IOException i) {
            try {   // if IOError close socket as client is already closed
                socket.close();
                running = false;
                System.out.println("Connection to server has been closed");
            } catch (IOException ex) {}
        }
    }
}