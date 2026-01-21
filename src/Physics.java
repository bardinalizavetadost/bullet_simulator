import java.util.HashMap;

public class Physics {
    public static final String F_GRAVITY = "gravity";
    public static final String F_DRAG = "drag";
    final double massKg;
    double totalPathLength = 0; // путь
    double totalDistance = 0; // перемещение
    double maxHeight = 0;
    double maxDistance = 0;

    HashMap<String, Vector> forces = new HashMap<>() {{
        put(F_GRAVITY, new Vector(0, 0));
        put(F_DRAG, new Vector(0, 0));
    }};
    Vector force;
    Vector acceleration;
    Vector velocity;
    Vector position;
    Vector position0;

    public Physics(double massKg) {
        this.massKg = massKg;
        this.forces.put(F_GRAVITY, new Vector(0, -9.81 * massKg));
    }

    void setup(double position0X, double position0Y, double velocity0X, double velocity0Y) {
        totalDistance = 0;
        totalPathLength = 0;
        maxHeight = 0;
        maxDistance = 0;

        force = new Vector(0, 0);
        acceleration = new Vector(0, 0);
        velocity = new Vector(velocity0X, velocity0Y);
        position = new Vector(position0X, position0Y);
        position0 = new Vector(position0X, position0Y);
    }

    void update(double timeDeltaSec) {
        updateForce();
        updateAcceleration();
        updatePosition(timeDeltaSec);
    }

    private void updateForce() {
        double p = 1.29;
        double SD = 0.147;
        double d = 5.45;
        double S = 3.14 * (d / 1000) * (d / 1000) / 4;
        double BC = 0.185;

        Vector drag = new Vector(
                -0.5 * BC * S * p * velocity.x * velocity.len() / 1000,
                -0.5 * BC * S * p * velocity.y * velocity.len() / 1000
        );
        forces.put(F_DRAG, drag);
    }

    private void updateAcceleration() {
        // сумма всех сил
        force = forces.values().stream().reduce(new Vector(0.0, 0.0), Vector::plus);
        // ускорение из силы
        acceleration = force.times(1 / massKg);
    }

    private void updatePosition(double timeDeltaSec) {
        // v(t+delta) = v(t) + a(t) * delta
        velocity = velocity.plus(acceleration.times(timeDeltaSec));
        // r(t+delta) = r(t) + v(t) * delta
        Vector delta = velocity.times(timeDeltaSec);
        position = position.plus(delta);

        // путь
        totalPathLength += delta.len();
        // перемещение
        totalDistance = position.minus(position0).len();
        // макс x/y
        maxHeight = Math.max(maxHeight, position.y - position0.x);
        maxDistance = Math.max(maxDistance, position.x - position0.y);
    }

    public double getVelocity() {
        return velocity.len();
    }

    public double getVelocityAngleDeg() {
        return velocity.angleDeg();
    }

    public double getAcceleration() {
        return acceleration.len();
    }

    public Point getPosition() {
        return new Point(position.x, position.y);
    }
}
