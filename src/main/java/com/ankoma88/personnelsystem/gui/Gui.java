package com.ankoma88.personnelsystem.gui;

import com.ankoma88.personnelsystem.client.Worker;
import com.ankoma88.personnelsystem.model.Command;
import com.ankoma88.personnelsystem.model.Employee;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Gui extends JFrame {
    private static final Logger log = Logger.getLogger(Gui.class.getName());
    private static final String MAIN_FRAME_TITLE = "Personnel System Client",
                                TABLE_TITLE = "Subordinates",
                                COLUMN_ID = "Id",
                                COLUMN_FULL_NAME = "Full Name",
                                COLUMN_DEPARTMENT = "Department",
                                COLUMN_SUPERVISOR = "Supervisor",
                                COLUMN_COMMENT = "Comment",
                                TREE_ROOT = "Company",
                                TABS_INFO = "Employee Info",
                                BTN_CLOSE = "Close",
                                BTN_DELETE = "Delete employee",
                                BTN_UPDATE = "Update employee",
                                BTN_CREATE = "Create new subordinate",
                                BTN_SAVE = "Save new employee";

    private Worker worker = new Worker();

    private Map<String, Employee> nameSupervisorMap, nameSubordinateMap;
    private String selectedSupervisor;

    private JFrame mainFrame;

    private JPanel topPanel, tablePanel, statusPanel;
    private JTree treePanel;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;

    private DefaultTableModel tableModel;
    private JTable table;
    private JScrollPane tableScrollPane, treePanelScrollPane;
    private JTabbedPane tabbedPane;

    private JButton closeBtn, deleteBtn, updateBtn, createBtn, saveBtn;

    private JTextField fullNameField, departmentField, supervisorField, commentField;

    private JLabel statusLabel;


    public Gui() {

        nameSupervisorMap = new HashMap<String, Employee>();
        nameSubordinateMap = new HashMap<String, Employee>();

        topPanel = new JPanel();
        tablePanel = new JPanel();
        statusPanel = new JPanel();
        treePanel = new JTree();
        root = new DefaultMutableTreeNode(TREE_ROOT);
        table = new JTable();
        tableScrollPane = new JScrollPane();
        treePanelScrollPane = new JScrollPane();
        tabbedPane = new JTabbedPane();

        mainFrame = new JFrame(MAIN_FRAME_TITLE);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        createTreeModel();
        setupTreePanel();

        createTableModel();
        setupTablePanel();

        setupTopPanel();
        setupBottomPanel();
        addPanelsToMainFrame();

        mainFrame.pack();
        mainFrame.setLocationByPlatform(true);
        mainFrame.setVisible(true);


    }

    private void setupTreePanelBehavior() {
        treePanel.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {

                //Fill employees tree
                fillEmployeesTree();

                //Detect selected node and respective employee
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treePanel.getLastSelectedPathComponent();

                if (selectedNode == null) return;

                //Clear subordinates table
                tableModel.setRowCount(0);

                //Detect respective employee
                String selectedEmployeeName = selectedNode.getUserObject().toString();

                //Open employee tab and return Employee object
                Employee selectedEmployee = openInfoTab(selectedEmployeeName);

                //Fill subordinates table if needed
                if (isSupervisor(selectedEmployeeName))
                    fillSubordinatesTable(tableModel, selectedEmployee.getId(), selectedEmployeeName, selectedNode);


            }
        });
    }

    private void fillEmployeesTree() {
        //Get supervisors from database, put them in their map, add them to root
        java.util.List<Employee> supervisors = null;
        try{
            supervisors = worker.getSupervisors();
            for (Employee e : supervisors) {
                nameSupervisorMap.put(e.getFullName(), e);
                root.add(new DefaultMutableTreeNode(e.getFullName()));
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void fillSubordinatesTable(DefaultTableModel tableModel, int key, String supervisorName, DefaultMutableTreeNode selectedParent) {
        //Clearing children (subordinates) of selected node (supervisor)
        selectedParent.removeAllChildren();
        treeModel.reload(selectedParent);

        //Getting subordinates of selected supervisor from server database
        java.util.List<Employee> subordinates = worker.getSubordinates(key);
        for (Employee e : subordinates) {

            //Inserting them to table
            Object[] row = {e.getId(), e.getFullName(), e.getDepartment(), supervisorName, e.getComment()};
            tableModel.addRow(row);

            //Inserting them to tree under supervisor
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(e.getFullName());
            treeModel.insertNodeInto(childNode, selectedParent, selectedParent.getChildCount());

            //Remember subordinate
            nameSubordinateMap.put(e.getFullName(), e);
        }
    }

    private void setupTreePanel() {
        treePanel.setModel(treeModel);
        setupTreePanelGui();
        setupTreePanelBehavior();
    }

    private void setupTablePanel() {
        table.setModel(tableModel);
        setupTablePanelGui();
        setupTablePanelBehavior();
    }

    private void createTreeModel() {
        treeModel = new DefaultTreeModel(root);
        treeModel.addTreeModelListener(new EmployeeTreeModelListener());
    }

    private void createTableModel() {
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn(COLUMN_ID);
        tableModel.addColumn(COLUMN_FULL_NAME);
        tableModel.addColumn(COLUMN_DEPARTMENT);
        tableModel.addColumn(COLUMN_SUPERVISOR);
        tableModel.addColumn(COLUMN_COMMENT);
    }

    private void setupTreePanelGui() {
        treePanel.setBorder(BorderFactory.createTitledBorder(TREE_ROOT));
        treePanel.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treePanel.setPreferredSize(new Dimension(300, 600));
        treePanelScrollPane.setViewportView(treePanel);
    }

    private void setupTablePanelBehavior() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JTable target = (JTable) e.getSource();
                final int row = target.getSelectedRow();
                final String selectedValue = (String) target.getValueAt(row, 1);
                Employee employee = nameSubordinateMap.get(selectedValue);
                createTab(employee, false);
            }
        });
    }

    private void setupTablePanelGui() {
        tableScrollPane.setViewportView(table);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        tablePanel.setBorder(BorderFactory.createTitledBorder(TABLE_TITLE));
        tablePanel.setLayout(new BorderLayout(5, 5));
    }

    private void setupBottomPanel() {
        statusPanel.setPreferredSize(new Dimension(mainFrame.getWidth(), 25));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusLabel = new JLabel("Status bar");
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(statusLabel);
    }

    private void setupTopPanel() {
        topPanel.setLayout(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder(TABS_INFO));
        topPanel.setPreferredSize(new Dimension(600, 200));
        topPanel.add(tabbedPane);
    }

    private void setupButtons() {
        updateBtn = new JButton(BTN_UPDATE);
        updateBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                updateEmployee();
            }
        });

        deleteBtn = new JButton(BTN_DELETE);
        deleteBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                deleteEmployee();
            }
        });

        createBtn = new JButton(BTN_CREATE);
        createBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                openSaveTab();
            }
        });

        saveBtn = new JButton(BTN_SAVE);
        saveBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                saveNewSubordinate();
            }
        });

        closeBtn = new JButton(BTN_CLOSE);
        closeBtn.setForeground(Color.BLUE);
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                closeTab();
            }
        });
    }

    private void updateEmployee() {
        String empName = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
        Employee oldEmployee;
        try {
            oldEmployee = nameSubordinateMap.get(empName);
        } catch (NullPointerException npe) {
            oldEmployee = nameSupervisorMap.get(empName);
        }

        selectedSupervisor = supervisorField.getText();

        oldEmployee.setFullName(fullNameField.getText());
        oldEmployee.setDepartment(departmentField.getText());
        oldEmployee.setComment(commentField.getText());
        int newSup = 0;
        try {
            newSup = nameSupervisorMap.get(selectedSupervisor).getId();
        } catch (NullPointerException npe) {
            log.info("Such supervisor doesn't exist!");
            statusLabel.setText("Such supervisor doesn't exist!");
            return;
        }
        oldEmployee.setSupervisor(newSup);

        Employee result = worker.updateEmployee(oldEmployee);
        log.info("updated employee with id " + result.getId());
        statusLabel.setText("updated employee with id " + result.getId());

        //remove nodes
        TreePath path = treePanel.getSelectionPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) (path.getLastPathComponent());
        treeModel.removeNodeFromParent(node);

        nameSubordinateMap.put(result.getFullName(), result);

        //inserting updated employee and closing tabs
        TreePath supervisorPath = findTreePath(selectedSupervisor);
        DefaultMutableTreeNode supervisorNode = (DefaultMutableTreeNode) supervisorPath.getLastPathComponent();
        treeModel.insertNodeInto(new DefaultMutableTreeNode(result.getFullName()), supervisorNode, supervisorNode.getChildCount());
        treePanel.setSelectionPath(new TreePath(supervisorNode.getPath()));
        tabbedPane.removeAll();
    }

    private void closeTab() {
        tabbedPane.remove(tabbedPane.getSelectedIndex());
    }

    private void saveNewSubordinate() {
        Employee newEmployee = new Employee();
        newEmployee.setFullName(fullNameField.getText());
        newEmployee.setDepartment(departmentField.getText());

        int supervisorId = nameSupervisorMap.get(selectedSupervisor).getId();

        newEmployee.setSupervisor(supervisorId);

        newEmployee.setComment(commentField.getText());
        Employee result = worker.create(newEmployee);
        log.info("created new employee with id " + result.getId());
        statusLabel.setText("created new employee with id " + result.getId());

        TreePath supervisorPath = findTreePath(selectedSupervisor);
        DefaultMutableTreeNode supervisorNode = (DefaultMutableTreeNode) supervisorPath.getLastPathComponent();
        treeModel.insertNodeInto(new DefaultMutableTreeNode(result.getFullName()), supervisorNode, supervisorNode.getChildCount());
        treePanel.setSelectionPath(new TreePath(supervisorNode.getPath()));
        tabbedPane.removeAll();
    }

    private void openSaveTab() {
        selectedSupervisor = fullNameField.getText();
        createTab(new Employee("enter full name", "set department", 0, "add comment"), true);
    }

    private void deleteEmployee() {
        String empName = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
        int key;
        try {
            key = nameSubordinateMap.get(empName).getId();
        } catch (NullPointerException npe) {
            key = nameSupervisorMap.get(empName).getId();
        }
        Command result = worker.deleteEmployee(key);
        log.info("deleting employee with id " + key + "... " + result);
        statusLabel.setText("deleting employee with id " + key + "... " + result);

        TreePath path = treePanel.getSelectionPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) (path.getLastPathComponent());
        treeModel.removeNodeFromParent(node);
        treePanel.setSelectionPath(new TreePath(root.getPath()));

        tabbedPane.removeAll();
    }

    private void createTab(Employee employee, boolean isCreateNewTab) {
        setupButtons();
        setupFields(employee);

        if (isCreateNewTab) {
            createSaveTab(employee);
        }
        else {
            if (isSupervisor(employee.getFullName())) {
                createSupervisorTab(employee);
            } else if (isSubordinate(employee.getFullName())) {
                createSubordinateTab(employee);
            }
        }
    }

    private void createSubordinateTab(Employee employee) {
        JPanel tab = setupTabGridBagGui(updateBtn, deleteBtn, null, closeBtn,
                fullNameField, departmentField, supervisorField, commentField);
        tabbedPane.addTab(employee.getFullName(), tab);
        tabbedPane.setSelectedComponent(tab);
    }


    private void createSupervisorTab(Employee employee) {
        JPanel tab = setupTabGridBagGui(null, null, createBtn, closeBtn,
                                        fullNameField, departmentField,
                                        supervisorField, commentField);
        tabbedPane.addTab(employee.getFullName(), tab);
        tabbedPane.setSelectedComponent(tab);

        fullNameField.setEditable(false);
        departmentField.setEditable(false);
        supervisorField.setEditable(false);
        commentField.setEditable(false);
    }

    private void createSaveTab(Employee employee) {
            JPanel tab = setupTabGridBagGui(null, null, saveBtn, closeBtn,
                    fullNameField, departmentField,
                    supervisorField, commentField);
            tabbedPane.addTab(employee.getFullName(), tab);
            tabbedPane.setSelectedComponent(tab);

        supervisorField.setEditable(false);
        supervisorField.setText(selectedSupervisor);
    }

    private void setupFields(Employee employee) {
        fullNameField = new JTextField(employee.getFullName());
        departmentField = new JTextField(employee.getDepartment());
        String supervisor = employee.getSupervisor().toString();
        supervisor = representSupervisor(supervisor);
        supervisorField = new JTextField(supervisor);
        commentField = new JTextField(employee.getComment());
    }

    private JPanel setupTabGridBagGui(JButton firstBtn,
                                      JButton secondBtn,
                                      JButton thirdBtn,
                                      JButton fourthBtn,
                                      JTextField fullNameField,
                                      JTextField departmentField,
                                      JTextField supervisorField,
                                      JTextField commentField) {
        JPanel tab = new JPanel();
        Border border = tab.getBorder();
        Border margin = new EmptyBorder(10, 10, 10, 10);
        tab.setBorder(new CompoundBorder(border, margin));

        GridBagLayout panelGridBagLayout = new GridBagLayout();
        panelGridBagLayout.columnWidths = new int[] { 86, 86, 0 };
        panelGridBagLayout.rowHeights = new int[] { 20, 20, 20, 20, 20, 0 };
        panelGridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        panelGridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        tab.setLayout(panelGridBagLayout);

        addLabelAndTextToGridbagLayout(COLUMN_FULL_NAME, 0, tab, fullNameField);
        addLabelAndTextToGridbagLayout(COLUMN_DEPARTMENT, 1, tab, departmentField);
        addLabelAndTextToGridbagLayout(COLUMN_SUPERVISOR, 2, tab, supervisorField);
        addLabelAndTextToGridbagLayout(COLUMN_COMMENT, 3, tab, commentField);

        if (firstBtn!=null) {
            addButtonsToGridbagLayout(0, tab, firstBtn);
        }
        if (secondBtn!=null) {
            addButtonsToGridbagLayout(1, tab, secondBtn);
        }
        if (thirdBtn!=null) {
            addButtonsToGridbagLayout(2, tab, thirdBtn);
        }
        if (fourthBtn!=null) {
            addButtonsToGridbagLayout(3, tab, fourthBtn);
        }

        return tab;
    }

    private void addButtonsToGridbagLayout(int yPos, Container tab, JButton button) {
        GridBagConstraints gridBagConstraintForBtn = new GridBagConstraints();
        gridBagConstraintForBtn.fill = GridBagConstraints.BOTH;
        gridBagConstraintForBtn.insets = new Insets(0, 0, 5, 5);
        gridBagConstraintForBtn.gridx = 2;
        gridBagConstraintForBtn.gridy = yPos;
        tab.add(button, gridBagConstraintForBtn);
    }

    private void addLabelAndTextToGridbagLayout(String labelText, int yPos, Container tab, JTextField textField) {
            JLabel fieldLabel = new JLabel(labelText);
            GridBagConstraints gridBagConstraintForLabel = new GridBagConstraints();
            gridBagConstraintForLabel.fill = GridBagConstraints.BOTH;
            gridBagConstraintForLabel.insets = new Insets(0, 0, 5, 5);
            gridBagConstraintForLabel.gridx = 0;
            gridBagConstraintForLabel.gridy = yPos;
            tab.add(fieldLabel, gridBagConstraintForLabel);


            GridBagConstraints gridBagConstraintForTextField = new GridBagConstraints();
            gridBagConstraintForTextField.fill = GridBagConstraints.BOTH;
            gridBagConstraintForTextField.insets = new Insets(0, 0, 5, 5);
            gridBagConstraintForTextField.gridx = 1;
            gridBagConstraintForTextField.gridy = yPos;
            tab.add(textField, gridBagConstraintForTextField);
            textField.setColumns(10);
    }

    private boolean isSupervisor(String selectedEmployeeName) {
        return nameSupervisorMap.containsKey(selectedEmployeeName);
    }

    private boolean isSubordinate(String selectedEmployeeName) {
        return nameSubordinateMap.containsKey(selectedEmployeeName);
    }

    private Employee openInfoTab(String selectedEmployeeName) {
        Employee selectedEmployee = null;
        if (isSupervisor(selectedEmployeeName)) {
            selectedEmployee = nameSupervisorMap.get(selectedEmployeeName);
            createTab(selectedEmployee, false);
        } else if (isSubordinate(selectedEmployeeName)) {
            selectedEmployee = nameSubordinateMap.get(selectedEmployeeName);
            createTab(selectedEmployee, false);
        }
        return selectedEmployee;
    }

    //convert supervisor's id into his name
    private String representSupervisor(String supervisor) {
        if (supervisor.equals("0")) {
            supervisor = "";
        } else {
            Collection<Employee> c = nameSupervisorMap.values();
            for (Employee emp : c) {
                if (supervisor.equals(String.valueOf(emp.getId()))) {
                    supervisor = emp.getFullName();
                }
            }
        }
        return supervisor;
    }

    private void addPanelsToMainFrame() {
        mainFrame.getContentPane().add(topPanel, BorderLayout.NORTH);
        mainFrame.getContentPane().add(treePanelScrollPane, BorderLayout.WEST);
        mainFrame.getContentPane().add(tableScrollPane, BorderLayout.CENTER);
        mainFrame.getContentPane().add(statusPanel, BorderLayout.SOUTH);
    }

    private TreePath findTreePath(String s) {
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (node.toString().equalsIgnoreCase(s)) {
                return new TreePath(node.getPath());
            }
        }
        return null;
    }

    private class EmployeeTreeModelListener implements TreeModelListener {

        @Override
        public void treeNodesChanged(TreeModelEvent e) {
            log.info("treeNodesChanged");
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            log.info("treeNodesInserted");
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
            log.info("treeNodesRemoved");
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            log.info("treeStructureChanged");
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Gui();
            }
        });
    }


}
