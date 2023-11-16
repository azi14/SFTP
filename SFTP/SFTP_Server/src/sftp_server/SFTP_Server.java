package sftp_server;

import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
 * SFTPServer
 * This is an implementation of the RFC 913 Simple File Transfer Protocol
 */
public class SFTP_Server {

    /**
     * @param port - Server Port
     * @param auth_file - Location of Authentication File
     * @throws java.lang.Exception
     */

    static String authentication_file;

    public static void main(String[] args) throws Exception {
        // check for port and auth file
        if (args.length == 2){
            if (!validate_authentication_path(args[1])){
                return;
            }
        } else {
            System.out.println("wrong args, enter 2 args");
            return;
        }

        //create ftp folder if it doesn't exist
        System.out.println("Creating /ftp folder...");
        new File(FileSystems.getDefault().getPath("ftp/").toFile().getAbsoluteFile().toString()).mkdirs();
        System.out.println("/ftp folder created!");

        ServerSocket welcomeSocket = new ServerSocket(Integer.parseInt(args[0]));
        System.out.println("Now Socket Started...");

        while(true) {
            Socket socket = welcomeSocket.accept();
            new Instance(socket, authentication_file).start(); //run an SFTP instance for each client connection
        }
    }

    //validate_authentication_path
    public static boolean validate_authentication_path(String filePathString){
        File file_path = new File(filePathString);
        if(file_path.exists() && !file_path.isDirectory()) {
            authentication_file = filePathString;
            System.out.println("authentication file found!");
            return true;
        } else {
            authentication_file = null;
            System.out.println("no authentication file in this path?");
            return false;
        }
    }
}