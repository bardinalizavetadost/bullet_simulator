public class Vector {
    public double x = 0;
    public double y = 0;

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector() {
    }

    public double len() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    public double angleRad() {
        return Math.atan2(y, x);
    }

    public double angleDeg() {
        return Math.toDegrees(angleRad());
    }

    public Vector plus(Vector v) {
        return new Vector(x + v.x, y + v.y);
    }

    public Vector minus(Vector v) {
        return new Vector(x - v.x, y - v.y);
    }

    public Vector times(double v) {
        return new Vector(x * v, y * v);
    }
}
