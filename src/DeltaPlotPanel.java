import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

public class DeltaPlotPanel extends JPanel {

    private final List<Point> yDrag;
    private final List<Point> yNoDrag;
    private final List<Point> delta;

    private double scrollX = 0;
    private double scaleY = 1.0;
    private int hoveredIndex = -1;

    public DeltaPlotPanel(List<Point> yDrag, List<Point> yNoDrag) {
        this.yDrag = yDrag;
        this.yNoDrag = yNoDrag;
        this.delta = calculateDelta(yDrag, yNoDrag);

        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                scrollX += e.getWheelRotation() * 20;
                repaint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int newHoveredIndex = -1;
                if (delta.isEmpty()) {
                    setHoveredIndex(newHoveredIndex);
                    repaint();
                    return;
                }
                double minX = delta.getFirst().x;
                double maxX = delta.getLast().x;
                int padding = 50;
                int chartWidth = getWidth() - 2 * padding;

                for (int i = 0; i < delta.size(); i++) {
                    Point p = delta.get(i);
                    int x = (int) (padding + ((p.x - minX - scrollX) / (maxX - minX)) * chartWidth);
                    if (Math.abs(e.getX() - x) < 5) {
                        newHoveredIndex = i;
                        break;
                    }
                }
                if (newHoveredIndex != hoveredIndex) {
                    setHoveredIndex(newHoveredIndex);
                    repaint();
                }
            }
        });
    }

    private List<Point> calculateDelta(List<Point> yDrag, List<Point> yNoDrag) {
        List<Point> delta = new ArrayList<>();
        int n = Math.min(yDrag.size(), yNoDrag.size());
        for (int i = 0; i < n; i++) {
            double x = yDrag.get(i).x;
            double y1 = yDrag.get(i).y;
            double y2 = yNoDrag.get(i).y;
            if (y1 < 0 || y2 < 0) break;
            delta.add(new Point(x, y2 - y1));
        }
        return delta;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (delta.isEmpty()) {
            return;
        }

        double minX = delta.getFirst().x;
        double maxX = delta.getLast().x;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (Point p : delta) {
            if (p.y > 0) { // Log scale only for positive values
                minY = Math.min(minY, p.y);
                maxY = Math.max(maxY, p.y);
            }
        }

        if (minY == Double.MAX_VALUE) {
            return;
        }

        int padding = 50;
        int chartWidth = getWidth() - 2 * padding;
        int chartHeight = getHeight() - 2 * padding;

        // Draw axes
        g2d.drawLine(padding, getHeight() - padding, padding, padding);
        g2d.drawLine(padding, getHeight() - padding, getWidth() - padding, getHeight() - padding);

        // Draw axis labels
        g2d.setColor(Color.BLACK);
        g2d.drawString("X(m)", getWidth() - 50, getHeight() - padding / 2);
        g2d.drawString("delta Y(cm)", 10, padding); // TODO imp

        // Draw x-axis ticks and labels
        for (int i = 0; i < 10; i++) {
            double x = minX + (i / 10.0) * (maxX - minX);
            int xPos = (int) (padding + ((x - minX - scrollX) / (maxX - minX)) * chartWidth);
            if (xPos >= padding && xPos <= getWidth() - padding) {
                g2d.drawLine(xPos, getHeight() - padding, xPos, getHeight() - padding + 5);
                g2d.drawString(String.format("%.1f", x), xPos - 10, getHeight() - padding + 20);
            }
        }

        // Draw y-axis ticks and labels (logarithmic)
        double logMinY = Math.log10(minY);
        double logMaxY = Math.log10(maxY);
        for (double y = Math.pow(10, Math.floor(logMinY)); y <= Math.pow(10, 1 + Math.ceil(logMaxY)); y *= 10) {
            if (y <= 0) continue;
            double logY = Math.log10(y);
            if (logY >= logMinY && logY <= logMaxY) {
                int yPos = (int) (getHeight() - padding - ((logY - logMinY) / (logMaxY - logMinY)) * chartHeight);
                g2d.drawLine(padding - 5, yPos, padding, yPos);
                g2d.drawString(String.format("%.2f", y * 1000), padding - 40, yPos + 5);
            }
        }

        // Plot data
        g2d.setColor(Color.BLUE);

        for (int i = 0; i < delta.size() - 1; i++) {
            Point p1 = delta.get(i);
            Point p2 = delta.get(i + 1);

            if (p1.y <= 0 || p2.y <= 0) continue;

            int x1 = (int) (padding + ((p1.x - minX - scrollX) / (maxX - minX)) * chartWidth);
            int y1 = (int) (getHeight() - padding - ((Math.log10(p1.y) - logMinY) / (logMaxY - logMinY)) * chartHeight);
            int x2 = (int) (padding + ((p2.x - minX - scrollX) / (maxX - minX)) * chartWidth);
            int y2 = (int) (getHeight() - padding - ((Math.log10(p2.y) - logMinY) / (logMaxY - logMinY)) * chartHeight);

            g2d.drawLine(x1, y1, x2, y2);
        }

        // Draw hover label
        if (hoveredIndex != -1) {
            Point p = delta.get(hoveredIndex);
            Point pDrag = yDrag.get(hoveredIndex);
            Point pNoDrag = yNoDrag.get(hoveredIndex);
            int x = (int) (padding + ((p.x - minX - scrollX) / (maxX - minX)) * chartWidth);
            int y = (int) (getHeight() - padding - ((Math.log10(p.y) - logMinY) / (logMaxY - logMinY)) * chartHeight);


            g2d.setColor(Color.BLACK);
            g2d.drawOval(x - 4, y - 4, 8, 8);
            g2d.drawString(
                    String.format("x=%.2f", p.x),
                    x, y + 15
            );
            g2d.drawString(
                    String.format("y=%.2fm", pDrag.y),
                    x, y + 30
            );
            g2d.drawString(
                    String.format("(б/с)y=%.2fm", pNoDrag.y),
                    x, y + 45
            );
            g2d.drawString(
                    String.format("delta=%.1fcm", 1000 * p.y),
                    x, y + 60
            );
        }
    }

    public void setHoveredIndex(int index) {
        this.hoveredIndex = index;
    }
}
