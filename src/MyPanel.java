import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
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
    private final JSlider playbackSlider;
    private final JButton launchButton;

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

    boolean paused = false;
    double maxTime = 0;

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

        launchButton = new JButton("Запустить");
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
        JSlider timeSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, (int) Bird.slowTimeBy);
        timeSlider.addChangeListener(e -> {
            Bird.slowTimeBy = timeSlider.getValue();
            timeLabel.setText("замедление времени x%d".formatted((int) Bird.slowTimeBy));
        });
        inputPanel.add(timeSlider);

        inputPanel.setLayout(new GridLayout(0, 2));
        add(inputPanel);

        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 1);
        playbackSlider.setBounds(50, 150, 400, 50);
        add(playbackSlider);
        playbackSlider.addChangeListener(e -> {
            if(paused) {
                bird.resetToTime(playbackSlider.getValue() / (double) 100 * maxTime);
            }
        });

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
        paused = false;
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
        paused = false;
        updateAngleSpeedFieldsFromCurrent();
        repaint();
    }

    private void updateTimeSlider() {
        int steps = 20;
        int maxval = 100;
        playbackSlider.setMinimum(0);
        playbackSlider.setMaximum(maxval);
        playbackSlider.setValue(maxval);
        playbackSlider.setMajorTickSpacing(steps);
        playbackSlider.setMinorTickSpacing(steps / 10);
        playbackSlider.setPaintTicks(true);
        playbackSlider.setPaintLabels(true);

        double factor = maxTime / (double) maxval;
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = 0; i <= maxval; i += steps) {
            double value = i * factor;
            labelTable.put(i, new JLabel(String.format("%.1fc", value)));
        }
        playbackSlider.setLabelTable(labelTable);
    }

    private void updateSimulation() {
        if (!paused) {
            bird.update();
            maxTime = bird.currentTime;
        }
        playbackSlider.setVisible(paused);
        launchButton.setEnabled(!paused && !bird.isLaunched && !bird.hitBoundary);
        grabFocus();
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
                        Координаты: X=%.1fm Y=%.1fm<br>
                        Путь=%.1fm, Перемещение=%.1fm<br>
                        Макс.высота=%.1fm, Макс.дальность=%.1fm<br><br><br>
                        Время полета %.3fc<br>
                        </html>""",
                startSpeed,
                startAngle,
                bird.physics.getVelocity(),
                bird.physics.getVelocityAngleDeg(),
                bird.physics.position.x, bird.physics.position.y,
                bird.physics.totalPathLength,
                bird.physics.totalDistance,
                bird.physics.maxHeight,
                bird.physics.maxDistance,
                bird.currentTime
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
            g.drawString("нажмите 'сброс' для перезапуска", getWidth() / 2, getHeight() / 2 + 50);
        }

        if (paused) {
            g.setColor(Color.BLUE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("пауза", getWidth() / 2, getHeight() / 2);
        }
    }

    // Методы KeyListener
    public void onKeyPressed(KeyEvent e) {
        if (bird.hitBoundary) return;
        // System.out.println(e.paramString());
        if (e.getKeyChar() == 'p' && bird.isLaunched) {
            paused = !paused;
            if (paused) {
                updateTimeSlider();
            } else {
                bird.dropFutureHistory();
            }
        }
        if (e.getKeyChar() == 's' && !bird.isLaunched) {
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
