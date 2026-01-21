import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;

public class MyPanel extends JPanel {
    public static final double PIXELS_IN_METER = 1;
    public static final double PIXELS_IN_METERPERSECOND = 1;

    public static final int HEIGHT_PIXELS = 600;
    public static final int WIDTH_PIXELS = 800;

    public static final int PADDING_PX = 30;
    public static final int PHYSICS_HEIGHT = HEIGHT_PIXELS - 2 * PADDING_PX;
    public static final double MAX_HEIGHT_METERS = PHYSICS_HEIGHT / PIXELS_IN_METER;
    public static final int PHYSICS_WIDTH = WIDTH_PIXELS - PADDING_PX;
    public static final double MAX_WIDTH_METERS = PHYSICS_WIDTH / PIXELS_IN_METER;

    JTextField angleField, speedField;
    JLabel infoLabel;

    double startAngle = 45;
    double startSpeed = 10;

    boolean showMouseAngle = false;

    double startXm = 0;
    double startYm = 100;
    Bird bird = new Bird(startXm, startYm);

    TrajectoryDrawer trajectory = new TrajectoryDrawer();

    boolean useMouse = true;
    private double startTimeSec = 0;

    public MyPanel() {
        setBackground(Color.CYAN);
        setLayout(null);

        // Создаем панель ввода
        JPanel inputPanel = new JPanel();

        inputPanel.setBackground(new Color(240, 240, 240));
        inputPanel.setBounds(100, 10, 350, 120);
        inputPanel.setLayout(new GridLayout(4, 2, 5, 5));

        inputPanel.add(new JLabel("Угол (град):"));
        angleField = new JTextField("45.0");
        angleField.addActionListener(e -> {
            updateCurrentAngleSpeedFromScreen();
            showMouseAngle = false; // Сбрасываем отображение угла мыши
        });
        inputPanel.add(angleField);

        inputPanel.add(new JLabel("Скорость (speed):"));
        speedField = new JTextField("10.0");
        speedField.addActionListener(e -> updateCurrentAngleSpeedFromScreen());
        inputPanel.add(speedField);

        JButton launchButton = new JButton("Запустить");
        launchButton.addActionListener(e -> launchBird());
        inputPanel.add(launchButton);

        JButton resetButton = new JButton("Сброс");
        resetButton.addActionListener(e -> resetSimulation());
        inputPanel.add(resetButton);

        inputPanel.add(new JLabel("наведение"));
        JToggleButton useMouseBtn = new JToggleButton("?Мышь");
        useMouseBtn.setSelected(useMouse);
        useMouseBtn.addItemListener((e) -> useMouse = e.getStateChange() == ItemEvent.SELECTED);
        inputPanel.add(useMouseBtn);

        JLabel timeLabel = new JLabel("замедление времени x1");
        inputPanel.add(timeLabel);
        JSlider timeSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, (int) Bird.slowTimeBy);
        timeSlider.addChangeListener(e -> {
            Bird.slowTimeBy = timeSlider.getValue();
            timeLabel.setText("замедление времени x%d".formatted((int) Bird.slowTimeBy));
        });
        inputPanel.add(timeSlider);

        inputPanel.setLayout(new GridLayout(0, 2));
        add(inputPanel);

        // Панель информации
        infoLabel = new JLabel("");
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        infoLabel.setBounds(450, 10, 600, 200);
        add(infoLabel);

