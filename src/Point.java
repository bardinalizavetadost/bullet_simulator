public record Point(double x, double y) {

    // метры в экранные писели
    // для x достаточно просто помножить на коэффициент перевода
    public int toScreenX() {
        return (int) (x * MyPanel.PIXELS_IN_METER) + MyPanel.PADDING_PX;
    }

    // метры в экранные писели
    // для y надо перевернуть, так как начало координат экрана в верхнем левом углу
    public int toScreenY() {
        return (int) (MyPanel.PHYSICS_HEIGHT - y * MyPanel.PIXELS_IN_METER);
    }
}

