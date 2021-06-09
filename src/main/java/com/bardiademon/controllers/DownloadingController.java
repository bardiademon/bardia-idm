package com.bardiademon.controllers;

import com.bardiademon.Download;
import com.bardiademon.Main;
import com.bardiademon.bardiademon.Log;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class DownloadingController
{

    private String url, filename, path;
    private boolean createDir, theNameHasNoSuffix;
    private Stage stage;
    private DownloadResult downloadResult;

    @FXML
    public TextField txtURL;

    @FXML
    public Text txtDownloaded;

    @FXML
    public Text txtSpeed;

    @FXML
    public Text txtTime;

    @FXML
    public ProgressBar progress;

    @FXML
    public ProgressIndicator circleProgress;

    @FXML
    public Button btnPause;

    @FXML
    public Button btnCancel;

    @FXML
    public void onClickBtnPause ()
    {

    }

    @FXML
    public void onClickBtnCancel ()
    {
    }

    public static void Launch (final String URL , final String Filename , final String Path , final boolean CreateDir , final boolean TheNameHasNoSuffix , final DownloadResult _DownloadResult)
    {
        Main.Launch ("Downloading" , Filename , (Main.Controller <DownloadingController>) (controller , stage) ->
        {
            controller.url = URL;
            controller.filename = Filename;
            controller.path = Path;
            controller.createDir = CreateDir;
            controller.theNameHasNoSuffix = TheNameHasNoSuffix;
            controller.stage = stage;
            controller.downloadResult = _DownloadResult;
            controller.run ();
        });
    }

    private void run ()
    {
        txtURL.setText (url);
        final Download.Callback callback = (Progress , Downloaded , Speed , Time) -> Platform.runLater (() ->
        {
            if (Progress.equals ("101"))
            {
                stage.close ();
                stage.hide ();

                if (downloadResult != null) downloadResult.Result (true);
            }
            else
            {
                final int progress = Integer.parseInt (Progress);
                this.progress.setProgress ((float) progress / 100);
                circleProgress.setProgress (this.progress.getProgress ());

                txtDownloaded.setText (Downloaded);
                txtSpeed.setText (Speed);
                txtTime.setText (Time);
            }

            System.gc ();
        });

        try
        {
            new Download (url , path , callback , createDir , theNameHasNoSuffix);
        }
        catch (final Exception e)
        {
            if (downloadResult != null) downloadResult.Result (false);
            Log.N (e);
        }
    }

    public interface DownloadResult
    {
        void Result (final boolean done);
    }
}
