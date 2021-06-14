package com.bardiademon.controllers;

import com.bardiademon.Downloder.Download.Download;
import com.bardiademon.Downloder.Download.On;
import com.bardiademon.Main;
import com.bardiademon.bardiademon.GetSize;
import com.bardiademon.bardiademon.Log;
import javafx.application.Platform;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadingController implements On
{

    private String url, filename, path;
    private boolean createDir, theNameHasNoSuffix;
    private Stage stage;
    private DownloadResult downloadResult;
    private Download download;

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

    private boolean pause;

    // for method close ,
    // downloadResult.Result (done);
    //        Platform.runLater (() ->
    //        {
    //            stage.close ();
    //            stage.hide ();
    //        });
    private boolean done;

    private Download.ResumeDownload resumeDownload;

    private static final String BTN_PAUSE_TX__PAUSE = "Pause", BTN_PAUSE_TX__RESUME = "Resume";

    @FXML
    public void onClickBtnPause ()
    {
        if (!pause) new Thread (() -> download.pause ()).start ();
        else
        {
            pause = false;
            Platform.runLater (() -> btnPause.setText (BTN_PAUSE_TX__PAUSE));
            if (resumeDownload != null) resumeDownload.Resume (true);
        }
    }

    @FXML
    public void onClickBtnCancel ()
    {
        close (false);
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

            controller.stage.getScene ().getWindow ().addEventFilter (WindowEvent.WINDOW_CLOSE_REQUEST , windowEvent -> new Thread (() ->
                    Platform.runLater (() -> controller.close (false))).start ());

        });
    }

    private void run ()
    {
        txtURL.setText (url);
        download = new Download (url , path , createDir , theNameHasNoSuffix , false , true , this);
    }

    @Override
    public void OnDownloading (final int Percent , final String Progress , final String DownloadedString , final long DownloadedLong , final String Speed , final String Time)
    {
        Platform.runLater (() ->
        {
            txtSpeed.setText (Speed);
            txtTime.setText (Time);

            final float progress = (float) Percent / 100;
            DownloadingController.this.progress.setProgress (progress);
            DownloadingController.this.circleProgress.setProgress (progress);

            txtDownloaded.setText (URLDecoder.decode (DownloadedString , StandardCharsets.UTF_8));

            if (DownloadingController.this.progress.getProgress () >= 1) close (true);
        });
    }

    private void close (final boolean done)
    {
        this.done = done;
        download.compulsoryStop ();
    }

    @Override
    public boolean OnConnected (final long Size , final File Path)
    {
        return true;
    }

    @Override
    public boolean OnConnectedList (final long Size)
    {
        return false;
    }

    @Override
    public int OnExistsFile (final boolean FullNotDownloaded)
    {
        final AtomicInteger result = new AtomicInteger (Download.FIEC_CANCEL);
        new Thread (() -> Platform.runLater (() -> DownloadFileIsExistsController.Launch ((command , Filename) ->
        {
            result.set (command);
            DownloadingController.this.filename = Filename;
            synchronized (DownloadingController.this)
            {
                DownloadingController.this.notify ();
                DownloadingController.this.notifyAll ();
            }
        } , filename , FullNotDownloaded))).start ();

        synchronized (DownloadingController.this)
        {
            try
            {
                DownloadingController.this.wait ();
            }
            catch (final InterruptedException e)
            {
                Log.N (e);
            }
        }

        if (result.get () == Download.FIEC_CANCEL) close (false);

        return result.get ();
    }

    @Override
    public String OnEnterPath (final String Filename , final boolean JustDir)
    {
        return path;
    }

    @Override
    public void OnErrorPath (final Exception E , final File _File , final String Filename)
    {

    }

    @Override
    public String OnEnterLink ()
    {
        return url;
    }

    @Override
    public void OnDownloaded (final File Path)
    {
//        alert (Alert.AlertType.INFORMATION , "Downloaded" , Path.getAbsolutePath () , "Downloaded was completed");
        AfterDownloadController.Launch (txtURL.getText () , Path.getAbsolutePath () , GetSize.Get (Path.length ()) , Path.length ());
        close (true);
    }

    @Override
    public void OnErrorDownloading (final Exception E)
    {
        alert (Alert.AlertType.ERROR , "Error" , "Downloading error" , "Error: " + E.getMessage ());
        close (false);
    }

    @Override
    public void OnPause (final Download.ResumeDownload resumeDownload)
    {
        pause = true;
        Platform.runLater (() -> btnPause.setText (BTN_PAUSE_TX__RESUME));
        this.resumeDownload = resumeDownload;
    }

    private void alert (final Alert.AlertType alertType , final String title , final String header , final String content)
    {
        Platform.runLater (() ->
        {
            final Alert alert = new Alert (alertType);
            alert.setTitle (title);
            alert.setHeaderText (header);
            alert.setContentText (content);
            alert.show ();
        });
    }

    @Override
    public void OnErrorPause (final Exception E , final boolean Pause)
    {

    }

    @Override
    public void OnFilename (final String Filename)
    {

    }

    @Override
    public String OnRenameFileExists (final String Filename)
    {
        return filename;
    }

    @Override
    public void OnErrorRenameFileExists (final Exception E)
    {

    }

    @Override
    public void OnCancelDownload ()
    {

    }

    @Override
    public void OnCompulsoryStop ()
    {
        downloadResult.Result (done);
        Platform.runLater (() ->
        {
            stage.close ();
            stage.hide ();
            System.gc ();
        });
    }

    @Override
    public void OnCompulsoryStopCloseStreamError (final Exception e)
    {
        alert (Alert.AlertType.ERROR , "Error" , "Close download error" , "Error compulsory stop > " + e.getMessage ());
    }

    @Override
    public void OnError (final Exception E)
    {

    }

    public interface DownloadResult
    {
        void Result (final boolean done);
    }
}
