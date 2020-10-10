package org.example;

import javax.swing.*;

public class Launcher {
    public static void main( String[] args )
    {
        JFrame window = new JFrame("Заточка");
        JPanel content = new App();
        window.setContentPane(content);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocation(300, 300);
        window.pack();
        window.setResizable(true);
        window.setVisible(true);
    }
}
