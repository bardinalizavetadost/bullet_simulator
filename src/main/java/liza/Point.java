package liza;

/**
 * Класс Point представляет точку в двумерном пространстве.
 * Используется для хранения координат в метрической системе.
 */
public class Point {
    /**
     * Координата X в метрах
     */
    public double x;

    /**
     * Координата Y в метрах
     */
    public double y;

    /**
     * Конструктор класса Point
     *
     * @param x Координата X в метрах
     * @param y Координата Y в метрах
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Получение координаты X
     *
     * @return Координата X в метрах
     */
    public double getX() {
        return x;
    }

    /**
     * Установка координаты X
     *
     * @param x Координата X в метрах
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Получение координаты Y
     *
     * @return Координата Y в метрах
     */
    public double getY() {
        return y;
    }

    /**
     * Установка координаты Y
     *
     * @param y Координата Y в метрах
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Преобразование координаты X в экранные пиксели
     * Учитывает прокрутку экрана
     *
     * @return Координата X в пикселях
     */
    public int toScreenX() {
        double scrollOffsetX = MyPanel.getStaticScrollOffsetX();
        return (int) ((x - scrollOffsetX) * MyPanel.PIXELS_IN_METER) + MyPanel.PADDING_PX;
    }

    /**
     * Преобразование координаты Y в экранные пиксели
     * Учитывает инверсию оси Y (вверх положительное направление)
     *
     * @return Координата Y в пикселях
     */
    public int toScreenY() {
        return (int) (MyPanel.getStaticPhysicsHeight() - y * MyPanel.PIXELS_IN_METER);
    }
}

