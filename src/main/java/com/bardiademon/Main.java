package com.bardiademon;

import com.bardiademon.bardiademon.Default;
import com.bardiademon.bardiademon.Log;
import com.bardiademon.controllers.MainController;
import com.bardiademon.models.Database;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.IOException;
import java.net.URL;

public final class Main extends Application
{
    public static final Database DATABASE = new Database ();

    public static MainController mainController;

    public static final Object Wait = new Object ();

    @Override
    public void start (final Stage primaryStage)
    {
        Launch ("Main" , Default.APP_NAME , (Controller <MainController>) (main , stage) ->
        {
            mainController = main;
            stage.getScene ().getWindow ().addEventFilter (WindowEvent.WINDOW_CLOSE_REQUEST , windowEvent ->
            {
                System.gc ();
                Platform.exit ();
                System.exit (0);
            });
        });
    }

    public static MainController getMainController ()
    {
        return mainController;
    }

    public static <T> T Launch (final String FXMLFilename , final String Title , final Controller <T> _Controller)
    {
        final URL resource = (Main.class.getClassLoader ()).getResource (FXMLFilename + ".fxml");

        final var objController = new Object ()
        {
            T controller = null;
        };

        if (resource != null)
        {
            final FXMLLoader fxmlLoader = new FXMLLoader (resource);
            final Stage stage = new Stage ();
            stage.setResizable (false);

            stage.setTitle (Title);
            try
            {
                stage.setScene (new Scene (fxmlLoader.load ()));
                if (_Controller != null)
                {
                    objController.controller = fxmlLoader.getController ();
                    _Controller.GetController (objController.controller , stage);
                }
                stage.show ();
            }
            catch (final IOException e)
            {
                Log.N (e);
            }

        }
        else Log.N (new Exception ("Resource is null."));

        return objController.controller;
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
