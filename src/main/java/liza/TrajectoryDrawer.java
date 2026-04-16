package liza;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс TrajectoryDrawer отвечает за расчет и отрисовку траектории полета.
 * Предоставляет методы для предсказания траектории и визуализации истории полета.
 */
public class TrajectoryDrawer {
    /**
     * Конструктор класса TrajectoryDrawer
     */
    public TrajectoryDrawer() {
    }

    /**
     * Получение предсказанной траектории полета
     * Рассчитывает траекторию без реального запуска симуляции
     *
     * @param config Конфигурация физических параметров
     * @return Список точек траектории
     */
    public ArrayList<Point> getPredictedTrajectory(PhyConfig config) {
        Bird demoBird = new Bird(config);
        demoBird.launch(config);

        double timeStepSec = 0.05;
        ArrayList<Point> points = new ArrayList<>();

        points.add(demoBird.physics.getPosition());

        while (!demoBird.hitBoundary && demoBird.isLaunched) {
            demoBird.physics.update(timeStepSec);
            demoBird.updatePositionFromPhysics();

            Point pos = demoBird.physics.getPosition();
            points.add(pos);
        }
        return points;
    }

    /**
     * Предсказание и отрисовка траектории
     * Визуализирует предсказанную траекторию полета
     *
     * @param g         Графический контекст для отрисовки
     * @param colorType Тип цвета для отрисовки
     * @param config    Конфигурация физических параметров
     */
    public void predictAndDraw(Graphics g,
                               int colorType,
                               PhyConfig config) {

        ArrayList<Point> points = getPredictedTrajectory(config);
        drawTrack(g, points, new ArrayList<>(), colorType);
    }

    /**
     * Отрисовка истории полета
     * Визуализирует пройденный путь пули
     *
     * @param g Графический контекст для отрисовки
     * @param b Объект птицы с историей полета
     */
    public void drawHistory(Graphics g, Bird b) {
        ArrayList<Point> pts = new ArrayList<>();
        for (BirdState p : b.history) {
            pts.add(new Point(p.position().x, p.position().y));
        }
        drawTrack(g, pts, new ArrayList<>(), 0);
    }

    /**
     * Отрисовка траектории
     * Визуализирует линию траектории между точками
     *
     * @param g         Графический контекст для отрисовки
     * @param points    Точки траектории
     * @param hits      Точки столкновений
     * @param colorType Тип цвета для отрисовки
     */
    void drawTrack(Graphics g, List<Point> points, List<Point> hits, int colorType) {
        if (points.size() < 2) return;

        Graphics2D g2d = (Graphics2D) g;
        if (colorType == 0) {
            g2d.setColor(new Color(255, 100, 100, 250));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        } else {
            g2d.setColor(new Color(255, 40, 255, 80));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        }

        int prevX = points.get(0).toScreenX();
        int prevY = points.get(0).toScreenY();
        for (Point p : points) {
            int curY = p.toScreenY();
            int curX = p.toScreenX();
            g2d.drawLine(prevX, prevY, curX, curY);
            prevX = curX;
            prevY = curY;
        }

        for (Point p : hits) {
            int curX = p.toScreenX();
            int curY = p.toScreenY();

            g2d.setColor(new Color(255, 0, 0, 200));
            g2d.fillOval(curX, curY, Bird.DIAMETER_PX, Bird.DIAMETER_PX);
        }
    }
}
