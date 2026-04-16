package liza;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс Bird представляет игровой объект пуля в симуляторе баллистики.
 * Управляет физикой полета, визуализацией и взаимодействием с окружением.
 */
public class Bird {
    /**
     * Диаметр изображения пули в пикселях
     */
    static final int DIAMETER_PX = 100;

    /**
     * Ширина тела пули в пикселях
     */
    private static final int BULLET_BODY_WIDTH = 25;

    /**
     * Высота тела пули в пикселях
     */
    private static final int BULLET_BODY_HEIGHT = 15;

    /**
     * Длина носовой части пули в пикселях
     */
    private static final int BULLET_NOSE_LENGTH = 10;

    /**
     * Кэш изображений пуль для разных конфигураций
     */
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();
    /**
     * Коэффициент замедления времени для визуализации полета
     * Позволяет наблюдать за полетом в замедленном темпе
     */
    public static double slowTimeBy = 1;

    /**
     * Статический блок инициализации для загрузки изображений пуль
     */
    static {
        preloadImages();
    }

    /**
     * Время последнего обновления в секундах
     */
    double lastTimeSec = 0;
    /**
     * Флаг столкновения с границей
     */
    boolean hitBoundary = false;
    /**
     * Флаг состояния запуска
     */
    boolean isLaunched = false;
    /**
     * Физический движок для расчета движения
     */
    Physics physics = new Physics();
    /**
     * Текущее время полета в секундах
     */
    double currentTime = 0;
    /**
     * Время последней записи состояния
     */
    double lastDumpTime = 0;
    /**
     * Начальная скорость для предварительного запуска
     */
    Vector velocity0 = new Vector(1, 0);
    /**
     * История состояний полета для отката времени
     */
    ArrayList<BirdState> history = new ArrayList<>();
    /**
     * Координата X в пикселях
     */
    private int x;
    /**
     * Координата Y в пикселях
     */
    private int y;

    /**
     * Конструктор класса Bird
     *
     * @param config Конфигурация физических параметров
     */
    public Bird(PhyConfig config) {
        physics.config = config;
        reset();
    }

    /**
     * Метод для предварительной загрузки изображений всех конфигураций пуль
     * Загружает изображения из ресурсов и кэширует их для последующего использования
     */
    public static void preloadImages() {
        for (PhyConfig.BulletConfig config : PhyConfig.BulletConfig.values()) {
            String imageName = config.name();
            try (InputStream is = Bird.class.getResourceAsStream("/" + imageName + ".png")) {
                if (is != null) {
                    BufferedImage img = ImageIO.read(is);
                    imageCache.put(imageName, img);
                }
            } catch (IOException e) {
                System.err.println("Could not load image: " + imageName);
            }
        }
    }

    /**
     * Сброс состояния пули в начальное положение
     * Инициализирует физический движок и обновляет позицию
     */
    public void reset() {
        isLaunched = false;
        hitBoundary = false;
        history.clear();
        physics.setup(physics.config);
        updatePositionFromPhysics();
    }

    /**
     * Метод отрисовки пули на экране
     *
     * @param g Графический контекст для отрисовки
     */
    public void paint(Graphics g) {
        if (hitBoundary) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.translate(x, y);

        double angle = isLaunched ? -physics.velocity.angleRad() : -velocity0.angleRad();

        BufferedImage currentBulletImage = null;
        if (physics.config.selectedConfig != null) {
            currentBulletImage = imageCache.get(physics.config.selectedConfig.name());
        }

        if (currentBulletImage != null) {
            g2d.rotate(angle + Math.PI / 2);
            int iw = currentBulletImage.getWidth();
            int ih = currentBulletImage.getHeight();

            // Scale to DIAMETER_PX width while maintaining aspect ratio
            double scale = (double) DIAMETER_PX / Math.max(iw, ih);
            int drawW = (int) (iw * scale);
            int drawH = (int) (ih * scale);

            g2d.drawImage(currentBulletImage, -drawW / 2, -drawH / 2, drawW, drawH, null);
        } else {
            g2d.rotate(angle);
            Rectangle2D.Double body = new Rectangle2D.Double(
                    -BULLET_BODY_WIDTH,
                    -BULLET_BODY_HEIGHT / 2.0,
                    BULLET_BODY_WIDTH,
                    BULLET_BODY_HEIGHT
            );
            g2d.setColor(Color.DARK_GRAY);
            g2d.fill(body);
            g2d.setColor(Color.BLACK);
            g2d.draw(body);

            int[] noseX = {0, BULLET_NOSE_LENGTH, 0};
            int[] noseY = {-BULLET_BODY_HEIGHT / 2, 0, BULLET_BODY_HEIGHT / 2};

            g2d.setColor(Color.BLACK);
            g2d.fillPolygon(noseX, noseY, 3);
            g2d.drawPolygon(noseX, noseY, 3);
        }

        g2d.dispose();

        if (isLaunched) {
            ForceVisualizer.drawForceVectors(g, physics.forces, physics.velocity, x, y);
        }
    }

