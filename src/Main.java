import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        MyPanel panel = new MyPanel();

        JFrame frame = new JFrame();
        frame.setSize(MyPanel.WIDTH_PIXELS, MyPanel.HEIGHT_PIXELS);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("простая модель");
        frame.add(panel);
        frame.setVisible(true);
    }
}