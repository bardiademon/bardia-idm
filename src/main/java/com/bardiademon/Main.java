package com.bardiademon;

import com.bardiademon.bardiademon.Default;
import com.bardiademon.bardiademon.Log;
import com.bardiademon.models.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
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
        Launch ("Main" , Default.APP_NAME , null);
    }

    public static <T> void Launch (final String FXMLFilename , final String Title , final Controller <T> _Controller)
    {
        final URL resource = (Main.class.getClassLoader ()).getResource (FXMLFilename + ".fxml");
        if (resource != null)
        {
            final FXMLLoader fxmlLoader = new FXMLLoader (resource);
            final Stage stage = new Stage ();
            stage.setTitle (Title);
            try
            {
                stage.setScene (new Scene (fxmlLoader.load ()));
                if (_Controller != null) _Controller.GetController (fxmlLoader.getController () , stage);
                stage.show ();
            }
            catch (final IOException e)
            {
                Log.N (e);
            }
        }
        else Log.N (new Exception ("Resource is null."));
    }

    public interface Controller<T>
    {
        void GetController (final T t , final Stage stage);
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
