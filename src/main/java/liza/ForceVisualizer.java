package liza;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс ForceVisualizer отвечает за визуализацию векторов сил.
 * Отображает силы тяжести, сопротивления воздуха и скорости.
 */
public class ForceVisualizer {
    /**
     * Цвета для различных векторов
     */
    private static final Map<String, Color> FORCE_COLORS = Map.of(
            Physics.F_GRAVITY, Color.BLUE,
            Physics.F_DRAG, Color.RED,
            "speed", Color.BLACK
    );

    /**
     * Форматы отображения значений векторов
     */
    private static final Map<String, String> FORCE_FMT = Map.of(
            Physics.F_GRAVITY, "%s=%.2fN",
            Physics.F_DRAG, "%s=%.6fN",
            "speed", "%s=%.0fm/s"
    );

    /**
     * Размер головки стрелки
     */
    private static final int ARROW_HEAD_SIZE = 8;

    /**
     * Отрисовка векторов сил
     * Визуализирует все действующие силы и скорость
     *
     * @param g       Графический контекст для отрисовки
     * @param forces  Коллекция действующих сил
     * @param speed   Вектор скорости
     * @param originX Координата X начала векторов
     * @param originY Координата Y начала векторов
     */
    public static void drawForceVectors(Graphics g,
                                        HashMap<String, Vector> forces,
                                        Vector speed,
                                        int originX,
                                        int originY) {

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Map.Entry<String, Vector> entry : forces.entrySet()) {
            String forceName = entry.getKey();
            Vector force = entry.getValue();
            drawVector(g2d, forceName, force, originX, originY);
        }
        drawVector(g2d, "speed", speed, originX, originY);
    }

    /**
     * Отрисовка отдельного вектора
     * Визуализирует вектор с меткой и стрелкой
     *
     * @param g2d     Графический контекст для отрисовки
     * @param label   Название вектора
     * @param vector  Вектор для отрисовки
     * @param originX Координата X начала вектора
     * @param originY Координата Y начала вектора
     */
    private static void drawVector(Graphics2D g2d,
                                   String label,
                                   Vector vector,
                                   int originX,
                                   int originY) {

        double magnitude = 100;
        double angle = vector.angleRad();

        int endX = originX + (int) (magnitude * Math.cos(angle));
        int endY = originY - (int) (magnitude * Math.sin(angle));

        Color color = FORCE_COLORS.getOrDefault(label, Color.GRAY);
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(originX, originY, endX, endY);

        drawArrowHead(g2d, originX, originY, endX, endY, color);

        int labelX = originX + (int) (0.75 * magnitude * Math.cos(angle)) - (int) (15 * Math.cos(angle));
        int labelY = originY - (int) (0.75 * magnitude * Math.sin(angle)) + (int) (15 * Math.sin(angle));

        g2d.setColor(color.darker());
        FontMetrics fm = g2d.getFontMetrics();
        String displayLabel = String.format(FORCE_FMT.get(label), label, vector.len());
        int labelWidth = fm.stringWidth(displayLabel);

        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRect(labelX - 5, labelY - fm.getHeight() + 5, labelWidth + 10, fm.getHeight());

        g2d.setColor(color.darker());
        g2d.drawString(displayLabel, labelX, labelY);
    }

    /**
     * Отрисовка головки стрелки
     * Визуализирует треугольную головку стрелки
     *
     * @param g2d   Графический контекст для отрисовки
     * @param x1    Координата X начала линии
     * @param y1    Координата Y начала линии
     * @param x2    Координата X конца линии
     * @param y2    Координата Y конца линии
     * @param color Цвет стрелки
     */
    private static void drawArrowHead(Graphics2D g2d,
                                      int x1, int y1,
                                      int x2, int y2,
                                      Color color) {

        double angle = Math.atan2(y1 - y2, x1 - x2);

        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        xPoints[0] = x2;
        yPoints[0] = y2;

        xPoints[1] = x2 + (int) (ARROW_HEAD_SIZE * Math.cos(angle + Math.PI / 6));
        yPoints[1] = y2 + (int) (ARROW_HEAD_SIZE * Math.sin(angle + Math.PI / 6));

        xPoints[2] = x2 + (int) (ARROW_HEAD_SIZE * Math.cos(angle - Math.PI / 6));
        yPoints[2] = y2 + (int) (ARROW_HEAD_SIZE * Math.sin(angle - Math.PI / 6));

        g2d.setColor(color);
        g2d.fillPolygon(xPoints, yPoints, 3);

        g2d.setColor(color.darker());
        g2d.drawPolygon(xPoints, yPoints, 3);
    }
}
