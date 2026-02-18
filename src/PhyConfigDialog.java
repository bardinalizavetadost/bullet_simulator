import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class PhyConfigDialog extends JDialog {
    private final PhyConfig config;
    private final DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
    private final DecimalFormat df2 = new DecimalFormat("0.00", dfs);
    private final DecimalFormat df3 = new DecimalFormat("0.000", dfs);

    private JTextField tempField;
    private JTextField pressureField;
    private JTextField massField;
    private JTextField caliberField;
    private JTextField bcField;
    private JTextField energyField;
    private JTextField launchAngleField;

    private JTextField airDensityField;
    private JTextField startSpeedField;

    public PhyConfigDialog(JFrame parent, PhyConfig config) {
        super(parent, "Конфигурация физики", true);
        this.config = config;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        loadConfigValues();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

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

    private void loadConfigValues() {
        tempField.setText(df2.format(config.temperatureC));
        pressureField.setText(df2.format(config.pressurePa));
        massField.setText(df3.format(config.massG));
        caliberField.setText(df2.format(config.caliber));
        bcField.setText(df3.format(config.ballisticCoef));
        energyField.setText(df2.format(config.startEnergyJ));
        launchAngleField.setText(df2.format(config.launchAngleDeg));

        updateCalculatedFields();
    }

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

    private void updateCalculatedFields() {
        airDensityField.setText(df3.format(config.airDensityKgM3()));
        startSpeedField.setText(df2.format(config.startSpeedMS()));
    }

    private double parseDouble(String text) {
        return Double.parseDouble(text.trim().replace(",", "."));
    }

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