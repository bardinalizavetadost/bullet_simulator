/**
 * Класс PhyConfig хранит конфигурацию физических параметров симуляции.
 * Включает характеристики пули, атмосферные условия и параметры запуска.
 */
public class PhyConfig {
    /**
     * Выбранная конфигурация пули из предустановленных
     */
    public BulletConfig selectedConfig = null;

    /**
     * Начальная координата X в метрах
     */
    double position0X = 0;

    /**
     * Начальная координата Y в метрах
     */
    double position0Y = 100;

    /**
     * Угол запуска в градусах
     */
    double launchAngleDeg = 45;

    /**
     * Температура воздуха в градусах Цельсия
     */
    double temperatureC = 15;

    /**
     * Давление воздуха в Паскалях
     */
    double pressurePa = 101330;

    /**
     * Начальная энергия в Джоулях
     */
    double startEnergyJ = 1700;

    /**
     * Баллистический коэффициент
     */
    double ballisticCoef = 0.185;

    /**
     * Масса пули в граммах
     */
    double massG = 3.43;

    /**
     * Калибр пули в миллиметрах
     */
    double caliber = 5.45;

    // Predefined bullet configurations

    /**
     * Конструктор по умолчанию
     */
    public PhyConfig() {
    }

    /**
     * Конструктор с параметрами
     *
     * @param position0X     Начальная координата X в метрах
     * @param position0Y     Начальная координата Y в метрах
     * @param launchAngleDeg Угол запуска в градусах
     * @param temperatureC   Температура воздуха в °C
     * @param pressurePa     Давление воздуха в Па
     * @param startEnergyJ   Начальная энергия в Дж
     * @param ballisticCoef  Баллистический коэффициент
     * @param massG          Масса пули в граммах
     * @param caliber        Калибр в миллиметрах
     */
    public PhyConfig(double position0X, double position0Y, double launchAngleDeg, double temperatureC, double pressurePa, double startEnergyJ, double ballisticCoef, double massG, double caliber) {
        this.position0X = position0X;
        this.position0Y = position0Y;
        this.launchAngleDeg = launchAngleDeg;
        this.temperatureC = temperatureC;
        this.pressurePa = pressurePa;
        this.startEnergyJ = startEnergyJ;
        this.ballisticCoef = ballisticCoef;
        this.massG = massG;
        this.caliber = caliber;
    }

    /**
     * Создание конфигурации без учета сопротивления воздуха
     *
     * @return Новая конфигурация с нулевым баллистическим коэффициентом
     */
    PhyConfig withoutDrag() {
        return new PhyConfig(position0X, position0Y, launchAngleDeg, temperatureC, pressurePa, startEnergyJ, 0, massG, caliber);
    }

    /**
     * Вычисление площади поперечного сечения пули
     *
     * @return Площадь в квадратных метрах
     */
    double bulletCrossSectionalArea() {
        return Math.PI * (caliber / 1000) * (caliber / 1000) / 4;
    }

    /**
     * Преобразование массы из граммов в килограммы
     *
     * @return Масса в килограммах
     */
    double massKg() {
        return massG / 1000;
    }

    /**
     * Вычисление плотности воздуха
     *
     * @return Плотность воздуха в кг/м³
     */
    double airDensityKgM3() {
        return pressurePa * 29 / (8.31447 * (temperatureC + 273.15)) / 1000;
    }

    /**
     * Вычисление начальной скорости
     *
     * @return Скорость в м/с
     */
    double startSpeedMS() {
        return Math.sqrt(startEnergyJ * 2 / massKg());
    }

    /**
     * Установка энергии на основе скорости
     *
     * @param speed Скорость в м/с
     */
    void setEnergyFromSpeed(double speed) {
        startEnergyJ = massKg() * speed * speed / 2;
    }

    /**
     * Перечисление предустановленных конфигураций пуль
     * Содержит характеристики различных боеприпасов
     */
    public enum BulletConfig {
        PM(6.10, 9.27, 0.100, 303, "Пистолет Макарова + 9х18mm"),
        AK74(3.42, 5.62, 0.157, 1385, "AK-74 + 5,45x39 ПС"),
        AK105(3.42, 5.62, 0.157, 1309, "AK-105 + 5,45x39 ПС"),
        AK12(3.42, 5.62, 0.157, 1385, "AK12 + 5,45x39 ПС"),
        AKM(7.90, 7.85, 0.150, 2019, "AKM + 7,62x39 M43"),
        SVD(9.60, 7.92, 0.389, 3355, "СВД + 7,62x54 ЛПС"),
        ASVK(48.30, 12.96, 0.600, 17040, "АСВК + 12,7x108 Б-32"),
        TIGER(9.60, 7.92, 0.381, 3111, "Тигр + Barnaul FMJ 9,6 грамма"),
        TOZ78(2.40, 5.66, 0.120, 174, "ТОЗ-78 + 22lr Охотник-370Э"),
        SV338M(16.20, 8.60, 0.684, 6061, "СВ-338 + .338LM SWISS P Target 8,6×70мм"),
        M24(9.53, 7.82, 0.200, 3306, "M24 + 7,62×51 мм .308 Win NATO M80 FMJ"),
        M82(16.20, 8.59, 0.306, 5784, "M82 + .338 Lapua Magnum 8,6×70мм охотничий FMJBT"),
        AIRGUN3DJ(0.50, 4.50, 0.016, 3, "Пневматика 3 Джоуля + Квинтор-Бета-0.5");

        /**
         * Масса пули в граммах
         */
        public final double massG;

        /**
         * Калибр пули в миллиметрах
         */
        public final double caliber;

        /**
         * Баллистический коэффициент
         */
        public final double ballisticCoef;

        /**
         * Начальная энергия в Джоулях
         */
        public final double startEnergyJ;

        /**
         * Название конфигурации
         */
        public final String name;

        BulletConfig(double massG, double caliber, double ballisticCoef, double startEnergyJ, String name) {
            this.massG = massG;
            this.caliber = caliber;
            this.ballisticCoef = ballisticCoef;
            this.startEnergyJ = startEnergyJ;
            this.name = name;
        }
    }
}
