package com.todo;
import com.todo.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
public class Main {
    public static void main(String[] args) {
        DatabaseConnection db_Connection = new DatabaseConnection();
        try{
            Connection cn = db_Connection.getDBConnection();
            System.out.println("Database connection is successful");
        }
        catch(SQLException e){
            System.out.println("The databse connection has failed");
        }
    }
}