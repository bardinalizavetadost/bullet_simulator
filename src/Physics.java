import java.util.HashMap;

public class Physics {
    public static final String F_GRAVITY = "gravity";
    public static final String F_DRAG = "drag";
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
    PhyConfig config;

    public Physics() {
    }

    void setup(PhyConfig config) {
        totalDistance = 0;
        totalPathLength = 0;
        maxHeight = 0;
        maxDistance = 0;

        force = new Vector(0, 0);
        acceleration = new Vector(0, 0);

        double angleRad = Math.toRadians(config.launchAngleDeg);
        double v0X = config.startSpeedMS() * Math.cos(angleRad);
        double v0Y = config.startSpeedMS() * Math.sin(angleRad);
        velocity = new Vector(v0X, v0Y);
        position = new Vector(config.position0X, config.position0Y);
        position0 = new Vector(config.position0X, config.position0Y);

        this.config = config;
        this.forces.put(F_GRAVITY, new Vector(0, -9.81 * this.config.massKg()));
    }

    void update(double timeDeltaSec) {
        updateForce();
        updateAcceleration();
        updatePosition(timeDeltaSec);
    }

    private void updateForce() {
        double S = config.bulletCrossSectionalArea();
        double rho = config.airDensityKgM3();
        Vector drag = new Vector(
                -0.5 * config.ballisticCoef * S * rho * velocity.x * velocity.len() / 1000,
                -0.5 * config.ballisticCoef * S * rho * velocity.y * velocity.len() / 1000
        );
        forces.put(F_DRAG, drag);
    }

    private void updateAcceleration() {
        // сумма всех сил
        force = forces.values().stream().reduce(new Vector(0.0, 0.0), Vector::plus);
        // ускорение из силы
        acceleration = force.times(1 / config.massKg());
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
        maxHeight = Math.max(maxHeight, position.y - position0.y);
        maxDistance = Math.max(maxDistance, position.x - position0.x);
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
