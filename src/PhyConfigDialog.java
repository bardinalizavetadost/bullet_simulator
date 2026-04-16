import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Класс PhyConfigDialog представляет диалоговое окно для настройки физических параметров.
 * Позволяет пользователю изменять характеристики пули, атмосферные условия и параметры запуска.
 */
public class PhyConfigDialog extends JDialog {
    /**
     * Конфигурация физических параметров
     */
    private final PhyConfig config;

    /**
     * Символы форматирования десятичных чисел
     */
    private final DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(Locale.ENGLISH);

    /**
     * Формат для отображения чисел с двумя знаками после запятой
     */
    private final DecimalFormat df2 = new DecimalFormat("0.00", dfs);

    /**
     * Формат для отображения чисел с тремя знаками после запятой
     */
    private final DecimalFormat df3 = new DecimalFormat("0.000", dfs);

    /**
     * Поле ввода температуры
     */
    private JTextField tempField;

    /**
     * Поле ввода давления
     */
    private JTextField pressureField;

    /**
     * Поле ввода массы пули
     */
    private JTextField massField;

    /**
     * Поле ввода калибра
     */
    private JTextField caliberField;

    /**
     * Поле ввода баллистического коэффициента
     */
    private JTextField bcField;

    /**
     * Поле ввода начальной энергии
     */
    private JTextField energyField;

    /**
     * Поле ввода угла запуска
     */
    private JTextField launchAngleField;

    /**
     * Поле отображения плотности воздуха
     */
    private JTextField airDensityField;

    /**
     * Поле отображения начальной скорости
     */
    private JTextField startSpeedField;

    /**
     * Выпадающий список выбора конфигурации пули
     */
    private JComboBox<String> bulletConfigComboBox;

    /**
     * Флаг обновления из выбора в выпадающем списке
     */
    private boolean updatingFromSelection = false;

    /**
     * Последняя выбранная конфигурация для сравнения
     */
    private PhyConfig.BulletConfig lastSelectedConfig = null;

    /**
     * Конструктор класса PhyConfigDialog
     *
     * @param parent Родительское окно
     * @param config Конфигурация физических параметров
     */
    public PhyConfigDialog(JFrame parent, PhyConfig config) {
        super(parent, "Конфигурация физики", true);
        this.config = config;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        loadConfigValues();
        setLocationRelativeTo(parent);

        // Add action listener for bullet configuration combo box
        bulletConfigComboBox.addActionListener(e -> {
            if (updatingFromSelection) return;

            String selected = (String) bulletConfigComboBox.getSelectedItem();
            if (selected == null || selected.equals("ручная настройка")) {
                config.selectedConfig = null;
                return;
            }

            // Find the matching configuration
            for (PhyConfig.BulletConfig bulletConfig : PhyConfig.BulletConfig.values()) {
                if (bulletConfig.name.equals(selected)) {
                    config.selectedConfig = bulletConfig;
                    updatingFromSelection = true;

                    // Update fields with the selected configuration
                    massField.setText(df3.format(bulletConfig.massG));
                    caliberField.setText(df2.format(bulletConfig.caliber));
                    bcField.setText(df3.format(bulletConfig.ballisticCoef));
                    energyField.setText(df2.format(bulletConfig.startEnergyJ));

                    // Update the config object
                    config.massG = bulletConfig.massG;
                    config.caliber = bulletConfig.caliber;
                    config.ballisticCoef = bulletConfig.ballisticCoef;
                    config.startEnergyJ = bulletConfig.startEnergyJ;

                    // Update calculated fields
                    updateCalculatedFields();
                    updatingFromSelection = false;

                    // Store the last selected configuration
                    lastSelectedConfig = bulletConfig;
                    return;
                }
            }
        });
    }

