package sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

//The Client class that connects to a server
//It implements Runnable for using in the JavaFX application
public class Client implements Runnable {
    // The Socket of the Client
    private Socket clientSocket;
    // The reader to read in server input
    private BufferedReader serverToClientReader;
    //The writer to send message to the server
    private PrintWriter clientToServerWriter;
    //the username of the client
    protected String name;
    //the chatlog of message read from the server
    public ObservableList<String> chatLog;

    //Creates a client
    public Client(String hostName, int portNumber, String name) throws UnknownHostException, IOException, DuplicateUsernameException, UsernameLengthExceeded {

        //Try to establish a connection to the server
        clientSocket = new Socket(hostName, portNumber);
        // Instantiate writers and readers to the socket
        serverToClientReader = new BufferedReader(new InputStreamReader(
                clientSocket.getInputStream()));
        clientToServerWriter = new PrintWriter(
                clientSocket.getOutputStream(), true);
        chatLog = FXCollections.observableArrayList();
        //Send name data to the server
        this.name = name;
        clientToServerWriter.println(name);

        //Read the server's response
        String serverMessage =  serverToClientReader.readLine();

        //If the server sends "REJECT" we know our name was a duplicate
        if(serverMessage.equals( "REJECT")){
            //Throw the exception to inform why we were not allowed to connect
            throw new DuplicateUsernameException("Dupe Name");

        }
        //If the server sends "NAME_LENGTH" we know our name was too long
        else if (serverMessage.equals("NAME_LENGTH")){
            //Throw the exception to inform why we were not allowed to connect
            throw new UsernameLengthExceeded("Username exceeded max length!");
        }



    }


    //Sends our message to the server
    public void writeToServer(String input) {

        clientToServerWriter.println(input);

    }

    //The Run method called upon starting the Runnable
    public void run() {

        // Infinite loop to update the chat log from the server
        while (true) {

            try {
                //Read the server's input
                final String inputFromServer = serverToClientReader.readLine();

                //If we get a null response something is wrong
                if(inputFromServer.equals(null)){

                    //Break out of the connection
                    break;
                }


                //When we get the input from the server we add it to our chatlog
                Platform.runLater(new Runnable() {
                    public void run() {
                        chatLog.add(inputFromServer);
                    }
                });

            } catch (SocketException e) {
                //If a SocketException occurred we relay that through the chatLog
                Platform.runLater(new Runnable() {
                    public void run() {
                        chatLog.add("Error in server!");
                    }

                });
                break;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (Exception e){
                //An unknown exception occurred
                Platform.runLater(new Runnable() {
                    public void run() {
                        chatLog.add("Unknown error occurred!");
                    }
                });
            }
        }

    }

}

