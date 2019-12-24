package main;

import javax.swing.*;

public class MainWindow {
    
    
    public static final String TITLE = "Bulk Store Database";
    private JPanel rootPanel;
    private JButton warehousesButton;
    private JButton itemsButton;
    private JButton clientsButton;
    private JButton suppliesButton;
    private JButton salesButton;
    private JTable table1;
    private JTextField nameField;
    private JComboBox activationComboBox;
    private JButton loadGroupButton;
    private JButton createGroupButton;
    private JTextField widthField;
    private JTextField heightField;
    private JTextField layersField;
    private JTextField sizeField;
    private JTextField runsField;
    private JTextField maxDensityField;
    private JTextField distributionField;
    private JButton deleteGroupButton;
    private JButton refreshButton;
    
    private void initComponents() {
        warehousesButton.addActionListener(ev -> {
            JFrame frame = new JFrame(TITLE);
            WarehousesWindow gui = new WarehousesWindow();
            frame.setContentPane(gui.getRootPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
        itemsButton.addActionListener(ev -> {
            JFrame frame = new JFrame(TITLE);
            ItemsWindow gui = new ItemsWindow();
            frame.setContentPane(gui.getRootPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
        clientsButton.addActionListener(ev -> {
            JFrame frame = new JFrame(TITLE);
            ClientsWindow gui = new ClientsWindow();
            frame.setContentPane(gui.getRootPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
        suppliesButton.addActionListener(ev -> {
            JFrame frame = new JFrame(TITLE);
            SuppliesWindow gui = new SuppliesWindow();
            frame.setContentPane(gui.getRootPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
        salesButton.addActionListener(ev -> {
            JFrame frame = new JFrame(TITLE);
            SalesWindow gui = new SalesWindow();
            frame.setContentPane(gui.getRootPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
    }
    
    
    public MainWindow() {
        initComponents();
    }
    
    
    public static void main(String[] args) {
        Utils.createNewDatabase();
        JFrame frame = new JFrame(TITLE);
        MainWindow gui = new MainWindow();
        frame.setContentPane(gui.rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
