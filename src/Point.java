public class Point {

    public double x;
    public double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    // метры в экранные писели
    // для x достаточно просто помножить на коэффициент перевода
    // учитываем scroll offset для горизонтальной прокрутки
    public int toScreenX() {
        double scrollOffsetX = MyPanel.getStaticScrollOffsetX();
        return (int) ((x - scrollOffsetX) * MyPanel.PIXELS_IN_METER) + MyPanel.PADDING_PX;
    }

    // метры в экранные писели
    // для y надо перевернуть, так как начало координат экрана в верхнем левом углу
    public int toScreenY() {
        return (int) (MyPanel.getStaticPhysicsHeight() - y * MyPanel.PIXELS_IN_METER);
    }
}

