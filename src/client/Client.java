/**
 * Full Name: Sumit Kumar Thakur
 * Regd no.: 1941012938
 */

package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    // Input Streams for sending out data between Server-Client
    private ObjectInputStream inputStream;
    // Output Streams for receiving out data between Server-Client
    private ObjectOutputStream outputStream;

    private Socket socket;

    ClientGUI clientGUI;

    private String server = "localhost";
    public String username = "NewClient";
    private int port = 3000;

    // Client Constructor
    Client(String username, ClientGUI clientGUI) {
        this.username = username;
        this.clientGUI = clientGUI;
    }

    // Client overloading
    Client(ClientGUI clientGUI) {
        this.clientGUI = clientGUI;
    }

    // to get current client
    public String getUsername() {
        return username;
    }

    // to set current client
    public void setUsername(String username) {
        this.username = username;
    }

    public boolean startClient() {

        try {
            socket = new Socket(server, port);
        }

        catch (Exception ec) {
            showMessage("Error connectiong to server:" + ec);
            return false;
        }

        // msg for client on connection success
        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        showMessage(msg);

        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        }

        catch (IOException eIO) {
            showMessage("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        new ServerReply().start();

        // to register new client
        try {
            outputStream.writeObject(username);
        } catch (IOException eIO) {
            showMessage("Exception doing login : " + eIO);
            // disconnect if any error
            disconnect();
            return false;
        }

        return true;
    }

    private void showMessage(String msg) {
        clientGUI.updateClientLog(msg);
    }

    void sendMessage(String[] msg) {
        try {
            outputStream.writeObject(msg);
        } catch (IOException e) {
            showMessage("Exception writing to server: " + e);
        }
    }

    private void disconnect() {
        try {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (socket != null)
                socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // continiously wait for reply from server and parse response
    class ServerReply extends Thread {
        public void run() {
            while (true) {
                try {
                    String[] response = new String[6];

                    response = (String[]) inputStream.readObject();

                    String contentType = response[2].substring(13);
                    String guiusername = response[3];

                    if (contentType.trim().equalsIgnoreCase("broadcast")
                            || contentType.trim().equalsIgnoreCase("multicast")
                            || contentType.trim().equalsIgnoreCase("unicast")) {
                        clientGUI.updateClientLog(response[3]);
                        clientGUI.updateClientLog(response[6]);
                    }

                    else if (contentType.trim().equalsIgnoreCase("username")) {

                        if (response[6].equalsIgnoreCase("rejected")) {
                            clientGUI.setValidUser(false);
                            clientGUI.setOldUser(true);
                            clientGUI.infoBox("Username: " + guiusername + " is Already Connect, choose another",
                                    "Alert");
                        }

                        else if (response[6].equalsIgnoreCase("accepted")) {
                            clientGUI.setValidUser(true);
                            clientGUI.setOldUser(false);
                            clientGUI.btnSendMessage.setEnabled(true);
                            clientGUI.infoBox("User: " + guiusername + " is Connected", "Alert");
                        }

                        else {
                            clientGUI.setValidUser(true);
                            clientGUI.setOldUser(true);
                            clientGUI.btnSendMessage.setEnabled(true);
                            clientGUI.infoBox("Welcome back " + guiusername + "!", "Alert");
                        }
                        clientGUI.loginUser();
                    } else if (contentType.trim().equalsIgnoreCase("client-list")) {
                        clientGUI.updateClientList(response);
                    } else if (contentType.trim().equalsIgnoreCase("message-file")) {
                        System.out.println("Message response from server: " + response[6].length());
                        if (response[6].length() == 0)
                            response[6] += "There are No messages in the queue.\n";
                        clientGUI.getMsgLogFromServer(response[6]);
                    }
                } catch (Exception e) {
                    showMessage("Server has close the connection: " + e);
                    if (clientGUI != null)
                        clientGUI.connectionFailed();
                    break;
                }

            }
        }
    }
}
