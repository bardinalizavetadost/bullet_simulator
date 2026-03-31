import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.Locale;

public class MyPanel extends JPanel {
    public static final double PIXELS_IN_METER = 1;
    public static final double PIXELS_IN_METERPERSECOND = 1;
    public static final int PADDING_PX = 30;
    
    private static MyPanel instance;
    
    private final JSlider playbackSlider;
    private JScrollBar horizontalScrollbar;

    JLabel infoLabel;

    boolean showMouseAngle = false;

    TrajectoryDrawer trajectory = new TrajectoryDrawer();

    boolean useMouse = true;
    int lastHorizontalScroll = 0;
    boolean paused = false;
    double maxTime = 0;
    
    // Viewport scrolling
    private double scrollOffsetX = 0; // Left edge of viewport in meters
    private boolean autoScrollEnabled = true;

    PhyConfig mainConfig = new PhyConfig();
    Bird bird = new Bird(mainConfig);

    public MyPanel() {
        instance = this;
        setBackground(Color.CYAN);
        setLayout(null);

        // Создаем панель ввода
        JPanel inputPanel = new JPanel();

        inputPanel.setBackground(new Color(240, 240, 240));
        inputPanel.setBounds(100, 10, 350, 120);
        inputPanel.setLayout(new GridLayout(4, 2, 5, 5));

        JButton resetButton = new JButton("Сброс");
        resetButton.addActionListener(e -> resetSimulation());
        inputPanel.add(resetButton);

        JButton settingsButton = new JButton("Настройки");
        settingsButton.addActionListener(e -> openConfig());
        inputPanel.add(settingsButton);

        inputPanel.add(new JLabel("Наведение"));
        JToggleButton useMouseBtn = new JToggleButton("Мышью");
        useMouseBtn.setSelected(useMouse);
        useMouseBtn.addItemListener((e) -> {
            useMouse = e.getStateChange() == ItemEvent.SELECTED;
            useMouseBtn.setText(useMouse ? "Мышью" : "Параметрами");
        });
        inputPanel.add(useMouseBtn);

        inputPanel.add(new JLabel("Авто-прокрутка"));
        JToggleButton autoScrollBtn = new JToggleButton("Вкл");
        autoScrollBtn.setSelected(autoScrollEnabled);
        autoScrollBtn.addItemListener((e) -> {
            autoScrollEnabled = e.getStateChange() == ItemEvent.SELECTED;
            autoScrollBtn.setText(autoScrollEnabled ? "Вкл" : "Выкл");
        });
        inputPanel.add(autoScrollBtn);

        JLabel timeLabel = new JLabel("Замедление времени x1");
        inputPanel.add(timeLabel);
        JSlider timeSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, (int) Bird.slowTimeBy);
        timeSlider.addChangeListener(e -> {
            Bird.slowTimeBy = timeSlider.getValue();
            timeLabel.setText("Замедление времени x%d".formatted((int) Bird.slowTimeBy));
        });
        inputPanel.add(timeSlider);

