/*
 * Full Name: Sumit Kumar Thakur
 * Regd No. : 1941012938
 */

package server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Server {
    ServerGUI serverGUI;

    int port = 3000; // port number
    // List for connected Client
    ArrayList<CThread> ClientList;

    int clientID;
    // determining server status
    private boolean runT;
    // Map to eliminate redundency
    HashMap<String, String> activeClient;
    // File to access and store usernames
    File usernameFile;
    // allows to read and write in random fashion
    RandomAccessFile raf;
    boolean fileFound;

    public HashMap<String, String> getActiveClient() {
        return activeClient;
    }

    public void setActiveClient(HashMap<String, String> activeClient) {
        this.activeClient = activeClient;
    }

    public Server(ServerGUI serverGUI) {
        this.serverGUI = serverGUI;
        // ArrayList for the Client list
        this.ClientList = new ArrayList<CThread>();
        // Initialize Client ID
        clientID = 0;
        // initialize the active client that are connected or disconnected
        activeClient = new HashMap<>();
        usernameFile = new File(".\\src\\server\\usernames.txt");
    }

    public void startServer() {
        runT = true;
        try {

            readUserNameInMap(activeClient, usernameFile);
            serverGUI.updateTable(activeClient);
            System.out.println(activeClient);

            ServerSocket serverSocket = new ServerSocket(port);

            while (runT) {
                Socket socket = serverSocket.accept();
                display("Listening for Clients on " + socket.getInetAddress() + ":" + socket.getPort() + "\n");

                // if server was to shut down
                if (!runT)
                    break;

                // Create thread for each client
                CThread cThread = new CThread(socket);
                ClientList.add(cThread);
                cThread.start();
            }
            // if server shuts down close inout and output streams
            try {

                for (int i = 0; i < ClientList.size(); ++i) {
                    CThread cThread = ClientList.get(i);
                    try {
                        cThread.inputStream.close();
                        cThread.outputStream.close();
                        cThread.socket.close();
                    } catch (IOException ioE) {
                        ioE.printStackTrace();
                    }
                }
                serverSocket.close();
            }

            // Error Handler
            catch (Exception e) {
                display("Exception closing the server and clients: " + e);
            }
        }
        // server crash
        catch (IOException e) {
            String msg = " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }

    private void display(String msg) {
        serverGUI.txtrServerLog.append(msg + "\n");
    }

    public void readUserNameInMap(HashMap<String, String> map, File fileName) {
        try {

            String line;

            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(":", 2);
                if (parts.length >= 2) {
                    String key = parts[0];
                    String value = "Disconnected";
                    map.put(key, value);
                } else {
                    System.out.println("ignoring line: " + line);
                }
            }

            for (String key : map.keySet()) {
                System.out.println(key + ":" + map.get(key));
            }

            reader.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void writeUsernamesInFile(HashMap<String, String> map, File fileName) {
        BufferedWriter bf = null;
        try {

            bf = new BufferedWriter(new FileWriter(fileName));
            for (Entry<String, String> entry : map.entrySet()) {
                bf.write(entry.getKey() + ":" + entry.getValue());
                bf.newLine();
            }

            bf.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                // close the writer
                bf.close();
            } catch (Exception e) {
            }
        }
    }

    protected void stop() {

        runT = false;
        writeUsernamesInFile(activeClient, usernameFile);
        try {
            new Socket("localhost", port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class CThread extends Thread {

        Socket socket;
        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;

        int id;

        String user;

        CThread(Socket socket) {
            // unique id for each client
            id = ++clientID;
            this.socket = socket;
            try {

                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());

                user = (String) inputStream.readObject();

                display(user + " just connected");
            } catch (IOException e) {

                display("Exception creating new Input/output Streams: " + e);
                return;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public String getUserName() {
            return user;
        }

        public void setUserName(String user) {
            this.user = user;
        }

        private void display(String string) {
            serverGUI.updateLog(string + "\n");

        }

        private boolean respond(String[] respose) {

            if (!socket.isConnected()) {
                close();
                return false;
            }

            try {
                outputStream.writeObject(respose);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        public void run() {

            boolean runT = true;
            while (runT) {

                String[] request = new String[7];
                String[] response = new String[7];
                try {

                    request = (String[]) inputStream.readObject();

                    Date date = new Date();
                    String strDateFormat = "DD/MM/YYYY hh:mm:ss a";
                    DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

                    String formattedDate = dateFormat.format(date);

                    response[0] = "HTTP/1.1 200 OK";
                    response[1] = "Server: localhost";
                    response[2] = "Content-type: message";
                    response[3] = "Date: " + formattedDate;
                    response[4] = "Content-Length: " + request[5].length();
                    response[5] = "\r\n";
                    response[6] = "";

                    if (request[0].contains("GET") && request[3].contains("client-list")) {
                        response[2] = "Content-type: client-list";

                        for (Entry ct : activeClient.entrySet()) {
                            response[6] += ct.getKey() + ",";
                        }

                        response[6] += "broadcast";

                        String requestLine = "";
                        for (String s : request) {
                            requestLine += s + " ";
                        }

                        serverGUI.updateLog(requestLine);

                        for (CThread ct : ClientList) {

                            if (!ct.respond(response))
                                ClientList.remove(ct);
                        }

                    }

                    else if (request[0].contains("PUT") && (request[3].contains("username"))) {

                        String userDest = "NewClient";

                        CThread currCT = null;
                        for (CThread ct : ClientList) {
                            if (ct.getUserName().equalsIgnoreCase(userDest)) {
                                currCT = ct;
                                break;
                            }
                        }

                        String newusr = request[6];

                        String resp = "accepted";

                        if (activeClient.containsKey(newusr)) {
                            if (activeClient.get(newusr).equalsIgnoreCase("Connected"))
                                resp = "rejected";
                            else {
                                resp = "accepted again";
                                activeClient.put(newusr, "Connected");
                                currCT.setUserName(newusr);
                            }
                        } else {
                            activeClient.put(newusr, "Connected");
                            currCT.setUserName(newusr);
                        }
                        request[6] = resp;
                        String requestLine = "";
                        for (String s : request) {
                            requestLine += s + " ";
                        }

                        response[2] = "Content-type: username";
                        response[6] = request[6];
                        response[3] = newusr;

                        currCT.respond(response);

                        serverGUI.updateLog(requestLine + ": Connection from " + newusr);
                        serverGUI.updateTable(activeClient);
                        respond(response);
                    }

                    else if (request[0].contains("POST")
                            && (request[3].contains("BROADCAST") || request[3].contains("broadcast"))) {

                        String requestLine = "";
                        for (String s : request) {
                            requestLine += s + " ";
                        }

                        serverGUI.updateLog(requestLine);

                        response[2] = request[3];
                        response[6] = request[6];
                        for (Entry entry : activeClient.entrySet()) {
                            String tempusername = entry.getKey().toString();
                            writeMsgToUserFile(tempusername, response[3]);
                            writeMsgToUserFile(tempusername, response[6]);
                        }
                        for (CThread ct : ClientList) {

                            if (!ct.respond(response))
                                ClientList.remove(ct);
                        }

                    }

                    else if (request[0].contains("POST") && (request[3].contains("unicast"))) {

                        String requestLine = "";
                        for (String s : request) {
                            requestLine += s + " ";
                        }

                        serverGUI.updateLog(requestLine);

                        String userDest = request[2].substring(11).trim();
                        System.out.println("Server unicast: " + (userDest));

                        response[2] = request[3];
                        String sender = request[4].substring(5).trim();
                        response[6] = request[6];

                        writeMsgToUserFile(userDest, response[3]);
                        writeMsgToUserFile(userDest, response[6]);
                        writeMsgToUserFile(sender, response[3]);
                        writeMsgToUserFile(sender, response[6]);

                        for (CThread ct : ClientList) {
                            if (ct.getUserName().equalsIgnoreCase(userDest)) {
                                ct.respond(response);
                            }
                            if (ct.getUserName().equalsIgnoreCase(sender)) {
                                ct.respond(response);
                            }
                        }
                    }

                    else if (request[0].contains("POST") && (request[3].contains("multicast"))) {

                        String requestLine = "";
                        for (String s : request) {
                            requestLine += s + " ";
                        }

                        serverGUI.updateLog(requestLine);

                        String[] userDest = request[2].substring(11).trim().split(";");
                        System.out.println("Server multi: " + Arrays.toString(userDest));
                        response[2] = request[3];

                        String sender = request[4].substring(5).trim();
                        response[6] = request[6];

                        for (String tempusername : userDest) {
                            writeMsgToUserFile(tempusername, response[3]);
                            writeMsgToUserFile(tempusername, response[6]);
                        }

                        for (String s : userDest) {
                            for (CThread ct : ClientList) {
                                System.out.println("This is multicast: " + s + " User:" + ct.getUserName() + " ->");
                                System.out.print(ct.getUserName().equalsIgnoreCase(s) + "\n");
                                if (ct.getUserName().equalsIgnoreCase(s)) {
                                    ct.respond(response);
                                }
                            }
                        }
                    }

                    else if (request[0].contains("DELETE")) {

                        String requestLine = "";
                        for (String s : request) {
                            requestLine += s + " ";
                        }

                        serverGUI.updateLog(requestLine);

                        String userAgent = request[2].substring(12);

                        Iterator it = ClientList.iterator();
                        while (it.hasNext()) {
                            CThread ct = (CThread) it.next();
                            if (userAgent.equalsIgnoreCase(ct.getUserName())) {
                                activeClient.put(ct.getUserName(), "Disconnected"); // change the value of username to
                                                                                    // disconnected whos logging out
                                it.remove();
                            }
                        }

                        serverGUI.updateTable(activeClient);
                    }

                    else if (request[0].contains("GET") && request[3].contains("message-file")) {

                        String userDest = request[2].substring(12);

                        String fileContent = readMsgsFromUserFile(userDest);

                        System.out.println("Reading msg file: Content:\n" + fileContent);
                        response[2] = request[3];

                        response[6] = fileContent;

                        String requestLine = "";
                        for (String s : request) {
                            requestLine += s + " ";
                        }

                        serverGUI.updateLog(requestLine);

                        for (CThread ct : ClientList) {
                            if (ct.getUserName().equalsIgnoreCase(userDest)) {
                                ct.respond(response);
                            }
                        }

                    }
                }

                catch (IOException e) {
                    display(user + " Exception reading Streams: " + e);
                    break;
                } catch (ClassNotFoundException e2) {
                    break;
                }
            }

            ClientList.remove(id - 1);
            close();
        }

        private void close() {
            try {
                if (outputStream != null)
                    outputStream.close();
                if (inputStream != null)
                    inputStream.close();
                if (socket != null)
                    socket.close();
                serverGUI.updateLog("\n" + user + "Disconnected\n");
                activeClient.put(user, "Disconnected");
                serverGUI.updateTable(activeClient);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        synchronized void remove(int id) {
            //
            for (CThread ct : ClientList) {
                if (ct.id == id) {
                    ClientList.remove(ct);
                    return;
                }
            }
        }

        public void writeMsgToUserFile(String username, String msg) {
            try {

                File file = new File(".//src//server//" + username + ".txt");
                if (file.createNewFile())
                    System.out.println("File is created!");
                else
                    System.out.println("File already exists.");

                FileWriter fr = new FileWriter(file, true);

                BufferedWriter br = new BufferedWriter(fr);

                PrintWriter pr = new PrintWriter(br);
                pr.println(msg);

                pr.close();

                br.close();

                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String readMsgsFromUserFile(String username) {

            StringBuilder contentBuilder = new StringBuilder();
            try {

                File file = new File(".//src//server//" + username + ".txt");

                if (file.createNewFile())
                    System.out.println("File is created!");
                else
                    System.out.println("File already exists.");

                BufferedReader br = new BufferedReader(new FileReader(file));

                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    contentBuilder.append(sCurrentLine).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            display("Queue has been cleared");

            return contentBuilder.toString();
        }
    }
}