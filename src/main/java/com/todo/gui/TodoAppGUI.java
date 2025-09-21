package com.todo.gui;
import com.todo.model.Todo;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.todo.dao.TodoAppDAO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class TodoAppGUI extends JFrame {
    private TodoAppDAO todoDAO;
    private JTable todoTable;
    private DefaultTableModel tableModel;
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JCheckBox completedCheckbox;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JComboBox<String> filterComboBox;

    public TodoAppGUI(){
        this.todoDAO = new TodoAppDAO();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadTodos();
    }

    private void initializeComponents(){
        setTitle("Todo Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        String[] columnNames = {"ID", "Title", "Description", "Completed", "Created At", "Updated At"};
        tableModel = new DefaultTableModel(columnNames, 0){
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };

        todoTable = new JTable(tableModel);
        todoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        todoTable.getSelectionModel().addListSelectionListener(
             (e) -> {
                if(!e.getValueIsAdjusting()){
                    // loadSlectedtodo();
                    
                }
             }
        );

        titleField = new JTextField(20);
        descriptionArea = new JTextArea(3,20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        completedCheckbox = new JCheckBox("Completed");
        addButton = new JButton("Add Todo");
        updateButton = new JButton("Update Todo");
        deleteButton = new JButton("Delete Todo");
        refreshButton = new JButton("Refresh Todo");

        String[] filterOptions = {"All", "Completed", "Pending"};
        filterComboBox = new JComboBox<>(filterOptions);
        filterComboBox.addActionListener((e) -> {
            //filterTodos()
        });
    }
    private void setupLayout(){
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Title"),gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(titleField,gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Description"),gbc);
        gbc.gridx = 1;
        // gbc.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(new JScrollPane(descriptionArea),gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        inputPanel.add(completedCheckbox,gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter"));
        filterPanel.add(filterComboBox);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(inputPanel,BorderLayout.CENTER);
        northPanel.add(buttonPanel,BorderLayout.SOUTH);
        northPanel.add(filterPanel,BorderLayout.NORTH);

        add(northPanel,BorderLayout.NORTH);

        add(new JScrollPane(todoTable),BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Select a todo to edit or delete"));
        add(statusPanel,BorderLayout.SOUTH);
    }

    private void setupEventListeners(){
        addButton.addActionListener((e) -> {addTodo();});
        updateButton.addActionListener((e) -> {updateTodo();});
        deleteButton.addActionListener((e) -> {deleteTodo();});
        refreshButton.addActionListener((e) -> {refreshTodo();});
    }

    private void addTodo(){
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean completed = completedCheckbox.isSelected();

        
        try{
            Todo todo = new Todo(title,description);
            todo.setCompleted(completed);
            todoDAO.createtodo(todo);
            JOptionPane.showMessageDialog(this, "Todo added successfully","Success", JOptionPane.INFORMATION_MESSAGE);
            loadTodos();

        }catch(SQLException e){
            JOptionPane.showMessageDialog(this, "Error adding todo","Failure",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTodo(){
        int row = todoTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete","Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,"Are you sure you want to delete this todo?","Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) todoTable.getValueAt(row, 0);
            try {
                boolean deleted = todoDAO.deleteTodo(id);
                if (deleted) {
                    JOptionPane.showMessageDialog(this,"Todo deleted successfully","Success", JOptionPane.INFORMATION_MESSAGE);
                    loadTodos(); // refresh table
                } else {
                    JOptionPane.showMessageDialog(this,"Failed to delete todo","Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,"Error deleting todo: " + e.getMessage(),"Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void updateTodo(){
        int row = todoTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a todo to update", "Validation Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id = (int) todoTable.getValueAt(row, 0);
        try {
            Todo todo = todoDAO.getTodoById(id);
            if (todo != null) {
                todo.setTitle(title);
                todo.setDescription(descriptionArea.getText().trim());
                todo.setCompleted(completedCheckbox.isSelected());
                if (todoDAO.updateTodo(todo)) {
                    JOptionPane.showMessageDialog(this, "Todo updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadTodos();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update todo", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
        } 
        catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating todo: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void filterTodos(){
        String selected=(String)filterComboBox.getSelectedItem();
        try{
            List<Todo> todos=todoDAO.getAllTodos();
            if(selected.equals("Completed")){
                todos.removeIf(t->!t.isCompleted());
            }
            else if(selected.equals("Pending")){
                todos.removeIf(Todo::isCompleted);
            }
            updateTable(todos);
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(this, "Error fetching todos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void refreshTodo(){

    }

    private void loadTodos(){

        try{
            List<Todo> todos = todoDAO.getAllTodos();
            updateTable(todos);
        }catch(SQLException e){
            JOptionPane.showMessageDialog(this, "Error loading todos : "+e.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE);
        }

    }

    private void updateTable(List<Todo> todos){
        tableModel.setRowCount(0);
        for(Todo t : todos){
            Object[] row = {t.getId(),t.getTitle(),t.getDescription(),t.isCompleted(),t.getCreated_at(),t.getUpdated_at()};
            tableModel.addRow(row);
        }
    }
}
