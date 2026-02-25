import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

public class MagnifyingGlass {
    private int x, y;
    private int size = 150; // размер лупы в пикселях
    private int zoomFactor = 2; // во сколько раз увеличивать
    private boolean visible = false;
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    // Границы для лупы (чтобы не выходила за пределы экрана)
    private int minX, maxX, minY, maxY;

    public MagnifyingGlass(int panelWidth, int panelHeight) {
        setBounds(panelWidth, panelHeight);
        // Начальная позиция - в углу
        this.x = 50;
        this.y = 50;
    }

    public void setBounds(int panelWidth, int panelHeight) {
        this.minX = 0;
        this.maxX = panelWidth - size;
        this.minY = 0;
        this.maxY = panelHeight - size;
    }

    public void startDrag(int mouseX, int mouseY) {
        if (isMouseOver(mouseX, mouseY)) {
            dragging = true;
            dragOffsetX = mouseX - x;
            dragOffsetY = mouseY - y;
        }
    }

    public void drag(int mouseX, int mouseY) {
        if (dragging) {
            // Новая позиция с учетом границ
            x = Math.min(maxX, Math.max(minX, mouseX - dragOffsetX));
            y = Math.min(maxY, Math.max(minY, mouseY - dragOffsetY));
        }
    }

    public void stopDrag() {
        dragging = false;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return visible &&
                mouseX >= x && mouseX <= x + size &&
                mouseY >= y && mouseY <= y + size;
    }

    public void toggle() {
        visible = !visible;
    }

    public void draw(Graphics2D g2d, MyPanel panel) {
        if (!visible) return;

        // Сохраняем текущий клип и трансформацию
        Shape oldClip = g2d.getClip();
        AffineTransform oldTransform = g2d.getTransform();

        // Создаем область лупы (круг)
        Ellipse2D.Double lens = new Ellipse2D.Double(x, y, size, size);

        // Устанавливаем клип на область лупы
        g2d.setClip(lens);

        // Центр лупы
        int centerX = x + size/2;
        int centerY = y + size/2;

        // Смещение так, чтобы крестик указывал на увеличиваемую точку
        double scale = zoomFactor;
        double tx = centerX - (centerX) * scale;
        double ty = centerY - (centerY) * scale;

        g2d.translate(tx, ty);
        g2d.scale(scale, scale);

        // Рисуем ТОЛЬКО дочерние компоненты
        Graphics2D clone = (Graphics2D) g2d.create();
        panel.paintComponents(clone);
        clone.dispose();

        // Возвращаем трансформацию
        g2d.setTransform(oldTransform);

        // Восстанавливаем клип
        g2d.setClip(oldClip);

        // Рисуем рамку лупы
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(Color.BLACK);
        g2d.drawOval(x, y, size, size);

        // Рисуем ручку лупы
        g2d.setStroke(new BasicStroke(8));
        int handleLength = 40;
        int handleX = x + size - 10;
        int handleY = y + size - 10;
        g2d.drawLine(handleX, handleY, handleX + handleLength, handleY + handleLength);

        // Рисуем крестик в центре
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.RED);
        g2d.drawLine(centerX - 10, centerY, centerX + 10, centerY);
        g2d.drawLine(centerX, centerY - 10, centerX, centerY + 10);

        // Текст с увеличением
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(zoomFactor + "x", x + 10, y + 25);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setZoomFactor(int zoom) {
        this.zoomFactor = zoom;
    }
}