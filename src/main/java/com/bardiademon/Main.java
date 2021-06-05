package com.bardiademon;

import com.bardiademon.bardiademon.Log;
import com.bardiademon.models.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public final class Main extends Application
{

    public static final Database DATABASE = new Database ();

    @Override
    public void start (final Stage primaryStage) throws Exception
    {
        final URL resource = (getClass ().getClassLoader ()).getResource ("main.fxml");
        if (resource != null)
        {
            final Parent root = FXMLLoader.load (resource);
            primaryStage.setTitle ("Hello World");
            primaryStage.setScene (new Scene (root));
            primaryStage.show ();
        }
        else System.out.println ("Resource is null.");
    }

    public static void main (final String[] args)
    {
        if (DATABASE.connected ())
            launch (args);
        else Log.N (new Exception ("Database not connected!"));
    }

    public static Database Database ()
    {
        return DATABASE;
    }
}
