package liza;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Диалоговое окно для отображения таблицы траектории полета
 * Показывает данные симуляции с интервалом в 100 метров
 */
public class TrajectoryTableDialog extends JDialog {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final List<BirdState> history;
    private final PhyConfig config;
    private final TrajectoryDrawer trajectoryDrawer;

    /**
     * Конструктор диалога таблицы траектории
     *
     * @param parent    Родительское окно
     * @param history   История состояний полета
     * @param config    Конфигурация физических параметров
     */
    public TrajectoryTableDialog(JFrame parent, List<BirdState> history, PhyConfig config) {
        super(parent, "Таблица траектории", false);
        this.history = history;
        this.config = config;
        this.trajectoryDrawer = new TrajectoryDrawer();
        
        // Инициализация таблицы
        String[] columnNames = {"Время, с", "Скорость, м/с", "Дистанция, м", "Высота, м", "Δ по вертикали, м"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        
        // Настройка таблицы
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Добавление скролла
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        // Настройка окна
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
        updateTable();
    }

    /**
     * Обновление таблицы данными из истории полета
     * Данные отображаются с интервалом в 100 метров
     */
    public void updateTable() {
        tableModel.setRowCount(0);
        if (history.isEmpty()) {
            return;
        }

        // Проходим по истории с интервалом в 100 метров
        double lastDistance = -100; // Начинаем с -100, чтобы первая точка была включена
        for (BirdState state : history) {
            double distance = state.position().x;
            
            // Проверяем, прошло ли 100 метров с последней записи
            if (distance >= lastDistance + 100) {
                // Находим высоту на начальной траектории для этой дистанции
                double baseHeight = config.position0Y + Math.tan(Math.toRadians(config.launchAngleDeg)) * distance;
                double verticalDelta = state.position().y - baseHeight;
                
                // Добавляем строку в таблицу
                Object[] row = {
                    String.format("%.3f", state.time()),
                    String.format("%.1f", state.velocity().len()),
                    String.format("%.1f", distance),
                    String.format("%.1f", state.position().y),
                    String.format("%.1f", verticalDelta)
                };
                tableModel.addRow(row);
                
                lastDistance = distance;
            }
        }
    }

}