    /**
     * Инициализация компонентов диалогового окна
     * Создает и настраивает все элементы пользовательского интерфейса
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create bullet configuration panel
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        configPanel.setBorder(BorderFactory.createTitledBorder("выбор пули"));

        bulletConfigComboBox = new JComboBox<>();
        bulletConfigComboBox.addItem("ручная настройка");
        for (PhyConfig.BulletConfig config : PhyConfig.BulletConfig.values()) {
            bulletConfigComboBox.addItem(config.name);
        }

        configPanel.add(new JLabel("преднастроенный вариант:"));
        configPanel.add(bulletConfigComboBox);

        mainPanel.add(configPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createProjectilePanel());
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createEnergyPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createAtmosphericPanel());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        JButton saveButton = new JButton("Применить");
        saveButton.addActionListener(e -> updateConfig());
        buttonPanel.add(saveButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
    }

    /**
     * Создание панели атмосферных условий
     *
     * @return Панель с элементами управления атмосферными параметрами
     */
    private JPanel createAtmosphericPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Атмосферные условия"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Температура:"), gbc);

        gbc.gridx = 1;
        tempField = new JTextField(10);
        addDocumentListener(tempField, this::updateConfig);
        panel.add(tempField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("°C"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Давление:"), gbc);

        gbc.gridx = 1;
        pressureField = new JTextField(10);
        addDocumentListener(pressureField, this::updateConfig);
        panel.add(pressureField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Па"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Плотность воздуха:"), gbc);

        gbc.gridx = 1;
        airDensityField = new JTextField(10);
        airDensityField.setEditable(false);
        airDensityField.setBackground(new Color(240, 240, 240));
        panel.add(airDensityField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("кг/м³"), gbc);

        return panel;
    }

    /**
     * Создание панели параметров снаряда
     *
     * @return Панель с элементами управления параметрами пули
     */
    private JPanel createProjectilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Параметры снаряда"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Масса:"), gbc);

        gbc.gridx = 1;
        massField = new JTextField(10);
        addDocumentListener(massField, this::updateConfig);
        panel.add(massField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("г"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Калибр:"), gbc);

        gbc.gridx = 1;
        caliberField = new JTextField(10);
        addDocumentListener(caliberField, this::updateConfig);
        panel.add(caliberField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("мм"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Баллистический коэффициент:"), gbc);

        gbc.gridx = 1;
        bcField = new JTextField(10);
        addDocumentListener(bcField, this::updateConfig);
        panel.add(bcField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel(""), gbc);

        return panel;
    }

    /**
     * Создание панели энергии и скорости
     *
     * @return Панель с элементами управления энергией и скоростью
     */
    private JPanel createEnergyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Энергия и скорость"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Начальная энергия:"), gbc);

        gbc.gridx = 1;
        energyField = new JTextField(10);
        addDocumentListener(energyField, this::updateConfig);
        panel.add(energyField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Дж"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Начальная скорость:"), gbc);

        gbc.gridx = 1;
        startSpeedField = new JTextField(10);
        startSpeedField.setEditable(false);
        startSpeedField.setBackground(new Color(240, 240, 240));
        panel.add(startSpeedField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("м/с"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Угол запуска:"), gbc);

        gbc.gridx = 1;
        launchAngleField = new JTextField(10);
        addDocumentListener(launchAngleField, this::updateConfig);
        panel.add(launchAngleField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("°"), gbc);

        return panel;
    }

    /**
     * Загрузка значений конфигурации в поля ввода
     * Устанавливает текущие значения параметров в соответствующие поля
     */
    private void loadConfigValues() {
        tempField.setText(df2.format(config.temperatureC));
        pressureField.setText(df2.format(config.pressurePa));
        massField.setText(df3.format(config.massG));
        caliberField.setText(df2.format(config.caliber));
        bcField.setText(df3.format(config.ballisticCoef));
        energyField.setText(df2.format(config.startEnergyJ));
        launchAngleField.setText(df2.format(config.launchAngleDeg));

        // Set the appropriate item in the combo box based on current config
        if (lastSelectedConfig != null &&
                config.massG == lastSelectedConfig.massG &&
                config.caliber == lastSelectedConfig.caliber &&
                config.ballisticCoef == lastSelectedConfig.ballisticCoef &&
                config.startEnergyJ == lastSelectedConfig.startEnergyJ) {
            bulletConfigComboBox.setSelectedItem(lastSelectedConfig.name);
        } else {
            bulletConfigComboBox.setSelectedItem("ручная настройка");
        }

        updateCalculatedFields();
    }

    /**
     * Обновление конфигурации из значений полей ввода
     * Считывает значения из полей и обновляет объект конфигурации
     */
    private void updateConfig() {
        try {
            config.temperatureC = parseDouble(tempField.getText());
            config.pressurePa = parseDouble(pressureField.getText());
            config.massG = parseDouble(massField.getText());
            config.caliber = parseDouble(caliberField.getText());
            config.ballisticCoef = parseDouble(bcField.getText());
            config.startEnergyJ = parseDouble(energyField.getText());
            config.launchAngleDeg = parseDouble(launchAngleField.getText());

            updateCalculatedFields();
        } catch (NumberFormatException ex) {
        }
    }

    /**
     * Обновление вычисляемых полей
     * Обновляет поля, которые рассчитываются на основе других параметров
     */
    private void updateCalculatedFields() {
        airDensityField.setText(df3.format(config.airDensityKgM3()));
        startSpeedField.setText(df2.format(config.startSpeedMS()));
    }

    /**
     * Парсинг строки в число
     * Преобразует строку в число с плавающей точкой
     *
     * @param text Строка для парсинга
     * @return Число с плавающей точкой
     */
    private double parseDouble(String text) {
        return Double.parseDouble(text.trim().replace(",", "."));
    }

    /**
     * Добавление слушателя изменений документа
     * Добавляет слушатель для отслеживания изменений в поле ввода
     *
     * @param field  Поле ввода
     * @param action Действие для выполнения при изменении
     */
    private void addDocumentListener(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }

            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            public void insertUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }
}