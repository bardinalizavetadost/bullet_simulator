import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class Bird {
    static final int DIAMETER_PX = 30;
    private static final int BULLET_BODY_WIDTH = 25;
    private static final int BULLET_BODY_HEIGHT = 15;
    private static final int BULLET_NOSE_LENGTH = 10;
    // Чтобы замедлить симуляции и было понятно как летит, а не когда по настоящему скорости
    public static double slowTimeBy = 1;
    double startXMeters;
    double startYMeters;
    double lastTimeSec = 0;
    boolean hitBoundary = false;
    boolean isLaunched = false;
    double mass = 0.00343;
    Physics physics = new Physics(mass);
    double currentTime = 0;
    double lastDumpTime = 0;
    Vector velocity0 = new Vector(1, 0);
    ArrayList<BirdState> history = new ArrayList<>();
    private int x;
    private int y;

    public Bird(double positionXMeters, double positionYMeters) {
        this.startXMeters = positionXMeters;
        this.startYMeters = positionYMeters;
        reset();
    }

    public void reset() {
        isLaunched = false;
        hitBoundary = false;
        history.clear();
        physics.setup(startXMeters, startYMeters, 0, 0);
        updatePositionFromPhysics();
    }

    public void paint(Graphics g) {
        if (hitBoundary) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(x, y);

        if (isLaunched) {
            g2d.rotate(-physics.velocity.angleRad());
        } else {
            g2d.rotate(-velocity0.angleRad());
        }

        Rectangle2D.Double body = new Rectangle2D.Double(
                -BULLET_BODY_WIDTH,
                -BULLET_BODY_HEIGHT / 2.0,
                BULLET_BODY_WIDTH,
                BULLET_BODY_HEIGHT
        );
        g2d.setColor(Color.DARK_GRAY);
        g2d.fill(body);
        g2d.setColor(Color.BLACK);
        g2d.draw(body);

        int[] noseX = {0, BULLET_NOSE_LENGTH, 0};
        int[] noseY = {-BULLET_BODY_HEIGHT / 2, 0, BULLET_BODY_HEIGHT / 2};

        g2d.setColor(Color.BLACK);
        g2d.fillPolygon(noseX, noseY, 3);
        g2d.drawPolygon(noseX, noseY, 3);
        g2d.dispose();

        if (isLaunched) {
            ForceVisualizer.drawForceVectors(g, physics.forces, physics.velocity, x, y);
        }
    }

    public void launch(double speed, double angle) {
        double angleRad = Math.toRadians(angle);
        // Рассчитываем компоненты скорости
        double v0X = speed * Math.cos(angleRad);
        double v0Y = speed * Math.sin(angleRad);

        physics.setup(startXMeters, startYMeters, v0X, v0Y);
        updatePositionFromPhysics();

        isLaunched = true;
        hitBoundary = false;

        lastTimeSec = Time.seconds();
        currentTime = 0;
        lastDumpTime = 0;
    }

    public void update() {
        if (!isLaunched || hitBoundary) //  позиция на земле
            return;

        double time = Time.seconds();
        double delta = (time - lastTimeSec) / slowTimeBy;
        lastTimeSec = time;
        if (delta > 0.1) return;

        dump();

        currentTime += delta;
        physics.update(delta);

        updatePositionFromPhysics();
    }

    void updatePositionFromPhysics() {
        Point nextPoint = physics.getPosition();
        x = nextPoint.toScreenX();
        y = nextPoint.toScreenY();

        checkHitBoundary();
    }

    public void checkHitBoundary() {
        if (!this.isLaunched) return;

        // проверка столкновений с землей или потолком
        boolean hit = physics.position.y < 0 || physics.position.y > MyPanel.MAX_HEIGHT_METERS;
        // Проверка столкновения с вертикальной границей экрана
        hit |= physics.position.x < 0 || physics.position.x > MyPanel.MAX_WIDTH_METERS;

        if (hit) {
            hitBoundary = true;
            isLaunched = false;
        }
    }

    public void dump() {
        if ((currentTime - lastDumpTime) > 0.05) {
            history.add(new BirdState(
                    currentTime,
                    physics.position,
                    physics.velocity
            ));
            lastDumpTime = currentTime;
        }
    }

    public void resetToTime(double time) {
        if (history.isEmpty()) return;
        BirdState nearestState = history.getFirst();
        double best = Double.MAX_VALUE;
        for (BirdState state : history) {
            double dt = Math.abs(state.time() - time);
            if (dt < best) {
                best = dt;
                nearestState = state;
            }
        }
        physics.position = nearestState.position();
        physics.velocity = nearestState.velocity();
        currentTime = nearestState.time();
        lastDumpTime = currentTime;
        updatePositionFromPhysics();
    }

    void dropFutureHistory() {
        ArrayList<BirdState> ok = new ArrayList<>();
        for (BirdState state : history) {
            if (state.time() < currentTime) ok.add(state);
        }
        history.retainAll(ok);
    }
}
