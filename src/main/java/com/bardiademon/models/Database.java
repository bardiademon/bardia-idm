package com.bardiademon.models;

import com.bardiademon.bardiademon.Log;
import com.bardiademon.bardiademon.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database
{
    private static final String URL_CONNECTION = "jdbc:sqlite:" + Path.DB;
    private Connection connection;

    public Database ()
    {
        try
        {
            connection = DriverManager.getConnection (URL_CONNECTION);
        }
        catch (final SQLException e)
        {
            Log.N (e);
        }
    }

    public Connection getConnection ()
    {
        return connection;
    }

    public boolean connected ()
    {
        try
        {
            return (connection != null && !connection.isClosed ());
        }
        catch (final SQLException e)
        {
            Log.N (e);
            return false;
        }
    }
}