        inputPanel.setLayout(new GridLayout(0, 2));
        add(inputPanel);

        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 1);
        playbackSlider.setBounds(50, 150, 400, 50);
        add(playbackSlider);
        playbackSlider.addChangeListener(e -> {
            if (paused) {
                bird.resetToTime(playbackSlider.getValue() / (double) 100 * maxTime);
            }
        });

        // Панель информации
        infoLabel = new JLabel("");
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        infoLabel.setBounds(450, 80, 650, 200);
        add(infoLabel);
        
        horizontalScrollbar = new JScrollBar(JScrollBar.HORIZONTAL);
        horizontalScrollbar.setVisible(true);
        horizontalScrollbar.setValues(0, 100, 0, 1000); // initial values
        horizontalScrollbar.addAdjustmentListener(e -> {
            if (e.getValue() != lastHorizontalScroll) {
//                autoScrollEnabled = false;
                lastHorizontalScroll = e.getValue();
            }
            scrollOffsetX = e.getValue() / PIXELS_IN_METER;
            repaint();
        });
        add(horizontalScrollbar);

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        if (bird.hitBoundary) return false;
                        if (e.getKeyChar() == 'p' && bird.isLaunched) {
                            paused = !paused;
                            if (paused) {
                                updateTimeSlider();
                            } else {
                                bird.dropFutureHistory();
                            }
                        }
                        if (e.getKeyChar() == 'g' && paused) {
                            java.util.List<Point> yDrag = trajectory.getPredictedTrajectory(mainConfig);
                            java.util.List<Point> yNoDrag = trajectory.getPredictedTrajectory(mainConfig.withoutDrag());
                            DeltaPlotDialog plotDialog = new DeltaPlotDialog(null, yDrag, yNoDrag);
                            plotDialog.setVisible(true);
                        }
                        if (e.getKeyChar() == 's' && !bird.isLaunched) {
                            launchBird();
                        }
                    }
                    return false;
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

        // Add ComponentListener to handle resize events
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionUIElements();
            }
        });

        // Таймер для обновления физики
        Timer timer = new Timer(10, e -> updateSimulation());
        timer.start();

        Timer timerst = new Timer(100, e -> {
            resetSimulation();
        });
        timerst.setRepeats(false);
        timerst.start();
    }
    
    private void repositionUIElements() {
        // Get current panel dimensions
        int width = getWidth();
        int height = getHeight();
        int physicsHeight = getPhysicsHeight();
        
        // Reposition input panel (keep it in top-left area, but scale proportionally)
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel && comp.getBackground().equals(new Color(240, 240, 240))) {
                // Input panel - keep near top-left
                comp.setBounds(Math.min(100, width / 8), 10, 350, 120);
            } else if (comp == infoLabel) {
                // Info label - position it on the right side
                int labelWidth = Math.min(600, width - 470);
                comp.setBounds(Math.max(450, width / 2 + 50), 10, labelWidth, 200);
            } else if (comp == playbackSlider) {
                // Playback slider - center it horizontally
                int sliderWidth = Math.min(400, width - 100);
                comp.setBounds(50, 150, sliderWidth, 50);
            } else if (comp == horizontalScrollbar) {
                // Position scrollbar at bottom, just below the physics area
                int scrollbarHeight = 16;
                int scrollbarY = physicsHeight + PADDING_PX;
                horizontalScrollbar.setBounds(0, scrollbarY, getPhysicsWidth() + PADDING_PX, scrollbarHeight);
                
                // Update visible amount
                int visibleAmount = getPhysicsWidth();
                horizontalScrollbar.setVisibleAmount(visibleAmount);
            }
        }
        
        repaint();
    }

    private void launchBird() {
        if (bird.isLaunched || bird.hitBoundary) return;

        bird.launch(mainConfig);
        showMouseAngle = false;
        paused = false;
//        autoScrollEnabled = true; // Re-enable auto-scroll on launch
    }

    private void openConfig() {
        PhyConfigDialog dialog = new PhyConfigDialog(null, mainConfig);
        dialog.setVisible(true);
    }

    private void resetSimulation() {
        bird.reset();
        showMouseAngle = false;
        paused = false;
        scrollOffsetX = 0; // Reset scroll position
//        autoScrollEnabled = true; // Re-enable auto-scroll
        if (horizontalScrollbar != null) {
            horizontalScrollbar.setValue(0);
            horizontalScrollbar.setMaximum(1000);
        }
        repaint();
        bird.preLanuch(mainConfig);
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
            
            // Auto-scroll logic: continuously keep bullet in view
            if (bird.isLaunched && !bird.hitBoundary && autoScrollEnabled) {
                double viewportWidth = getMaxWidthMeters();
                double birdX = bird.physics.position.x;
                
                // Keep bullet centered when it moves beyond 50% of viewport
                if (birdX > scrollOffsetX + viewportWidth * 0.5) {
                    scrollOffsetX = birdX - viewportWidth * 0.5;
                    scrollOffsetX = Math.max(0, scrollOffsetX);
                }
            }
            
            // Always update scrollbar to extend maximum as bullet travels
            if (bird.isLaunched) {
                updateHorizontalScrollbar();
            }
        } else {
            // In pause mode, scroll to keep bird visible
            if (bird.isLaunched) {
                scrollToKeepBirdVisible();
            }
        }
        playbackSlider.setVisible(paused);
