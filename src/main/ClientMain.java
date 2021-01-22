/**
 * Full Name: Sumit Kumar Thakur
 * Regd no.: 1941012938
 */
package main;

import java.awt.EventQueue;

import client.ClientGUI;

public class ClientMain {
    // Launch Client GUI
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ClientGUI window = new ClientGUI();
                    window.frmClient.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
