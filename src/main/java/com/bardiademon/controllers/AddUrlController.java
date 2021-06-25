package com.bardiademon.controllers;

import com.bardiademon.Downloder.Download.Download;
import com.bardiademon.Downloder.Download.OnInfoLink;
import com.bardiademon.Main;
import com.bardiademon.bardiademon.Default;
import com.bardiademon.bardiademon.Log;
import com.bardiademon.bardiademon.ShowMessage;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public final class AddUrlController implements Initializable
{
    private static final String TITLE = "Add URL - " + Default.APP_NAME;

    private EnterUrl enterUrl;
    public TextField txtUrl;
    public Button btnAddUrl;
    private Stage stage;

    private Download download;

    private Result result;

    @Override
    public void initialize (final URL url , final ResourceBundle resourceBundle)
    {
        download = new Download ();
    }

    public void onClickBtnAddUrl ()
    {
        checkURL (txtUrl.getText ());
    }

    private void checkURL (final String url)
    {
        if (url != null && !url.isEmpty ())
        {
            try
            {
                if (url.startsWith ("http://") || url.startsWith ("https://"))
                {
                    if (result == null)
                    {
                        Platform.runLater (() ->
                        {
                            btnAddUrl.setText ("Please wait...");
                            btnAddUrl.setDisable (true);
                        });
                    }

                    new Thread (() ->
                    {
                        try
                        {
                            download.newDownload (url , new OnInfoLink ()
                            {
                                @Override
                                public String OnEnterLink ()
                                {
                                    return url;
                                }

                                @Override
                                public boolean OnConnected (long l , File file)
                                {
                                    enterUrl.GetURL (((result == null) ? txtUrl.getText () : url));
                                    if (result == null)
                                    {
                                        Platform.runLater (() ->
                                        {
                                            stage.close ();
                                            stage.hide ();
                                            enterUrl = null;
                                            System.gc ();
                                        });
                                    }
                                    else
                                    {
                                        result.IsOk (true);
                                        System.gc ();
                                    }
                                    return true;
                                }

                                @Override
                                public void OnFilename (String s)
                                {

                                }

                                @Override
                                public void OnError (Exception e)
                                {
                                    if (result != null) result.IsOk (false);
                                    showErrorMessage (e , url);
                                }

                                @Override
                                public void OnPrint (String s)
                                {

                                }

                                @Override
                                public void OnCancelDownload ()
                                {
                                    Log.N ("OnCancelDownload " + getClass ().getName ());
                                }
                            });
                            new Download ();
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
        else
        {
            if (result == null) showMessage ("Empty url" , "Is Empty URL" , "You must enter an address");
            else result.IsOk (false);
        }
    }

    private void showErrorMessage (final Exception e , final String url)
    {
        showMessage ("Invalid url" , "Invalid Input URL" , e.getMessage () + " [" + url + "]");
    }

    private void showMessage (final String title , final String header , final String content)
    {
        if (result == null)
        {
            Platform.runLater (() ->
            {
                btnAddUrl.setText ("Add");
                btnAddUrl.setDisable (false);
            });

            ShowMessage.Show (Alert.AlertType.ERROR , title , header , content);
        }
        else Log.N (String.format ("Title: %s , Header: %s , Content: %s" , title , header , content));
    }

    public static void Launch (final EnterUrl enterUrl)
    {
        Main.Launch ("AddURL" , TITLE , (Main.Controller <AddUrlController>) (addUrlController , stage) ->
        {
            addUrlController.enterUrl = enterUrl;
            addUrlController.stage = stage;
        });
    }

    public static void Fast (final String URL , final Result _Result)
    {
        final AddUrlController addUrlController = new AddUrlController ();
        addUrlController.enterUrl = url -> Platform.runLater (() -> DownloadPreparationController.Launch (URL , null));
        addUrlController.result = _Result;
        addUrlController.download = new Download ();
        addUrlController.checkURL (URL);
    }

    public interface EnterUrl
    {
        void GetURL (final String URL);
    }

    public interface Result
    {
        void IsOk (final boolean ok);
    }
}
