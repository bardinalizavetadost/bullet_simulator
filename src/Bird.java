import java.awt.*;
import java.util.List;

public class Bird {
    static final int DIAMETER_PX = 30;

    double startXMeters;
    double startYMeters;

    double lastTimeSec = 0;

    boolean hitBoundary = false;
    boolean isLaunched = false;

    double mass = 1;
    Physics physics = new Physics(mass);

    // Чтобы замедлить симуляции и было понятно как летит, а не когда по настоящему скорости
    public static double slowTimeBy = 1;

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
        physics.setup(startXMeters, startYMeters, 0, 0);
        updatePositionFromPhysics();
    }

    public void paint(Graphics g) {
        // Если птица улетела за экран, не рисуем ее
        if (hitBoundary) return;

        // Птица
        g.setColor(Color.YELLOW);
        g.fillOval(x, y, DIAMETER_PX, DIAMETER_PX);
        g.setColor(Color.BLACK);
        g.fillOval(x + DIAMETER_PX / 2 - 3, y + DIAMETER_PX / 3, 4, 4);
        g.fillOval(x + DIAMETER_PX / 2 + 3, y + DIAMETER_PX / 3, 4, 4);

        // Рисуем клюв
        g.setColor(Color.ORANGE);
        int[] xPoints = {x + DIAMETER_PX, x + DIAMETER_PX + 10, x + DIAMETER_PX};
        int[] yPoints = {y + DIAMETER_PX / 2, y + DIAMETER_PX / 2, y + DIAMETER_PX / 2 + 5};
        g.fillPolygon(xPoints, yPoints, 3);
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
    }

    public void update() {
        if (!isLaunched || hitBoundary) //  позиция на земле
            return;

        double time = Time.seconds();
        double delta = (time - lastTimeSec) / slowTimeBy;
        lastTimeSec = time;
        physics.update(delta);

        updatePositionFromPhysics();
    }

    void updatePositionFromPhysics() {
        Point nextPoint = physics.getPosition();
        x = nextPoint.toScreenX();
        y = nextPoint.toScreenY();

        checkHitBoundary();
    }

    public boolean checkHitWalls(List<Wall> walls) {
        if (!this.isLaunched) return false;

        // Проверяем столкновения со стенами - отскок
        for (Wall wall : walls) {
            if (handleWallCollision(wall)) {
                return true;
            }
        }
        return false;
    }

    public void checkHitBoundary() {
        if (!this.isLaunched) return;

        // проверка столкновений с землей или потолком
        boolean hit = physics.positionY < 0 || physics.positionY > MyPanel.MAX_HEIGHT_METERS;
        // Проверка столкновения с вертикальной границей экрана
        hit |= physics.positionX < 0 || physics.positionX > MyPanel.MAX_WIDTH_METERS;

        if (hit) {
            hitBoundary = true;
            isLaunched = false;
        }
    }

    public boolean handleWallCollision(Wall wall) {
        if (hitBoundary) return false;

        // центр птицы
        double birdCenterX = x + DIAMETER_PX / 2.0;
        double birdCenterY = y + DIAMETER_PX / 2.0;
        // центр стены
        double wallCenterX = wall.x + wall.width / 2.0;
        double wallCenterY = wall.y + wall.height / 2.0;

        // Определяем, с какой стороны произошло столкновение
        double dx = birdCenterX - wallCenterX;
        double dy = birdCenterY - wallCenterY;

        // расстояние для столкновения
        double halfWidthSumPx = (DIAMETER_PX + wall.width) / 2.0;
        double halfHeightSumPx = (DIAMETER_PX + wall.height) / 2.0;

        // Определяем перекрытие по осям
        double overlapXpx = halfWidthSumPx - Math.abs(dx);
        double overlapYpx = halfHeightSumPx - Math.abs(dy);

        // проверяем ударилось ли
        if (overlapXpx < 0 || overlapYpx < 0) return false;

        if (overlapXpx < overlapYpx) {
            physics.hitX(); // Столкновение с боковой стороной (лево/право)
        } else {
            physics.hitY(); // Столкновение с верхней/нижней стороной
        }

        return true;
    }

    boolean checkCollision(Target target) {
        if (!target.active) return false;
        // Проверка столкновения между птицей и мишенью
        double dx = (x + DIAMETER_PX / 2.0) - (target.x + target.diameter / 2.0);
        double dy = (y + DIAMETER_PX / 2.0) - (target.y + target.diameter / 2.0);
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= (DIAMETER_PX / 2.0 + target.diameter / 2.0);
    }
}