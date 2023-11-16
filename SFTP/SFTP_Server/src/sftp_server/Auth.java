package sftp_server;

import java.io.*;

//https://datatracker.ietf.org/doc/html/rfc913

/*
 * Auth.txt  authentication format
 * "username account password"
 */

/**
 * Auth authenticates users
 */
public class Auth {

    protected static String auth_file;
    protected static Boolean user_verification = false;
    protected static Boolean account_verification = false;
    protected static Boolean password_verification = false;

    protected static String user;
    protected static String account = "";
    protected static String password;

    public Auth(String auth_file){
        Auth.auth_file = auth_file;   //find and locate authentication file
    }

    // USER {username} Command
    public String user(String user_text) throws Exception{
        File file = new File("auth.txt");
        BufferedReader reader = null;
        String text;
        String response = null;

        user_verification = false;
        account_verification = false;
        password_verification = false;

        try {
            reader = new BufferedReader(new FileReader(file));
            // Scan file for credentials
            while ((text = reader.readLine()) != null) {
                String[] userDetails = text.split(" ", -1);
                user = userDetails[0];
                account = userDetails[1];
                password = userDetails[2];
                if (user.equals(user_text)){
                    user_verification = true;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        //TODO for different verifications
        //if no user found
        if (!user_verification){
            return "invalid user, try again";
        } else {

            //output response based on status
            if (password_verification ){
                response = "!" + user + " logged in";
            } else {
                //TODO for other invalid cases
                response = "invalid , try again";
            }
        }
        return response;
    }

    //ACCT {account} Command
    public String acct(String account_text) throws Exception {
        // TODO search if account is valid
        String response = null;
        return response;
        }

    // PASS {password} Command
    public String pass(String password_text) throws Exception {
        // TODO check password
        String response = null;
        return response;

    }

    // verified() is a helper function to check if user has met all checks
    public boolean verified(){
        return user_verification && account_verification && password_verification;
    }
}