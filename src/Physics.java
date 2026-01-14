import java.util.ArrayList;

public class Physics {
    final double massKg;
    double forceX, forceY;

    double position0X, position0Y;
    double positionX, positionY;
    double velocityX, velocityY;
    double accelerationX, accelerationY;

    double totalPathLength = 0; // путь
    double totalDistance = 0; // перемещение
    double maxHeight = 0;

    public Physics(double massKg) {
        this.massKg = massKg;
    }

    void setup(double position0X, double position0Y, double velocity0X, double velocity0Y) {
        totalDistance = 0;
        totalPathLength = 0;
        maxHeight = 0;
        this.position0X = position0X;
        this.position0Y = position0Y;
        positionX = position0X;
        positionY = position0Y;
        velocityX = velocity0X;
        velocityY = velocity0Y;
    }

    void update(double timeDeltaSec) {
        updateForce();
        updateAcceleration();
        updatePosition(timeDeltaSec);
    }

    private void updateForce() {
        double p=1.29;
        double SD=0.147;
        double S=3.73;
        double BC=0.185;
        forceX = -0.5* BC*S*p*velocityX* getVelocity();
        forceY = -0.5* BC*S*p*velocityY* getVelocity()-9.81 * massKg;
    }

    private void updateAcceleration() {
        accelerationX = forceX / massKg;
        accelerationY = forceY / massKg;
    }

    private void updatePosition(double timeDeltaSec) {
        // x
        velocityX += accelerationX * timeDeltaSec;
        double deltaX = velocityX * timeDeltaSec;
        positionX += deltaX;
        // y
        velocityY += accelerationY * timeDeltaSec;
        double deltaY = velocityY * timeDeltaSec;
        positionY += deltaY;
        // путь
        totalPathLength += Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        totalDistance = Math.sqrt(Math.pow(positionX - position0X, 2) + Math.pow(positionY - position0Y, 2));
        maxHeight = Math.max(maxHeight, positionY);
    }

    public double getVelocity() {
        return Math.sqrt(velocityX * velocityX + velocityY * velocityY);
    }

    public double getVelocityAngle() {
        return Math.toDegrees(Math.atan2(velocityY, velocityX));
    }

    public double getAcceleration() {
        return Math.sqrt(accelerationX * accelerationX + accelerationY * accelerationY);
    }

    public Point getPosition() {
        return new Point(positionX, positionY);
    }
}