    /**
     * Подготовка к запуску пули
     * Инициализирует физический движок с заданной конфигурацией
     *
     * @param config Конфигурация физических параметров
     */
    public void preLanuch(PhyConfig config) {
        physics.setup(config);
        updatePositionFromPhysics();
    }

    /**
     * Запуск пули
     * Инициирует полет с заданной конфигурацией
     *
     * @param config Конфигурация физических параметров
     */
    public void launch(PhyConfig config) {
        preLanuch(config);
        isLaunched = true;
        hitBoundary = false;

        lastTimeSec = Time.seconds();
        currentTime = 0;
        lastDumpTime = 0;
    }

    /**
     * Обновление состояния пули
     * Выполняет расчет физики и обновление позиции
     * Вызывается на каждом шаге симуляции
     */
    public void update() {
        if (!isLaunched || hitBoundary) //  позиция на земле
            return;

        double time = Time.seconds();
        double delta = (time - lastTimeSec) / slowTimeBy;
        lastTimeSec = time;
        if (delta > 0.1) return;

        dump();

        currentTime += delta;
        physics.update(delta);

        updatePositionFromPhysics();
    }

    /**
     * Обновление экранных координат на основе физических расчетов
     * Преобразует метрические координаты в пиксельные
     */
    void updatePositionFromPhysics() {
        Point nextPoint = physics.getPosition();
        x = nextPoint.toScreenX();
        y = nextPoint.toScreenY();

        checkHitBoundary();
    }

    /**
     * Проверка столкновения с границами
     * Определяет, достигла ли пуля границы экрана
     */
    public void checkHitBoundary() {
        if (!this.isLaunched) return;
        boolean hit = physics.position.y < 0 || physics.position.y > MyPanel.getStaticMaxHeightMeters();
        if (hit) {
            hitBoundary = true;
            isLaunched = false;
        }
    }

    /**
     * Запись текущего состояния в историю
     * Выполняется с определенной периодичностью для отката времени
     */
    public void dump() {
        if ((currentTime - lastDumpTime) > 0.05) {
            history.add(new BirdState(
                    currentTime,
                    physics.position,
                    physics.velocity
            ));
            lastDumpTime = currentTime;
        }
    }

    /**
     * Восстановление состояния симуляции в определенный момент времени
     * Используется для отката времени
     *
     * @param time Время в секундах
     */
    public void resetToTime(double time) {
        if (history.isEmpty()) return;
        BirdState nearestState = history.get(0);
        double best = Double.MAX_VALUE;
        for (BirdState state : history) {
            double dt = Math.abs(state.time() - time);
            if (dt < best) {
                best = dt;
                nearestState = state;
            }
        }
        physics.position = nearestState.position();
        physics.velocity = nearestState.velocity();
        currentTime = nearestState.time();
        lastDumpTime = currentTime;
        updatePositionFromPhysics();
    }

    /**
     * Удаление будущих состояний из истории
     * Используется при откате времени и перезапуске
     */
    void dropFutureHistory() {
        ArrayList<BirdState> ok = new ArrayList<>();
        for (BirdState state : history) {
            if (state.time() < currentTime) ok.add(state);
        }
        history.retainAll(ok);
    }
}