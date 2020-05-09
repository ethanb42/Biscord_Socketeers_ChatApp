
package sample;
import javafx.collections.ObservableList;

import java.net.*;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;


// The chat server class
// Manages and keeps track of client connections
public class Server {
    //The port number of the server
    private int port;
    //Hashset of usernames of the connected clients
    private Set<String> usernames = new HashSet<>();
    //Hashset of chatThreads that store ChatThreads for  each connected client
    private Set<ChatThread> chatThreads = new HashSet<>();
    //A list of messages that stores the chatLog
    public ObservableList<String> chatLog;

    //Initializes the server with the specified port number
    public Server(int p) {
        this.port = p;
    }

    //Runs the server
    public static void main(String[] args) throws Exception {

         //Creates a server on port 80
         Server server = new Server(80);
         //Starts the server
         server.workIt();
    }

    //Manages clients connecting to the server and initiates the server
    public void workIt() {
        try {
            //The ServerSocket used to allow connections
            ServerSocket servSock = new ServerSocket(port);
            System.out.println("Port " + port + " ready and waiting.");

            //The Server will keep accepting clients
            while(true) {
                //Block till we get a new client
                Socket socket = servSock.accept();
                //Create a ChatThread to manage the new client
                ChatThread newClient = new ChatThread(socket, this);
                //Add the newClient to our HashSet of ChatThreads
                chatThreads.add(newClient);
                //Start the threads run function
                newClient.start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Synchronized is useful because
    //all other threads attempting to enter
    //the synchronized block are blocked until
    //the thread inside the synchronized block exits the block.

    //Adds the username to our username Hashset
    //True: The name could be added
    //False: The name already exists
    public synchronized boolean addUsername(String username) {
        return (usernames.add(username));
    }

    //Removes a user from the Server
    public synchronized void removeUser(String username, ChatThread clientThread) {
        //Removes the user from the username hashset
        if (usernames.remove(username)) {
            //Removes the user from the chatThreads Hashset
            chatThreads.remove(clientThread);
        }
    }

    //Sends a message to all clients connected.
    //Only one thread can access this method at a time!
    public synchronized void sendMessage(String message) {

        //Get the time on the server for the timestamp
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = now.format(formatter);
        formattedTime = "[" + formattedTime +"]";

        //For all the connected clients
        for(ChatThread stock : chatThreads){
            //Send the message
            stock.toClient(formattedTime + " " +message);
        }

    }

    //A Thread responsible for managing client connections
    private class ChatThread extends Thread {

        private Socket socket;
        private Server server;
        private PrintWriter outToClient;

        //Initializes the ChatThread
        ChatThread(Socket sock, Server serv) {
            socket = sock;
            server = serv;
        }

        //The run function for the thread
        public void run() {
            String username="";
            try {
                //Create our input/output streams to the client for communication
                InputStream in = socket.getInputStream();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(in));
                OutputStream out = socket.getOutputStream();
                outToClient = new PrintWriter(out, true);
                //Read the client's username
                 username = inFromClient.readLine();

                 //A boolean to loop till the client is allowed to connect
                // OR decided to stop attempting to connect
                boolean connecting = true;
                //We loop till we can accept the client's connection
                while (connecting) {
                   connecting = false;
                   //Check if the username is too long
                    int nameLength = username.length();
                    if(nameLength > 12 ){
                        //If the name is too long we tell the client
                        outToClient.println("NAME_LENGTH");
                        System.err.println(nameLength);
                        //We will have to loop again and await the new shorter name
                        connecting = true;
                    }
                    //This won't execute if the above if failed.
                    //Check if the username exists, if it doesn't it will be added.
                     else if(!server.addUsername(username)){
                         System.err.println(nameLength);
                         System.err.println(!server.addUsername(username));
                        outToClient.println("REJECT");
                        connecting = true;
                    }
                     else{
                         System.err.println(nameLength);
                         System.err.println(!server.addUsername(username));
                         //We only reach here when the username
                         //is less than 12 and the name was accepted
                         //We can break out of the loop
                         connecting = false;
                         break;
                     }
                     //Read in the new username from the client and repeat the process till acceptance
                    username = inFromClient.readLine();

                }
                //When we break out of the connection loop
                //We let the client know we accepted their connection.
                outToClient.println("ACCEPT");

                //We create the message to send everyone upon the user joining
                String message = "**"+ username + " has joined.**";
                //We request the server to  send the message
                server.sendMessage(message);

                //While the client is connected and has not typed the exit command
                while(!message.equals("!exit")) {
                    //We read in their next input
                    message =inFromClient.readLine();

                    //If that message is the exit command
                    if(message.equals("!exit")){
                        //We break out of the loop
                        break;
                    }

                    //If we are not exiting we format the clients name and message
                    //into the format we want to be displayed
                    String send = username +": " +  message;

                    //We request the server to  send the message
                    server.sendMessage(send);

                }

                //We reach here when the user types the exit command
                //We let them know we they are disconnected
                outToClient.println("**You have been disconnected**");
                //Remove the user from the server
                server.removeUser(username, this);
                try {
                    //Force the connection to close if it is not already.
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                //Upon removing the user we format the disconnect message to send
                message = "**"+ username + " has logged off.**";
                //We request the server to  send the message
                server.sendMessage(message);

            } catch (Exception e) {
                //If something goes wrong, we must disconnect the user and inform the connected clients.
                server.removeUser(username, this);

                try {
                    //Force the connection to close if it is not already.
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                String message = "**"+ username + " has disconnected.**";
                //We request the server to  send the message
                server.sendMessage(message);

                System.out.println(e.getMessage());
            }
        }


        //Sends a message to the client
        public void toClient(String message) {
            outToClient.println(message);
        }
    }
}