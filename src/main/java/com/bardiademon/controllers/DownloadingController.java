package com.bardiademon.controllers;

import com.bardiademon.Downloder.Download.Download;
import com.bardiademon.Downloder.Download.On;
import com.bardiademon.Main;
import com.bardiademon.bardiademon.GetSize;
import com.bardiademon.bardiademon.Log;
import com.bardiademon.bardiademon.ShowMessage;
import com.bardiademon.models.DownloadList.DownloadList;
import javafx.application.Platform;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadingController implements On
{

    @FXML
    public Text txtDownloadedOfFilesize;

    @FXML
    public Text txtCenterProgressDownloaded;

    @FXML
    public Label txtFilename;

    @FXML
    public Text txtDownloadedOfFilesize_byte;

    private String url, filename, path;
    private boolean createDir, theNameHasNoSuffix;
    private Stage stage;
    private DownloadResult downloadResult;
    private Download download;

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

    private boolean runDownloadResult = false;

    private boolean fast;

    private long filesize = 0;

    // for method close ,
    // downloadResult.Result (done);
    //        Platform.runLater (() ->
    //        {
    //            stage.close ();
    //            stage.hide ();
    //        });
    private boolean done;

    private Download.ResumeDownload resumeDownload;

    private boolean invokeClose = false;

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

    public static void Launch (final DownloadList _DownloadList , final DownloadResult _DownloadResult)
    {
        Launch (_DownloadList.getLink () , _DownloadList.getFilename () , _DownloadList.getPath () , _DownloadList.isCreatedDir () , _DownloadList.isTheNameHasNoSuffix () , _DownloadResult , null);
    }

    public static void Launch (final DownloadList _DownloadList , final DownloadResult _DownloadResult , final Controller _Controller)
    {
        Launch (_DownloadList.getLink () , _DownloadList.getFilename () , _DownloadList.getPath () , _DownloadList.isCreatedDir () , _DownloadList.isTheNameHasNoSuffix () , _DownloadResult , false , _Controller);
    }

    public static void Launch (final DownloadList _DownloadList , final DownloadResult _DownloadResult , final boolean Fast , final Controller _Controller)
    {
        Launch (_DownloadList.getLink () , _DownloadList.getFilename () , _DownloadList.getPath () , _DownloadList.isCreatedDir () , _DownloadList.isTheNameHasNoSuffix () , _DownloadResult , Fast , _Controller);
    }

    public static void Launch (final String URL , final String Filename , final String Path , final boolean CreateDir , final boolean TheNameHasNoSuffix , final DownloadResult _DownloadResult)
    {
        Launch (URL , Filename , Path , CreateDir , TheNameHasNoSuffix , _DownloadResult , false , null);
    }

    public static void Launch (final String URL , final String Filename , final String Path , final boolean CreateDir , final boolean TheNameHasNoSuffix , final DownloadResult _DownloadResult , final boolean Fast)
    {
        Launch (URL , Filename , Path , CreateDir , TheNameHasNoSuffix , _DownloadResult , Fast , null);
    }

    public static void Launch (final String URL , final String Filename , final String Path , final boolean CreateDir , final boolean TheNameHasNoSuffix , final DownloadResult _DownloadResult , Controller _Controller)
    {
        Launch (URL , Filename , Path , CreateDir , TheNameHasNoSuffix , _DownloadResult , false , _Controller);
    }

    public static void Launch (final String URL , final String Filename , final String Path , final boolean CreateDir , final boolean TheNameHasNoSuffix , final DownloadResult _DownloadResult , final boolean Fast , Controller _Controller)
    {
        Platform.runLater (() -> Main.Launch ("Downloading" , Filename , (Main.Controller <DownloadingController>) (controller , stage) ->
        {
            controller.url = URL;
            controller.filename = Filename;
            controller.path = Path;
            controller.createDir = CreateDir;
            controller.theNameHasNoSuffix = TheNameHasNoSuffix;
            controller.stage = stage;
            controller.downloadResult = _DownloadResult;
            controller.fast = Fast;

            Platform.runLater (() -> controller.txtFilename.setText (Filename));

            controller.run ();

            if (_Controller != null) _Controller.Get (controller);
            controller.stage.getScene ().getWindow ().addEventFilter (WindowEvent.WINDOW_CLOSE_REQUEST , windowEvent -> new Thread (() ->
                    Platform.runLater (() -> controller.close (false))).start ());
        }));
    }

    public void run ()
    {
        download = new Download (url , path , createDir , theNameHasNoSuffix , false , true , this);
    }

    public void setTxtDownloadedOfFilesize (final long size)
    {
        Platform.runLater (() ->
        {
            txtDownloadedOfFilesize.setText (String.format ("%s of %s Downloaded" , GetSize.Get (size) , GetSize.Get (filesize)));
            txtDownloadedOfFilesize_byte.setText (String.format ("%d byte of %d byte Downloaded" , size , filesize));
        });
    }

    @Override
    public void OnDownloading (final int Percent , final String Progress , final String DownloadedString , final long DownloadedLong , final String Speed , final String Time)
    {
        Platform.runLater (() ->
        {
            txtSpeed.setText (Speed);
            txtTime.setText (Time);

            setProgress (Percent);
            setTxtDownloadedOfFilesize (DownloadedLong);
        });

    }

    public void close (final boolean done)
    {
        invokeClose = true;
        this.done = done;
        download.compulsoryStop ();
    }

    @Override
    public boolean OnConnected (final long Size , final File Path)
    {
        filesize = Size;
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
        if (fast)
        {
            if (FullNotDownloaded) return Download.FIEC_RESUME;
            else return Download.FIEC_DELETE;
        }

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
    public void OnExistsFileError (final Exception E)
    {
        Log.N (E);
    }

    @Override
    public void OnExistsFileErrorDeleteFile (final Exception E , final File _File)
    {
        Log.N (_File.getAbsolutePath () , E);
        close (false);
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
        if (!invokeClose)
            Platform.runLater (() -> AfterDownloadController.Launch (url , Path.getAbsolutePath () , GetSize.Get (Path.length ()) , Path.length ()));

        close (true);
    }

    @Override
    public void OnErrorDownloading (final Exception E)
    {
        if (!invokeClose)
        {
            alert ("Downloading error" , "Error: " + E.getMessage ());
            close (false);
        }
        else Log.N (E);
    }

    @Override
    public void OnPause (final Download.ResumeDownload resumeDownload)
    {
        pause = true;
        Platform.runLater (() -> btnPause.setText (BTN_PAUSE_TX__RESUME));
        this.resumeDownload = resumeDownload;
    }

    private void alert (final String header , final String content)
    {
        ShowMessage.Show (Alert.AlertType.ERROR , "Error" , header , content);
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
        Log.N (E);
    }

    @Override
    public void OnCancelDownload ()
    {
        stop ();
    }

    @Override
    public void OnNewLink (final String Link)
    {
        url = Link;
    }

    @Override
    public void OnCompulsoryStop ()
    {
        stop ();
    }

    @Override
    public void OnCompulsoryStopCloseStreamError (final Exception e)
    {
        alert ("Close download error" , "Error compulsory stop > " + e.getMessage ());
        stop ();
    }

    private void stop ()
    {
        runDownloadResult ();
        Platform.runLater (() ->
        {
            stage.close ();
            stage.hide ();
            System.gc ();
        });
    }

    @Override
    public void OnError (final Exception E)
    {
        Log.N (E);
        close (false);
    }

    @Override
    public void OnPrint (final String Message)
    {
    }

    private void runDownloadResult ()
    {
        if (!runDownloadResult && downloadResult != null)
        {
            runDownloadResult = true;
            downloadResult.Result (done);
        }
    }

    private void setProgress (final int percent)
    {
        final float progress = (float) percent / 100;
        this.progress.setProgress (progress);
        this.circleProgress.setProgress (progress);
        Platform.runLater (() -> txtCenterProgressDownloaded.setText (String.format ("Downloaded: %d%%" , percent)));
    }

    public interface DownloadResult
    {
        void Result (final boolean done);
    }

    public interface Controller
    {
        void Get (final DownloadingController Controller);
    }
}
