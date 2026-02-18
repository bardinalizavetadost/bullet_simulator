public class PhyConfig {
    double position0X = 0, position0Y = 100;
    double launchAngleDeg = 45;

    double temperatureC = 25;
    double pressurePa = 101330;
    double startEnergyJ = 1700;
    double ballisticCoef = 0.185;
    double massG = 3.43;
    double caliber = 5.45;

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
