package com.bardiademon.controllers;

import com.bardiademon.Main;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public final class AddUrlController implements Initializable
{
    private static EnterUrl enterUrl;
    public TextField txtUrl;
    public Button btnAddUrl;
    private static Stage stage;

    @Override
    public void initialize (final URL url , final ResourceBundle resourceBundle)
    {

    }

    public void onClickBtnAddUrl (final ActionEvent actionEvent)
    {
        if (!txtUrl.getText ().isEmpty ())
        {
            enterUrl.GetURL (txtUrl.getText ());
            stage.close ();
            stage.hide ();
            enterUrl = null;
            System.gc ();
        }
        else
        {
            Alert alert = new Alert (Alert.AlertType.ERROR);
            alert.setTitle ("Empty url");
            alert.setHeaderText ("Is Empty URL");
            alert.setContentText ("You must enter an address");
            alert.showAndWait ();
        }
    }

    public static void Launch (final EnterUrl enterUrl)
    {
        AddUrlController.enterUrl = enterUrl;
        stage = Main.Launch ("AddURL" , "Add URL - Bardia IDM");
    }

    public interface EnterUrl
    {
        void GetURL (final String URL);
    }
}
