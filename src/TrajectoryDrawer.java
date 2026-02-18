import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TrajectoryDrawer {

    public TrajectoryDrawer() {
    }

    public void predictAndDraw(Graphics g,
                               int colorType,
                               PhyConfig config) {

        Bird demoBird = new Bird(config);
        demoBird.launch(config);

        double timeStepSec = 0.05;
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Point> hits = new ArrayList<>();

        points.add(demoBird.physics.getPosition());

        while (!demoBird.hitBoundary && demoBird.isLaunched) {
            demoBird.physics.update(timeStepSec);
            demoBird.updatePositionFromPhysics();

            Point pos = demoBird.physics.getPosition();
            points.add(pos);
        }

        drawTrack(g, points, hits, colorType);
    }

    public void drawHistory(Graphics g, Bird b) {
        ArrayList<Point> pts = new ArrayList<>();
        for(BirdState p : b.history) {
            pts.add(new Point(p.position().x, p.position().y));
        }
        drawTrack(g, pts, new ArrayList<>(), 0);
    }

    void drawTrack(Graphics g, List<Point> points, List<Point> hits, int colorType) {
        if (points.size() < 2) return;

        Graphics2D g2d = (Graphics2D) g;
        if (colorType == 0) {
            g2d.setColor(new Color(255, 100, 100, 250));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        } else {
            g2d.setColor(new Color(255, 40, 255, 80));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        }

        int prevX = points.getFirst().toScreenX();
        int prevY = points.getFirst().toScreenY();
        for (Point p : points) {
            int curY = p.toScreenY();
            int curX = p.toScreenX();
            g2d.drawLine(prevX, prevY, curX, curY);
            prevX = curX;
            prevY = curY;
        }

        for (Point p : hits) {
            int curX = p.toScreenX();
            int curY = p.toScreenY();

            g2d.setColor(new Color(255, 0, 0, 200));
            g2d.fillOval(curX, curY, Bird.DIAMETER_PX, Bird.DIAMETER_PX);
        }
    }
}