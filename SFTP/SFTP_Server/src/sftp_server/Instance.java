package sftp_server;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;


/*
 * Instance
 * This is a new thread of SFTP_Server which handles a new socket connection.
 */

public class Instance extends Thread{

    protected Socket socket;

    boolean running = true;


    String sendMode = "A";      //File Type, A=ASCII, B=Binary, C=Continuous

    private static final File root_path = FileSystems.getDefault().getPath("ftp/").toFile().getAbsoluteFile();
    public static String directory = "";
    String directory_path = "";
    String file_path = "";

    //User authentication
    protected static Auth auth;

    //Data Streams for ASCII and Binary
    BufferedReader ascii_from_client;
    DataOutputStream ascii_to_client;
    DataInputStream binary_from_client;
    DataOutputStream binary_to_client;

    //Flag for  RETR
    boolean retr = false;

    long file_length;                // File length to store

    Instance(Socket socket, String authFile){
        this.socket = socket;
        Instance.auth = new Auth(authFile);
    }

    @Override
    public void run(){
        try {
            socket.setReuseAddress(true);
            binary_to_client = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            binary_from_client = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            ascii_from_client = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ascii_to_client = new DataOutputStream(socket.getOutputStream());
            sendToClient("+Welcome to Azi's SFTP RFC913 Server\0");
        } catch (Exception e) {}

    }

    //mode detects commands and routes it to the correct function
    public void mode(String[] commandArgs) throws Exception{
        //"USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL", "NAME", "DONE", "RETR", "STOR"
        switch (commandArgs[0]) {
            case "USER":
                sendToClient(auth.user(commandArgs[1]));
                break;
            case "ACCT":
                sendToClient(auth.acct(commandArgs[1]));
                break;
            case "PASS":
                sendToClient(auth.pass(commandArgs[1]));
                break;
            case "TYPE":
                //TODO
            case "LIST":
                //TODO
            case "CDIR":
                //TODO
            case "KILL":
                //TODO
            case "NAME":
                //TODO
            case "TOBE":
                //TODO
            case "RETR":
                //TODO
            case "SEND":
                if (auth.verified()){
                    send();
                } else {
                    sendToClient("wrong authentication");
                }
                break;
            case "STOP":
                //TODO
            case "STOR":
                //TODO
            default:
                break;
        }
    }

    // TYPE { A | B | C } Command
    public void type(String inMode){
        if (null == inMode){
            sendToClient("-Type not valid");
        } else switch (inMode) {
            case "A":
                sendMode = "A";
                sendToClient("Ascii mode");
                break;
            case "B":
                sendMode = "B";
                sendToClient("Binary mode");
                break;
            case "C":
                sendMode = "C";
                sendToClient("Continuous mode");
                break;
            default:
                sendToClient("Invalid type");
                break;
        }
    }

    // LIST { F | V } directory-path Command
    public void list(String[] args) throws Exception{
        //TODO check directory

        if ("F".equals(args[1])){
            //LIST F
            //TODO

        } else if ("V".equals(args[1])){
            //LIST V TODO

        } else {
            sendToClient("Invalid list type, should be F or V ");
        }
        directory_path = "";   //Reset current directory path
    }

    // CDIR {directory} Command
    public void cdir(String[] args) throws Exception{
        //TODO
    }

    // KILL {filename} Command
    public void kill(String[] args) throws Exception {
        //TODO
    }

    // NAME {current-filename} Command
    public void name(String[] args) throws Exception {
        // Check if NAME has not already been used and waiting for TOBE command
        //TODO
    }



    // RETR {filename} Command
    public void retr(String[] args) throws Exception {
        //TODO
    }

    // SEND Command
    public void send(){
        if (!retr) {    //check if retr is activated
            sendToClient("-No File Selected");
        } else {
            byte[] bytes = new byte[(int) file_length];

            try {
                File file = new File(root_path.toString() + directory + file_path);
                if ("A".equals(sendMode)){      //ASCII
                    try (BufferedInputStream bufferedStream = new BufferedInputStream(new FileInputStream(file))) {
                        ascii_to_client.flush();
                        // Read and send by byte
                        int p = 0;
                        while ((p = bufferedStream.read(bytes)) >= 0) {
                            ascii_to_client.write(bytes, 0, p);
                        }
                        bufferedStream.close();
                        ascii_to_client.flush();
                    } catch (IOException e){
                        socket.close();
                        running = false;
                    }
                } else {              //Binary or Continuous
                    try (FileInputStream fileStream = new FileInputStream(file)) {
                        binary_to_client.flush();
                        int e;
                        while ((e = fileStream.read()) >= 0) {
                            binary_to_client.write(e);
                        }
                        fileStream.close();
                        binary_to_client.flush();
                    } catch (IOException e){
                        socket.close();
                        running = false;
                    }
                }
                retr = false;
            } catch (Exception e) {
            }
        }
    }



    // STOR { NEW| OLD | APP } {filename} Command
    public void stor(String[] args) throws Exception{
        // TODO
    }

    /*
     * -------------------------------------------------------------------------
     * HELPER FUNCTIONS
     */



    //read_from_client_side reads ASCII messages from client
    private String read_from_client_side() {
        String text = "";
        // TODO
        return text;
    }

    //sendToClient sends ASCII messages to client
    private void sendToClient(String text) {
        try {
            ascii_to_client.writeBytes(text + "\0");
        } catch (IOException lineErr) {
            try {   // if IOError close socket as client is already closed
                socket.close();
                running = false;
            } catch (IOException ex) {}
        }
    }

    // check_type checks whether both directory and file is valid
    private boolean check_type(String[] args) throws Exception{
        // TODO
        return true;
    }

}