# Физическая модель

## Основные принципы

Система моделирует движение пули с учетом сопротивления воздуха. Основные физические принципы:

1. **Закон Ньютона**: F = ma
2. **Сила тяжести**: Fg = mg
3. **Сила сопротивления воздуха**: Fd = ½ * C * ρ * S * v²
4. **Суперпозиция сил**: общее ускорение определяется суммой всех сил

## Математическая модель

### 1. Сила тяжести
Сила тяжести действует вертикально вниз и вычисляется по формуле:
```
Fg = m * g
```
где:
- m - масса пули (кг)
- g - ускорение свободного падения (9.81 м/с²)

В коде:
```java
forces.put(F_GRAVITY, new Vector(0, -9.81 * this.config.massKg()));
```

### 2. Сила сопротивления воздуха
Сила сопротивления воздуха зависит от скорости, формы и плотности среды:
```
Fd = ½ * C * ρ * S * v²
```
где:
- C - баллистический коэффициент
- ρ - плотность воздуха (кг/м³)
- S - площадь поперечного сечения (м²)
- v - скорость (м/с)

В коде:
```java
Vector drag = new Vector(
    -0.5 * config.ballisticCoef * S * rho * velocity.x * velocity.len() / 1000,
    -0.5 * config.ballisticCoef * S * rho * velocity.y * velocity.len() / 1000
);
```

### 3. Плотность воздуха
Плотность воздуха вычисляется по уравнению состояния идеального газа:
```
ρ = P * M / (R * T)
```
где:
- P - давление (Па)
- M - молярная масса воздуха (29 г/моль)
- R - универсальная газовая постоянная (8.31447 Дж/моль·К)
- T - температура (К)

В коде:
```java
double airDensityKgM3() {
    return pressurePa * 29 / (8.31447 * (temperatureC + 273.15)) / 1000;
}
```

### 4. Площадь поперечного сечения
Для цилиндрической пули площадь вычисляется как площадь круга:
```
S = π * (d/2)²
```
где d - диаметр пули.

В коде:
```java
double bulletCrossSectionalArea() {
    return Math.PI * (caliber / 1000) * (caliber / 1000) / 4;
}
```

## Численная модель

### 1. Обновление сил
На каждом шаге симуляции обновляются действующие силы:
```java
private void updateForce() {
    double S = config.bulletCrossSectionalArea();
    double rho = config.airDensityKgM3();
    Vector drag = new Vector(
        -0.5 * config.ballisticCoef * S * rho * velocity.x * velocity.len() / 1000,
        -0.5 * config.ballisticCoef * S * rho * velocity.y * velocity.len() / 1000
    );
    forces.put(F_DRAG, drag);
}
```

### 2. Обновление ускорения
Ускорение вычисляется как сумма всех сил, деленная на массу:
```java
private void updateAcceleration() {
    force = forces.values().stream().reduce(new Vector(0.0, 0.0), Vector::plus);
    acceleration = force.times(1 / config.massKg());
}
```

### 3. Обновление положения
Положение обновляется по методу Эйлера:
```java
private void updatePosition(double timeDeltaSec) {
    velocity = velocity.plus(acceleration.times(timeDeltaSec));
    Vector delta = velocity.times(timeDeltaSec);
    position = position.plus(delta);
}
```

## Метрики полета

### 1. Пройденный путь
Суммарная длина траектории:
```java
totalPathLength += delta.len();
```

### 2. Перемещение
Расстояние от начальной точки:
```java
totalDistance = position.minus(position0).len();
```

### 3. Максимальные значения
Отслеживание максимальных показателей:
```java
maxHeight = Math.max(maxHeight, position.y - position0.y);
maxDistance = Math.max(maxDistance, position.x - position0.x);
```
