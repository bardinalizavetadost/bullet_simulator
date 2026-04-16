package liza;

import java.awt.*;

/**
 * Класс Ruller отвечает за отрисовку линеек на экране.
 * Предоставляет методы для отрисовки вертикальной и горизонтальной линеек.
 */
public class Ruller {
    /**
     * Цвет фона линейки
     */
    private static final Color RULER_BG = Color.WHITE;
    /**
     * Цвет меток линейки
     */
    private static final Color RULER_FG = Color.BLACK;
    /**
     * Цвет границы линейки
     */
    private static final Color RULER_EDGE = Color.BLACK;
    /**
     * Длина основных меток
     */
    private static final int TICK_LENGTH = 10;
    /**
     * Длина промежуточных меток
     */
    private static final int MINOR_TICK_LENGTH = 5;
    /**
     * Отступ текста от метки
     */
    private static final int TEXT_OFFSET = 10;
    /**
     * Шрифт для текста на линейке
     */
    private static final Font TICK_FONT = new Font("SansSerif", Font.BOLD, 12);
    /**
     * Метка для горизонтальной линейки
     */
    private static final String LABEL_X = "(м.)";
    /**
     * Метка для вертикальной линейки
     */
    private static final String LABEL_Y = "(м.)";
    /**
     * Расстояние между метками в метрах
     */
    public static double M_PER_TICK = 100;
    /**
     * Расстояние между метками в пикселях
     */
    public static double PX_PER_TICK = M_PER_TICK * MyPanel.PIXELS_IN_METER;

    /**
     * Отрисовка вертикальной линейки
     * Отображает метки по оси Y с указанием высоты в метрах
     *
     * @param g Графический контекст для отрисовки
     */
    public static void paintRulerV(Graphics g) {
        int physicsHeight = MyPanel.getStaticPhysicsHeight();

        g.setColor(RULER_BG);
        g.fillRect(0, 0, MyPanel.PADDING_PX, physicsHeight);
        g.setColor(RULER_EDGE);
        g.drawLine(MyPanel.PADDING_PX, 0, MyPanel.PADDING_PX, physicsHeight);

        g.setFont(TICK_FONT);
        FontMetrics fm = g.getFontMetrics();

        int tickCount = (int) (physicsHeight / PX_PER_TICK) + 1;

        for (int i = 0; i <= tickCount; i++) {
            int yPos = physicsHeight - (int) (i * PX_PER_TICK);

            if (yPos < 0) continue;

            g.setColor(RULER_FG);
            g.drawLine(MyPanel.PADDING_PX - TICK_LENGTH, yPos, MyPanel.PADDING_PX, yPos);

            if (i < tickCount) {
                int minorYPos = yPos - (int) (PX_PER_TICK / 2);
                if (minorYPos > 0) {
                    g.drawLine(MyPanel.PADDING_PX - MINOR_TICK_LENGTH, minorYPos, MyPanel.PADDING_PX, minorYPos);
                }
            }

            double meterValue = i * M_PER_TICK;
            String label = String.format("%d", (int) meterValue);
            int labelWidth = fm.stringWidth(label);
            int labelX = MyPanel.PADDING_PX - TICK_LENGTH - labelWidth;
            int labelY = yPos + fm.getAscent() / 2;

            g.drawString(label, labelX, labelY);
        }
        g.drawString(LABEL_Y, 0, 20);
    }

    /**
     * Отрисовка горизонтальной линейки
     * Отображает метки по оси X с указанием расстояния в метрах
     * Учитывает прокрутку экрана
     *
     * @param g Графический контекст для отрисовки
     */
    public static void paintRulerH(Graphics g) {
        int physicsHeight = MyPanel.getStaticPhysicsHeight();
        int physicsWidth = MyPanel.getStaticPhysicsWidth();
        MyPanel panel = MyPanel.getInstance();
        int widthPixels = panel != null ? panel.getWidth() : 800;
        double scrollOffsetX = MyPanel.getStaticScrollOffsetX();

        g.setColor(RULER_BG);
        g.fillRect(0, physicsHeight, physicsWidth + MyPanel.PADDING_PX, MyPanel.PADDING_PX);
        g.setColor(RULER_EDGE);
        g.drawLine(MyPanel.PADDING_PX, physicsHeight, widthPixels, physicsHeight);

        g.setFont(TICK_FONT);
        FontMetrics fm = g.getFontMetrics();

        // Calculate which ticks are visible in the current viewport
        double startMeter = Math.floor(scrollOffsetX / M_PER_TICK) * M_PER_TICK;
        double endMeter = scrollOffsetX + (physicsWidth / MyPanel.PIXELS_IN_METER);

        for (double meterValue = startMeter; meterValue <= endMeter; meterValue += M_PER_TICK) {
            // Convert meter value to screen position
            int xPos = (int) ((meterValue - scrollOffsetX) * MyPanel.PIXELS_IN_METER) + MyPanel.PADDING_PX;

            if (xPos < MyPanel.PADDING_PX || xPos > widthPixels) continue;

            g.setColor(RULER_FG);
            g.drawLine(xPos, physicsHeight, xPos, physicsHeight + TICK_LENGTH);

            // Draw minor tick
            int minorXPos = (int) ((meterValue + M_PER_TICK / 2 - scrollOffsetX) * MyPanel.PIXELS_IN_METER) + MyPanel.PADDING_PX;
            if (minorXPos >= MyPanel.PADDING_PX && minorXPos < widthPixels) {
                g.drawLine(minorXPos, physicsHeight, minorXPos,
                        physicsHeight + MINOR_TICK_LENGTH);
            }

            // Draw label with actual meter value
            String label = String.format("%d", (int) meterValue);
            int labelWidth = fm.stringWidth(label);
            int labelX = xPos - labelWidth / 2;
            int labelY = physicsHeight + TICK_LENGTH + TEXT_OFFSET;

            g.drawString(label, labelX, labelY);
        }
        g.drawString(LABEL_X, physicsWidth - 10, physicsHeight + 20);
    }
}
