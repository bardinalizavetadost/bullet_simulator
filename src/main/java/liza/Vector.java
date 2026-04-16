package liza;

/**
 * Класс Vector представляет двумерный вектор.
 * Используется для хранения и расчета векторных величин (скорость, ускорение, сила).
 */
public class Vector {
    /**
     * Компонента X вектора
     */
    public double x = 0;

    /**
     * Компонента Y вектора
     */
    public double y = 0;

    /**
     * Конструктор класса Vector
     *
     * @param x Компонента X
     * @param y Компонента Y
     */
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Конструктор по умолчанию
     * Создает вектор с нулевыми компонентами
     */
    public Vector() {
    }

    /**
     * Вычисление длины вектора
     *
     * @return Длина вектора
     */
    public double len() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    /**
     * Вычисление угла вектора в радианах
     *
     * @return Угол в радианах
     */
    public double angleRad() {
        return Math.atan2(y, x);
    }

    /**
     * Вычисление угла вектора в градусах
     *
     * @return Угол в градусах
     */
    public double angleDeg() {
        return Math.toDegrees(angleRad());
    }

    /**
     * Сложение векторов
     *
     * @param v Вектор для сложения
     * @return Новый вектор - результат сложения
     */
    public Vector plus(Vector v) {
        return new Vector(x + v.x, y + v.y);
    }

    /**
     * Вычитание векторов
     *
     * @param v Вектор для вычитания
     * @return Новый вектор - результат вычитания
     */
    public Vector minus(Vector v) {
        return new Vector(x - v.x, y - v.y);
    }

    /**
     * Умножение вектора на скаляр
     *
     * @param v Скалярный множитель
     * @return Новый вектор - результат умножения
     */
    public Vector times(double v) {
        return new Vector(x * v, y * v);
    }
}
