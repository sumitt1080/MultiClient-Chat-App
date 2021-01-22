/**
 * Full Name: Sumit Kumar Thakur
 * Regd no.: 1941012938
 */

package main;

import java.awt.EventQueue;
import server.ServerGUI;

public class ServerMain {
    // Launch GUI Server
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ServerGUI window = new ServerGUI();
                    window.frmServer.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
