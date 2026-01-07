import java.awt.*;

public class Target {
    int x;
    int y;
    int diameter = 40;
    boolean active = true;

    public Target(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void paint(Graphics g) {
        if (!active) return;

        g.setColor(Color.RED);
        g.fillOval(x, y, diameter, diameter);
        g.setColor(Color.WHITE);
        g.drawOval(x + 5, y + 5, diameter - 10, diameter - 10);
        g.setColor(Color.BLACK);
        g.fillOval(x + diameter / 2 - 5, y + diameter / 2 - 5, 10, 10);
    }
}