/**
 * Full Name: Sumit Kumar Thakur
 * Regd no.: 1941012938
 */

package server;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;
import java.awt.Font;

public class ServerGUI implements ActionListener, WindowListener {

    public JFrame frmServer;
    public JTextArea txtrServerLog;
    Server server;
    public JButton btnStartServer;
    boolean keepGoing;
    private JTable table;
    JButton btnRefresh;
    DefaultTableModel model;

    // Create the Application
    public ServerGUI() {
        initialize();
    }

    // Initialize components of GUI
    private void initialize() {
        frmServer = new JFrame();
        frmServer.setTitle("Server");
        frmServer.setResizable(false);
        frmServer.setBounds(100, 100, 490, 519);
        frmServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        btnStartServer = new JButton("Start Server");

        JScrollPane scrollPane_1 = new JScrollPane();

        JLabel lblServerLog = new JLabel("Server Logs");

        JLabel lblClients = new JLabel("Clients");
        lblClients.setFont(new Font("Tahoma", Font.PLAIN, 13));

        btnRefresh = new JButton("Referesh List");
        btnRefresh.setVisible(false);
        btnRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                updateTable(server.getActiveClient());
            }
        });

        JScrollPane scrollPane = new JScrollPane();
        GroupLayout groupLayout = new GroupLayout(frmServer.getContentPane());
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
                .createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup().addGap(31).addGroup(groupLayout
                                .createParallelGroup(Alignment.LEADING).addComponent(lblServerLog)
                                .addGroup(groupLayout.createSequentialGroup()
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 433,
                                                        Short.MAX_VALUE)
                                                .addGroup(groupLayout.createSequentialGroup()
                                                        .addComponent(lblClients, GroupLayout.PREFERRED_SIZE, 73,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addGap(77).addComponent(btnRefresh))
                                                .addComponent(scrollPane_1)))))
                        .addGroup(groupLayout.createSequentialGroup().addGap(135).addComponent(btnStartServer,
                                GroupLayout.PREFERRED_SIZE, 188, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE)));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(groupLayout
                .createSequentialGroup().addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(groupLayout
                        .createSequentialGroup()
                        .addComponent(btnStartServer, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                        .addGap(24).addComponent(btnRefresh).addGap(13))
                        .addComponent(lblClients, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE).addGap(18)
                .addComponent(lblServerLog).addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE).addGap(17)));

        txtrServerLog = new JTextArea();
        scrollPane_1.setViewportView(txtrServerLog);

        model = new DefaultTableModel();
        table = new JTable(model);
        model.addColumn("Username");
        model.addColumn("Status");
        scrollPane.setViewportView(table);
        frmServer.getContentPane().setLayout(groupLayout);
        btnStartServer.addActionListener(this);
    }

    // handle Connect Button
    @Override
    public void actionPerformed(ActionEvent e) {
        // stop server if already running
        if (server != null) {
            server.stop();
            server = null;
            btnStartServer.setText("Start");
            return;
        }
        // start the server on localhost:3000
        server = new Server(this);
        // Start server Thread and continuously listen to the client for requests
        new ServerRunning().start();
        // set text of start button to stop
        txtrServerLog.append("Server Running....\n ");

        btnStartServer.setText("Stop");

    }

    // Print on Server Log
    void updateLog(String message) {
        txtrServerLog.append(message + "\n");
    }

    public void updateTable(HashMap<String, String> clientMap) {
        int row = 0;
        model.setRowCount(0);
        for (Map.Entry<String, String> entry : clientMap.entrySet()) {
            String[] temp = { entry.getKey(), entry.getValue() };
            model.addRow(temp);
            row++;
        }
    }

    // Creating a Thread for Server to hook with client
    class ServerRunning extends Thread {
        public void run() {

            // continue executing until server manually stopped
            server.startServer();

            // server stopped
            btnStartServer.setText("Start");
            updateLog("Server has been Stopped\n");
            server = null;
        }
    }

    @Override
    public void windowClosing(WindowEvent e) {

        if (server != null) {
            try {
                // close server connection
                server.stop();
            } catch (Exception eClose) {
            }
            server = null;
        }
        System.exit(0);
    }

    // handle close
    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }
}
