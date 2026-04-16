package liza;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        MyPanel panel = new MyPanel();

        JFrame frame = new JFrame();
        frame.setSize(800, 600); // Initial size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Не простая модель");
        frame.add(panel);
        frame.setResizable(true); // Enable resizing
        frame.setVisible(true);
    }
}