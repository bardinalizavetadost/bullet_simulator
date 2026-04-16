package liza;

import java.util.HashMap;

/**
 * Класс Physics реализует физический движок для расчета движения пули.
 * Учитывает силу тяжести и сопротивление воздуха.
 */
public class Physics {
    /**
     * Идентификатор силы тяжести
     */
    public static final String F_GRAVITY = "gravity";

    /**
     * Идентификатор силы сопротивления воздуха
     */
    public static final String F_DRAG = "drag";

    /**
     * Пройденный путь (суммарная длина траектории)
     */
    double totalPathLength = 0;

    /**
     * Перемещение (расстояние от начальной точки)
     */
    double totalDistance = 0;

    /**
     * Максимальная высота подъема
     */
    double maxHeight = 0;

    /**
     * Максимальная дальность полета
     */
    double maxDistance = 0;

    /**
     * Коллекция действующих сил
     */
    HashMap<String, Vector> forces = new HashMap<>() {{
        put(F_GRAVITY, new Vector(0, 0));
        put(F_DRAG, new Vector(0, 0));
    }};

    /**
     * Результирующая сила
     */
    Vector force;

    /**
     * Ускорение
     */
    Vector acceleration;

    /**
     * Скорость
     */
    Vector velocity;

    /**
     * Текущая позиция
     */
    Vector position;

    /**
     * Начальная позиция
     */
    Vector position0;

    /**
     * Конфигурация физических параметров
     */
    PhyConfig config;

    /**
     * Конструктор класса Physics
     */
    public Physics() {
    }

    /**
     * Инициализация физического движка с заданной конфигурацией
     *
     * @param config Конфигурация физических параметров
     */
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

    /**
     * Обновление состояния физики
     * Выполняет расчет сил, ускорения и позиции
     *
     * @param timeDeltaSec Временной шаг в секундах
     */
    void update(double timeDeltaSec) {
        updateForce();
        updateAcceleration();
        updatePosition(timeDeltaSec);
    }

    /**
     * Обновление действующих сил
     * Рассчитывает силу сопротивления воздуха
     */
    private void updateForce() {
        double S = config.bulletCrossSectionalArea();
        double rho = config.airDensityKgM3();
        Vector drag = new Vector(
                -0.5 * config.ballisticCoef * S * rho * velocity.x * velocity.len(),
                -0.5 * config.ballisticCoef * S * rho * velocity.y * velocity.len()
        );
        forces.put(F_DRAG, drag);
    }

    /**
     * Обновление ускорения
     * Рассчитывает ускорение на основе действующих сил
     */
    private void updateAcceleration() {
        // сумма всех сил
        force = forces.values().stream().reduce(new Vector(0.0, 0.0), Vector::plus);
        // ускорение из силы
        acceleration = force.times(1 / config.massKg());
    }

    /**
     * Обновление позиции
     * Рассчитывает новую позицию на основе ускорения и скорости
     *
     * @param timeDeltaSec Временной шаг в секундах
     */
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

    /**
     * Получение текущей скорости
     *
     * @return Скорость в м/с
     */
    public double getVelocity() {
        return velocity.len();
    }

    /**
     * Получение угла скорости
     *
     * @return Угол в градусах
     */
    public double getVelocityAngleDeg() {
        return velocity.angleDeg();
    }

    /**
     * Получение текущего ускорения
     *
     * @return Ускорение в м/с²
     */
    public double getAcceleration() {
        return acceleration.len();
    }

    /**
     * Получение текущей позиции
     *
     * @return Точка с координатами в метрах
     */
    public Point getPosition() {
        return new Point(position.x, position.y);
    }
}
