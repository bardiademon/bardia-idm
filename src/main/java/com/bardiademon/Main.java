package com.bardiademon;

import com.bardiademon.bardiademon.Log;
import com.bardiademon.models.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public final class Main extends Application
{
    public static final Database DATABASE = new Database ();

    @Override
    public void start (final Stage primaryStage)
    {
        Launch ("Main" , "Bardia IDM");
    }

    public static Stage Launch (final String FXMLFilename , final String title)
    {
        try
        {
            final URL resource = (Main.class.getClassLoader ()).getResource (FXMLFilename + ".fxml");
            if (resource != null)
            {
                final Stage stage = new Stage ();
                stage.setTitle (title);
                stage.setScene (new Scene (FXMLLoader.load (resource)));
                stage.show ();

                return stage;
            }
            else Log.N (new Exception ("Resource is null."));
        }
        catch (final IOException e)
        {
            Log.N (e);
        }

        return null;
    }

    public static void main (final String[] args)
    {
        if (DATABASE.connected ()) launch (args);
        else Log.N (new Exception ("Database not connected!"));
    }

    public static Database Database ()
    {
        return DATABASE;
    }
}
