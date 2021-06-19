package com.bardiademon.controllers;

import com.bardiademon.Main;
import com.bardiademon.bardiademon.Log;
import com.bardiademon.bardiademon.Path;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class AfterDownloadController implements Initializable
{
    private Stage stage;

    @FXML
    public TextField txtURL;

    @FXML
    public TextField txtPath;

    @FXML
    public CheckBox chkDontShow;

    @FXML
    public ImageView imgDownloaded;

    @FXML
    public Text txtSize;

    @FXML
    public void onClickBtnOpenFolder ()
    {
        open (txtPath.getText () , false);
    }

    private void open (final String path , final boolean openFile)
    {
        final File file = new File (path);
        if (file.exists ())
        {
            try
            {
                final Desktop desktop = Desktop.getDesktop ();
                if (file.isDirectory () || openFile) desktop.open (file);
                else desktop.open (file.getParentFile ());
            }
            catch (final IOException e)
            {
                Platform.runLater (() ->
                {
                    final Alert alert = new Alert (Alert.AlertType.ERROR);
                    alert.setTitle ("Error open folder");
                    alert.setHeaderText ("Cannot open folder");
                    alert.setContentText (file.getAbsolutePath ());
                    alert.show ();
                });
                Log.N (e);
            }
        }
    }

    @FXML
    public void onClickBtnOpen ()
    {
        open (txtPath.getText () , true);
    }

    @FXML
    public void onClickBtnClose ()
    {
        Platform.runLater (() ->
        {
            stage.close ();
            stage.hide ();
            System.gc ();
        });
    }

    @Override
    public void initialize (final URL url , final ResourceBundle resourceBundle)
    {
        try
        {
            imgDownloaded.setImage (new Image ((new File (Path.IC_DOWNLOADED).toURI ()).toString ()));
        }
        catch (final Exception e)
        {
            Log.N (e);
        }
    }

    public static void Launch (final String URL , final String Path , final String Size , final long SizeByte)
    {
        Platform.runLater (() -> Main.Launch ("AfterDownloaded" , "Downloaded was completed" , (Main.Controller <AfterDownloadController>) (controller , stage) -> Platform.runLater (() ->
        {
            controller.txtURL.setText (URLDecoder.decode (URL , StandardCharsets.UTF_8));
            controller.txtPath.setText (Path);
            controller.txtSize.setText (String.format ("Downloaded %s (%d Bytes)" , Size , SizeByte));
            controller.stage = stage;
            controller.stage.getScene ().getWindow ().addEventFilter (WindowEvent.WINDOW_CLOSE_REQUEST , windowEvent -> controller.onClickBtnClose ());
        })));
    }
}
