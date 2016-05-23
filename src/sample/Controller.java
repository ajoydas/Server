package sample;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Controller {
    public Button bstart;
    public TextArea textArea;
    public Button bstop;
    private boolean stopped;
    private ServerSocket welcomeSocket;
    private Socket connectionSocket;
    private Service<Void> service;

    @FXML
    public void initialize() throws IOException {
        textArea.setEditable(false);
        textArea.setText("<-----------Welcome---------->\n");
        stopped=true;
        bstop.setDisable(true);
        welcomeSocket = new ServerSocket(6789);
        connectionSocket=null;
    }

    public void buttonClicked(ActionEvent actionEvent) {
        //started=true;
        stopped=false;
        bstop.setDisable(false);
        bstart.setDisable(true);
        service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {

                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                                    int workerThreadCount = 0;
                                    int id = 1;
                                    while (true){
                                        if (stopped) break;
                                        try {
                                            connectionSocket = welcomeSocket.accept();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            break;
                                        }
                                        Task<Void> task=new Worker(connectionSocket,id);
                                        Thread t = new Thread(task);
                                        t.setDaemon(true);
                                        t.start();
                                        workerThreadCount++;
                                        textArea.appendText("Client [" + id + "] is now connected.\n");
                                        id++;
                                    }
                                return null;
                            }
                        };
                    }



            @Override
            protected void cancelled() {
                super.cancelled();
                textArea.appendText("Service Canceled\n");
            }
        };

        service.start();

    }

    private class Worker extends Task<Void>
    {
        int id;
        Socket connectionSocket;
        public Worker(Socket connectionSocket,int id) {
            this.id = id;
            this.connectionSocket=connectionSocket;
        }

        @Override
        protected Void call() throws Exception {
            String clientSentence;
            String capitalizedSentence;
            while(true)
                try {
                    DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    clientSentence = inFromServer.readLine();
                    if(stopped) {
                        connectionSocket.close();
                        break;
                    }
                    textArea.appendText("Cliend No. "+id+" says "+clientSentence+"\n");
                    capitalizedSentence = clientSentence.toUpperCase();
                    outToServer.writeBytes(capitalizedSentence + '\n');

                } catch (Exception e) {
                    System.out.println("Error! Connection lost for id "+id);
                }
            System.out.println("Client "+id+" connection Canceled");
            return null;
        }
    }

    public void bStopClicked(ActionEvent actionEvent) {
        stopped=true;
        bstop.setDisable(true);
        bstart.setDisable(false);
        service.cancel();
    }


}