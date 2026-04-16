import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Класс DeltaPlotDialog представляет диалоговое окно для отображения разницы траекторий.
 * Показывает сравнение траекторий с учетом и без учета сопротивления воздуха.
 */
public class DeltaPlotDialog extends JDialog {
    /**
     * Конструктор класса DeltaPlotDialog
     *
     * @param owner   Родительское окно
     * @param yDrag   Траектория с учетом сопротивления воздуха
     * @param yNoDrag Траектория без учета сопротивления воздуха
     */
    public DeltaPlotDialog(Frame owner, List<Point> yDrag, List<Point> yNoDrag) {
        super(owner, "разности высот полета", true);
        setSize(800, 600);
        setLocationRelativeTo(owner);

        DeltaPlotPanel plotPanel = new DeltaPlotPanel(yDrag, yNoDrag);
        add(plotPanel);
    }
}
