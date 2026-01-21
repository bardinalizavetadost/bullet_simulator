import java.awt.*;

public class Ruller {
    public static double M_PER_TICK = 100;
    public static double PX_PER_TICK = M_PER_TICK * MyPanel.PIXELS_IN_METER;

    private static final Color RULER_BG = Color.WHITE;
    private static final Color RULER_FG = Color.BLACK;
    private static final Color RULER_EDGE = Color.BLACK;

    private static final int TICK_LENGTH = 10;
    private static final int MINOR_TICK_LENGTH = 5;
    private static final int TEXT_OFFSET = 10;
    private static final Font TICK_FONT = new Font("SansSerif", Font.BOLD, 12);

    
    public static void paintRulerV(Graphics g) {
        g.setColor(RULER_BG);
        g.fillRect(0, 0, MyPanel.PADDING_PX, MyPanel.PHYSICS_HEIGHT);
        g.setColor(RULER_EDGE);
        g.drawLine(MyPanel.PADDING_PX, 0, MyPanel.PADDING_PX, MyPanel.PHYSICS_HEIGHT);

        g.setFont(TICK_FONT);
        FontMetrics fm = g.getFontMetrics();

        int tickCount = (int) (MyPanel.PHYSICS_HEIGHT / PX_PER_TICK) + 1;

        for (int i = 0; i <= tickCount; i++) {
            int yPos = MyPanel.PHYSICS_HEIGHT - (int)(i * PX_PER_TICK);

            if (yPos < 0) continue;

            g.setColor(RULER_FG);
            g.drawLine(MyPanel.PADDING_PX - TICK_LENGTH, yPos, MyPanel.PADDING_PX, yPos);

            if (i < tickCount) {
                int minorYPos = yPos - (int)(PX_PER_TICK / 2);
                if (minorYPos > 0) {
                    g.drawLine(MyPanel.PADDING_PX - MINOR_TICK_LENGTH, minorYPos, MyPanel.PADDING_PX, minorYPos);
                }
            }

            double meterValue = i * M_PER_TICK;
            String label = String.format("%d", (int)meterValue);
            int labelWidth = fm.stringWidth(label);
            int labelX = MyPanel.PADDING_PX - TICK_LENGTH - labelWidth;
            int labelY = yPos + fm.getAscent() / 2;

            g.drawString(label, labelX, labelY);
        }
    }

    public static void paintRulerH(Graphics g) {
        g.setColor(RULER_BG);
        g.fillRect(MyPanel.PADDING_PX, MyPanel.PHYSICS_HEIGHT, MyPanel.PHYSICS_WIDTH, MyPanel.PADDING_PX);
        g.setColor(RULER_EDGE);
        g.drawLine(MyPanel.PADDING_PX, MyPanel.PHYSICS_HEIGHT, MyPanel.WIDTH_PIXELS, MyPanel.PHYSICS_HEIGHT);

        g.setFont(TICK_FONT);
        FontMetrics fm = g.getFontMetrics();

        int tickCount = (int) (MyPanel.PHYSICS_WIDTH / PX_PER_TICK) + 1;

        for (int i = 0; i <= tickCount; i++) {
            int xPos = MyPanel.PADDING_PX + (int)(i * PX_PER_TICK);

            if (xPos > MyPanel.WIDTH_PIXELS) continue;

            g.setColor(RULER_FG);
            g.drawLine(xPos, MyPanel.PHYSICS_HEIGHT, xPos, MyPanel.PHYSICS_HEIGHT + TICK_LENGTH);

            if (i < tickCount) {
                int minorXPos = xPos + (int)(PX_PER_TICK / 2);
                if (minorXPos < MyPanel.WIDTH_PIXELS) {
                    g.drawLine(minorXPos, MyPanel.PHYSICS_HEIGHT, minorXPos,
                            MyPanel.PHYSICS_HEIGHT + MINOR_TICK_LENGTH);
                }
            }

            double meterValue = i * M_PER_TICK;
            String label = String.format("%d", (int)meterValue);
            int labelWidth = fm.stringWidth(label);
            int labelX = xPos - labelWidth / 2;
            int labelY = MyPanel.PHYSICS_HEIGHT + TICK_LENGTH + TEXT_OFFSET;

            g.drawString(label, labelX, labelY);
        }
    }
}