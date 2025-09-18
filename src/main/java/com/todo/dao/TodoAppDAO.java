package com.todo.dao;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import com.todo.util.DatabaseConnection;
import com.todo.model.Todo;

public class TodoAppDAO {
    private Todo getTodoRow(ResultSet rs) throws SQLException{
        int id = rs.getInt("id");
        String title= rs.getString("title");
        String description =rs.getString("description");
        boolean completed= rs.getBoolean("completed");
        LocalDateTime created_at = rs.getTimestamp("created_at").toLocalDateTime();
        LocalDateTime updated_at = rs.getTimestamp("updated_at").toLocalDateTime();

        Todo todo=new Todo(id,title,description,completed,created_at,updated_at);
        return todo;

    }
    public List<Todo> getAllTodos() throws SQLException {
        // Implement database retrieval logic here
        List<Todo> todos=new ArrayList<>();

        try (Connection conn= DatabaseConnection.getDBConnection();
        PreparedStatement stmt=conn.prepareStatement("SELECT * FROM todos ORDER BY created_at DESC");
        ResultSet res=stmt.executeQuery();)
        {
            while(res.next())
            {
                todos.add(getTodoRow(res));
            }
        } catch(SQLException e) {
            System.err.println("Error fetching todos: "+e.getMessage());
            throw e; // rethrow the exception after logging

        }
        return todos;
    }

}