        // мышь и клава
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                onKeyPressed(e);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                onMouseMoved(e);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClicked(e);
            }
        });
        setFocusable(true);

        // Таймер для обновления физики
        Timer timer = new Timer(10, e -> updateSimulation());
        timer.start();
    }

    private void launchBird() {
        if (bird.isLaunched || bird.hitBoundary) return;
        bird.launch(startSpeed, startAngle);
        showMouseAngle = false;
        startTimeSec = Time.seconds();
    }

    private void updateAngleSpeedFieldsFromCurrent() {
        angleField.setText(String.format(Locale.ENGLISH, "%.1f", startAngle));
        speedField.setText(String.format(Locale.ENGLISH, "%.1f", startSpeed));
        bird.velocity0.x = startSpeed * Math.cos(Math.toRadians(startAngle));
        bird.velocity0.y = startSpeed * Math.sin(Math.toRadians(startAngle));
    }

    private void updateCurrentAngleSpeedFromScreen() {
        try {
            startAngle = Double.parseDouble(angleField.getText());
            startSpeed = Double.parseDouble(speedField.getText());
            repaint();
        } catch (NumberFormatException e) {
            updateAngleSpeedFieldsFromCurrent();
            repaint();
        }
    }

    private void resetSimulation() {
        bird.reset();
        showMouseAngle = false;
        updateAngleSpeedFieldsFromCurrent();
        repaint();
    }

    private void updateSimulation() {

        bird.update();
        repaint();

        // Если птица врезалась в границу экрана
        if (bird.hitBoundary) {
            // Автоматический сброс через небольшую задержку
            Timer resetTimer = new Timer(1500, e -> resetSimulation());
            resetTimer.setRepeats(false);
            resetTimer.start();
            return;
        }

        // Обновляем информацию
        updateInfo();
        repaint();
    }

    private void updateInfo() {
        infoLabel.setText(String.format(
                """
                        <html>
                        Параметры:<br>
                        Начальные: скорость=%.1fm/s, угол скорости=%.1f°<br>
                        Текущие: скорость=%.1fm/s, угол скорости=%.1f°<br>
                        Путь=%.1fm, Перемещение=%.1fm<br>
                        Макс.высота=%.1fm, Макс.дальность=%.1fm<br>
                        Время=%.3fc<br>
                        </html>""",
                startSpeed,
                startAngle,
                bird.physics.getVelocity(),
                bird.physics.getVelocityAngleDeg(),
                bird.physics.maxHeight,
                bird.physics.maxDistance,
                bird.physics.totalPathLength,
                bird.physics.totalDistance,
                bird.isLaunched ? (Time.seconds() - startTimeSec) / Bird.slowTimeBy : 0
        ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Ruller.paintRulerH(g);
        Ruller.paintRulerV(g);

        // Рисуем предварительную траекторию если птица не запущена и не врезалась
        if (!bird.isLaunched && !bird.hitBoundary) {
            trajectory.clearHistory();
            trajectory.predictAndDraw(g, startXm, startYm, startAngle, startSpeed);
        }

        if (bird.isLaunched && !bird.hitBoundary) {
            trajectory.drawHistory(g, bird);
        }

        // Рисуем птицу (если она не врезалась в границу)
        if (!bird.hitBoundary) {
            bird.paint(g);
        } else {
            // Показываем сообщение о столкновении
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("СТОЛКНОВЕНИЕ!", getWidth() / 2, getHeight() / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Возврат через 1 секунд...", getWidth() / 2, getHeight() / 2 + 50);
        }
    }

    // Методы KeyListener
    public void onKeyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            launchBird();
        }
    }


    // Метод для вычисления угла по координатам клика мыши
    private double calculateAngleFromClick(int mouseX, int mouseY) {
        double dx = screenXtoXMeters(mouseX) - startXm;
        double dy = screenYtoYMeters(mouseY) - startYm;

        // Вычисляем угол в радианах с помощью Math.atan2
        // atan2 принимает (y, x) и возвращает угол от -PI до PI
        double angleRad = Math.atan2(dy, dx);

        // Конвертируем в градусы
        double angleDeg = Math.toDegrees(angleRad);

        // Округляем до десятых
        angleDeg = Math.round(angleDeg * 10.0) / 10.0;

        return angleDeg;
    }

    // Методы MouseListener
    public void onMouseClicked(MouseEvent e) {
        if (!bird.isLaunched && !bird.hitBoundary && useMouse) {
            launchBird();
        }
    }

    public void onMouseMoved(MouseEvent e) {
        if (!bird.isLaunched && !bird.hitBoundary && useMouse) {
            showMouseAngle = true;

            int mouseX = e.getX();
            int mouseY = e.getY();
            startAngle = calculateAngleFromClick(mouseX, mouseY);
            startSpeed = Math.sqrt(Math.pow(screenYtoYMeters(mouseY) - startYm, 2) + Math.pow(screenXtoXMeters(mouseX) - startXm, 2)) / Math.sqrt(PHYSICS_HEIGHT * PHYSICS_HEIGHT + PHYSICS_WIDTH * PHYSICS_WIDTH) * 900;

            updateAngleSpeedFieldsFromCurrent();
            repaint();
        }
    }

    public double screenXtoXMeters(int screenXpx) {
        return (screenXpx - PADDING_PX) / PIXELS_IN_METER;
    }

    public double screenYtoYMeters(int screenYpx) {
        return (PHYSICS_HEIGHT - screenYpx) / PIXELS_IN_METER;
    }
}
