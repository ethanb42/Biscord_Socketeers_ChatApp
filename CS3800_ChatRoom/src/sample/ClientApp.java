package sample;


import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.text.Text;

//The GUI aspect of the Client Class
public class ClientApp extends Application {

    private ArrayList<Thread> threads;

    //Launched the JavaFX app
    public static void main(String[] args){
        launch();
    }

    @Override
    //Stops the JavaFX app
    public void stop() throws Exception {
        // TODO Auto-generated method stub
        super.stop();
        for (Thread thread: threads){
            thread.interrupt();
        }
    }

    @Override
    //Starts the JavaFX app
    public void start(Stage primaryStage) throws Exception {
        // TODO Auto-generated method stub
        //Initialize the applications threads
        threads = new ArrayList<Thread>();
        //Set the title
        primaryStage.setTitle("JavaFX Chat Client");
        //Set the scene
        primaryStage.setScene(makeInitScene(primaryStage));
        //Display the scene
        primaryStage.show();
    }

    //This method creates the 1st screen for entering username / port number
    public Scene makeInitScene(Stage primaryStage) {
        /* Make the root pane and set properties */
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setVgap(10);
        rootPane.setHgap(10);
        rootPane.setAlignment(Pos.CENTER);

        /* Make the text fields and set properties */
        TextField nameField = new TextField();
       // TextField hostNameField = new TextField();
        TextField portNumberField = new TextField();

        /* Make the labels and set properties */
        Label nameLabel = new Label("Name ");
       // Label hostNameLabel = new Label("Host Name");
        Label portNumberLabel = new Label("Port Number");
        Label errorLabel = new Label();
        /* Make the button and its handler */
        Button submitClientInfoButton = new Button("Done");
        submitClientInfoButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            //When the user clicks the button
            public void handle(ActionEvent Event) {
                // TODO Auto-generated method stub
                //Instantiate the client class and start it's thread
                Client client;
                try {
                    //Try to make the client from the user's input
                    client = new Client("localhost", Integer
                            .parseInt(portNumberField.getText()), nameField
                            .getText());

                    Thread clientThread = new Thread(client);
                    clientThread.setDaemon(true);
                    clientThread.start();
                    threads.add(clientThread);


                    //Change the scene of the primaryStage to our next screen
                    primaryStage.close();
                    primaryStage.setScene(makeChatUI(client));
                    primaryStage.show();
                }
                catch(ConnectException e){
                    errorLabel.setTextFill(Color.RED);
                    errorLabel.setText("Invalid port number, try again.");

                }
                catch (NumberFormatException | IOException e) {

                    errorLabel.setTextFill(Color.RED);
                    errorLabel.setText("Invalid port number, try again.");
                }
                catch(DuplicateUsernameException e){

                    errorLabel.setTextFill(Color.RED);
                    errorLabel.setText("Username taken, try again.");
                }
                catch (UsernameLengthExceeded e){
                    nameField.clear();
                    errorLabel.setTextFill(Color.RED);
                    errorLabel.setText("Username too long,\n 12-character limit!, try again.");
                }
                catch(Exception e){
                    errorLabel.setTextFill(Color.RED);
                    errorLabel.setText("Unknown error occurred, try again.");
                }



            }
        });

       // Add the components to the root pane arguments are (Node, Column, Number, Row Number)

        rootPane.add(nameField, 0, 0);
        rootPane.add(nameLabel, 1, 0);

        rootPane.add(portNumberField, 0, 2);
        rootPane.add(portNumberLabel, 1, 2);
        rootPane.add(submitClientInfoButton, 0, 3, 2, 1);
        rootPane.add(errorLabel, 0, 4);
       //Make the scene and run it!
        return new Scene(rootPane, 400, 400);
    }

    //Creates the chat screen UI
    public Scene makeChatUI(Client client) {
        /* Make the root pane and set properties */
        GridPane rootPane = new GridPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setHgap(10);
        rootPane.setVgap(10);

        //Make a listview to hold the chatlogs
        ListView<String> chatListView = new ListView<String>();
        chatListView.setItems(client.chatLog);



        //Make the chat text box and set its OnAction to send a message to the server
        TextField chatTextField = new TextField();
        chatTextField.setPromptText("Enter message...");
        chatTextField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            //On the textfield submitted
            public void handle(ActionEvent event) {
                // TODO Auto-generated method stub
                //Write the message from the field to the server
                client.writeToServer(chatTextField.getText());
                System.out.println("Sent to server");
                //Clear the field
                chatTextField.clear();
                chatTextField.setPromptText("Enter message...");
            }
        });

        // Add the components to the root pane
        Text title = new Text("UserName: "+client.name);
        rootPane.add(title,0,0);
        rootPane.add(chatListView, 0, 1);
        rootPane.add(chatTextField, 0, 2);

        // Make and return the new scene
        return new Scene(rootPane, 400, 400);

    }
}
