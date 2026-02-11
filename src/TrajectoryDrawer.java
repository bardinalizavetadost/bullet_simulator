import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TrajectoryDrawer {

    ArrayList<Point> historyPoints = new ArrayList<>();

    public TrajectoryDrawer() {
    }

    public void predictAndDraw(Graphics g,
                               double positionXm, double positionYm,
                               double startAngle, double startSpeed) {

        Bird demoBird = new Bird(positionXm, positionYm);
        demoBird.launch(startSpeed, startAngle);

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

        drawTrack(g, points, hits);
    }

    public void drawHistory(Graphics g, Bird b) {
        ArrayList<Point> pts = new ArrayList<>();
        for(BirdState p : b.history) {
            pts.add(new Point(p.position().x, p.position().y));
        }
        drawTrack(g, pts, new ArrayList<>());
    }

    public void clearHistory() {
        historyPoints.clear();
    }

    void drawTrack(Graphics g, List<Point> points, List<Point> hits) {
        if (points.size() < 2) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(255, 100, 100, 150));
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

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