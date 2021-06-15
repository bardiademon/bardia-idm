package com.bardiademon.controllers;

import com.bardiademon.Main;
import com.bardiademon.bardiademon.Default;
import com.bardiademon.bardiademon.ShowMessage;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public final class AddUrlController implements Initializable
{
    private static final String TITLE = "Add URL - " + Default.APP_NAME;

    private EnterUrl enterUrl;
    public TextField txtUrl;
    public Button btnAddUrl;
    private Stage stage;

    @Override
    public void initialize (final URL url , final ResourceBundle resourceBundle)
    {

    }

    public void onClickBtnAddUrl ()
    {
        final String url = txtUrl.getText ();
        if (url != null && !url.isEmpty ())
        {
            try
            {
                if (url.startsWith ("http://") || url.startsWith ("https://"))
                {
                    Platform.runLater (() ->
                    {
                        btnAddUrl.setText ("Please wait...");
                        btnAddUrl.setDisable (true);
                    });

                    new Thread (() ->
                    {
                        try
                        {
                            // baraye barasi
                            new URL (url).openConnection ().connect (); // throw Exception

                            enterUrl.GetURL (txtUrl.getText ());
                            Platform.runLater (() ->
                            {
                                stage.close ();
                                stage.hide ();
                                enterUrl = null;
                                System.gc ();
                            });
                        }
                        catch (final Exception e)
                        {
                            showErrorMessage (e , url);
                        }
                    }).start ();

                }
                else throw new Exception ("Not started with http:// or https://");
            }
            catch (final Exception e)
            {
                showErrorMessage (e , url);
            }
        }
        else showMessage ("Empty url" , "Is Empty URL" , "You must enter an address");
    }

    private void showErrorMessage (final Exception e , final String url)
    {
        showMessage ("Invalid url" , "Invalid Input URL" , e.getMessage () + " [" + url + "]");
    }

    private void showMessage (final String title , final String header , final String content)
    {
        Platform.runLater (() ->
        {
            btnAddUrl.setText ("Add");
            btnAddUrl.setDisable (false);
        });

        ShowMessage.Show (Alert.AlertType.ERROR , title , header , content);
    }

    public static void Launch (final EnterUrl enterUrl)
    {
        Main.Launch ("AddURL" , TITLE , (Main.Controller <AddUrlController>) (addUrlController , stage) ->
        {
            addUrlController.enterUrl = enterUrl;
            addUrlController.stage = stage;
        });
    }

    public interface EnterUrl
    {
        void GetURL (final String URL);
    }
}
