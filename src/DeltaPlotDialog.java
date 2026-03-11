
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DeltaPlotDialog extends JDialog {
    public DeltaPlotDialog(Frame owner, List<Point> yDrag, List<Point> yNoDrag) {
        super(owner, "разности высот полета", true);
        setSize(800, 600);
        setLocationRelativeTo(owner);

        DeltaPlotPanel plotPanel = new DeltaPlotPanel(yDrag, yNoDrag);
        add(plotPanel);
    }
}