//        grabFocus();
        updateInfo();
        repaint();
    }
    
    private void scrollToKeepBirdVisible() {
        double viewportWidth = getMaxWidthMeters();
        double birdX = bird.physics.position.x;
        
        // If bird is outside visible area, scroll to it
        if (birdX < scrollOffsetX) {
            scrollOffsetX = Math.max(0, birdX - viewportWidth * 0.2);
            updateHorizontalScrollbar();
        } else if (birdX > scrollOffsetX + viewportWidth) {
            scrollOffsetX = birdX - viewportWidth * 0.8;
            updateHorizontalScrollbar();
        }
    }
    
    private void updateHorizontalScrollbar() {
        if (horizontalScrollbar != null) {
            int scrollValuePx = (int) (scrollOffsetX * PIXELS_IN_METER);
            
            // Update scrollbar maximum with margin ahead of bullet
            double viewportWidth = getMaxWidthMeters();
            double margin = viewportWidth * 2; // Add 2 viewports ahead as margin
            double maxDistance = bird.physics.position.x + margin;
            int maxScrollPx = (int) (maxDistance * PIXELS_IN_METER);
            
            // Set scrollbar values (value, visible amount, min, max)
            int visibleAmountPx = (int) (viewportWidth * PIXELS_IN_METER);
            horizontalScrollbar.setValues(scrollValuePx, visibleAmountPx, 0, maxScrollPx + visibleAmountPx);
        }
    }

    private void updateInfo() {
        bird.velocity0.x = mainConfig.startSpeedMS() * Math.cos(Math.toRadians(mainConfig.launchAngleDeg));
        bird.velocity0.y = mainConfig.startSpeedMS() * Math.sin(Math.toRadians(mainConfig.launchAngleDeg));

        infoLabel.setText(String.format(
                """
                        <html>
                        Параметры:<br>
                        Пуля: калибр=%.2fмм масса=%.2fг БК=%.3f<br>
                        Энергия выстрела: %.1fДж<br>
                        Начальные: скорость=%.1fm/s, угол скорости=%.1f°<br>
                        Текущие: скорость=%.1fm/s, угол скорости=%.1f°<br>
                        Координаты: X=%.1fm Y=%.1fm<br>
                        Путь=%.1fm, Перемещение=%.1fm<br>
                        Макс.высота=%.1fm, Макс.дальность=%.1fm<br><br><br>
                        Время полета %.3fc<br>
                        </html>""",
                mainConfig.caliber, mainConfig.massG, mainConfig.ballisticCoef,
                mainConfig.startEnergyJ,
                mainConfig.startSpeedMS(),
                mainConfig.launchAngleDeg,
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
            trajectory.predictAndDraw(g, 0, mainConfig);
        }

        if (!bird.hitBoundary) {
            trajectory.predictAndDraw(g, 1, mainConfig.withoutDrag());
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

    // Метод для вычисления угла по координатам клика мыши
    private double calculateAngleFromClick(int mouseX, int mouseY) {
        double dx = screenXtoXMeters(mouseX) - mainConfig.position0X;
        double dy = screenYtoYMeters(mouseY) - mainConfig.position0Y;

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
            double startAngle = calculateAngleFromClick(mouseX, mouseY);
            double startSpeed = Math.sqrt(
                    Math.pow(screenYtoYMeters(mouseY) - mainConfig.position0Y, 2) + Math.pow(screenXtoXMeters(mouseX) - mainConfig.position0X, 2))
                    / Math.sqrt(getPhysicsHeight() * getPhysicsHeight() + getPhysicsWidth() * getPhysicsWidth()) * 900;

            mainConfig.launchAngleDeg = startAngle;
            mainConfig.setEnergyFromSpeed(startSpeed);
            repaint();
        }
    }

    public double screenXtoXMeters(int screenXpx) {
        return ((screenXpx - PADDING_PX) / PIXELS_IN_METER) + scrollOffsetX;
    }

    public double screenYtoYMeters(int screenYpx) {
        return (getPhysicsHeight() - screenYpx) / PIXELS_IN_METER;
    }
    
    // Scroll offset getter
    public double getScrollOffsetX() {
        return scrollOffsetX;
    }
    
    public static double getStaticScrollOffsetX() {
        return instance != null ? instance.getScrollOffsetX() : 0;
    }
    
    // Dynamic dimension getters
    public int getPhysicsHeight() {
        return getHeight() - 2 * PADDING_PX;
    }
    
    public int getPhysicsWidth() {
        return getWidth() - PADDING_PX;
    }
    
    public double getMaxHeightMeters() {
        return getPhysicsHeight() / PIXELS_IN_METER;
    }
    
    public double getMaxWidthMeters() {
        return getPhysicsWidth() / PIXELS_IN_METER;
    }
    
    // Static accessors for other classes
    public static MyPanel getInstance() {
        return instance;
    }
    
    public static int getStaticPhysicsHeight() {
        return instance != null ? instance.getPhysicsHeight() : 540;
    }
    
    public static int getStaticPhysicsWidth() {
        return instance != null ? instance.getPhysicsWidth() : 770;
    }
    
    public static double getStaticMaxHeightMeters() {
        return instance != null ? instance.getMaxHeightMeters() : 540;
    }
    
    public static double getStaticMaxWidthMeters() {
        return instance != null ? instance.getMaxWidthMeters() : 770;
    }
}
