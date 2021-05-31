package com.bardiademon;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public final class Main extends Application
{

    @Override
    public void start (final Stage primaryStage) throws Exception
    {
        final URL resource = (getClass ().getClassLoader ()).getResource ("sample.fxml");
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
        launch (args);
    }
}
