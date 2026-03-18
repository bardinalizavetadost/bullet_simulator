public class PhyConfig {
    double position0X = 0, position0Y = 100;
    double launchAngleDeg = 45;

    double temperatureC = 25;
    double pressurePa = 101330;
    double startEnergyJ = 1700;
    double ballisticCoef = 0.185;
    double massG = 3.43;
    double caliber = 5.45;

    // Predefined bullet configurations
    public enum BulletConfig {
        M7N10(3.43, 5.45, 0.185, 1700, "5.45x39mm M7N10"),
        M43(7.9, 7.62, 0.295, 2100, "7.62x39mm M43"),
        Luger(8.04, 9.0, 0.131, 520, "9x19mm Parabellum"),
        M193(3.56, 5.56, 0.226, 1760, ".223 Remington M193"),
        FMJ(9.53, 7.62, 0.410, 3300, ".308 Winchester FMJ");

        public final double massG;
        public final double caliber;
        public final double ballisticCoef;
        public final double startEnergyJ;
        public final String name;

        BulletConfig(double massG, double caliber, double ballisticCoef, double startEnergyJ, String name) {
            this.massG = massG;
            this.caliber = caliber;
            this.ballisticCoef = ballisticCoef;
            this.startEnergyJ = startEnergyJ;
            this.name = name;
        }
    }

    public PhyConfig() {
    }

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

    PhyConfig withoutDrag() {
        return new PhyConfig(position0X, position0Y, launchAngleDeg, temperatureC, pressurePa, startEnergyJ, 0, massG, caliber);
    }

    double bulletCrossSectionalArea() {
        return Math.PI * (caliber / 1000) * (caliber / 1000) / 4;
    }

    double massKg() {
        return massG / 1000;
    }

    double airDensityKgM3() {
        return pressurePa * 29 / (8.31447 * (temperatureC + 273.15)) / 1000;
    }

    double startSpeedMS() {
        return Math.sqrt(startEnergyJ * 2 / massKg());
    }

    void setEnergyFromSpeed(double speed) {
        startEnergyJ = massKg() * speed * speed / 2;
    }
}
