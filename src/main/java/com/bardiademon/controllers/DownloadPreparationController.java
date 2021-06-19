package com.bardiademon.controllers;

import com.bardiademon.Downloder.Download.Download;
import com.bardiademon.Downloder.Download.OnInfoLink;
import com.bardiademon.Main;
import com.bardiademon.bardiademon.Default;
import com.bardiademon.bardiademon.GetSize;
import com.bardiademon.bardiademon.Log;
import com.bardiademon.bardiademon.ShowMessage;
import com.bardiademon.models.DownloadList.DownloadList;
import com.bardiademon.models.DownloadList.DownloadListService;
import com.bardiademon.models.Groups.Groups;
import com.bardiademon.models.Groups.GroupsService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public final class DownloadPreparationController implements Initializable
{
    private static final String TITLE = "Downloaded Preparation - " + Default.APP_NAME;

    @FXML
    public TextField txtURL;

    @FXML
    public JFXComboBox <String> groups;

    @FXML
    public TextField txtFilename;

    @FXML
    public TextField txtPath;

    @FXML
    public JFXButton btnChoosePath;

    @FXML
    public TextField txtSaveAs;

    @FXML
    public TextArea txtDescription;

    @FXML
    public JFXCheckBox chkCreateFolder;

    @FXML
    public JFXCheckBox chkTheNameHasNoSuffix;

    @FXML
    public JFXButton btnDownloadNow;

    @FXML
    public JFXButton btnCancel;

    @FXML
    public JFXButton btnDownloadLater;

    @FXML
    public JFXButton btnAddGroup;

    @FXML
    public Text txtConnectionMessage;

    @FXML
    public Text txtPathStatus;

    @FXML
    public Text txtFilesize;

    @FXML
    public ProgressBar progress;

    @FXML
    public CheckBox chkToHttps;

    private Stage downloadPreparation;
    private String url;

    private List <Groups> groupsInfo;

    private DownloadListService downloadListService;

    private DownloadList downloadList;

    private long filesize;

    private boolean isForInfo = false;
    private int forInfo_index;
    private ForInfo forInfo;
    private ListUrlController.LinkInformation forInfo_linkInformation;

    private boolean close = true;

    @Override
    public void initialize (final URL url , final ResourceBundle resourceBundle)
    {

    }

    @FXML
    public void onClickBtnDownloadNow ()
    {
        final String filename = txtFilename.getText ();
        final String saveAs = txtSaveAs.getText ();
        url = txtURL.getText ();

        if (notEmpty (filename) && notEmpty (saveAs) && notEmpty (url) && txtConnectionMessage.getText ().toLowerCase (Locale.ROOT).contains ("connected"))
        {
            if (isForInfo)
            {
                forInfo_linkInformation.setFilename (filename);
                forInfo_linkInformation.setPath (saveAs);
                forInfo_linkInformation.setLink (url);
                forInfo_linkInformation.setToHttps (chkToHttps.isSelected ());
                forInfo_linkInformation.setCreatedDir (chkCreateFolder.isSelected ());
                forInfo_linkInformation.setTheNameHasNoSuffix (chkTheNameHasNoSuffix.isSelected ());
                forInfo.GetDownloadList (forInfo_index , forInfo_linkInformation);
                btnDownloadCancel ();
            }
            else startDownloading (filename , saveAs);
        }
        else showAlert ("Check info" , "Please check info" , "Filename Or Save As Or URL");
    }

    private void startDownloading (final String filename , final String saveAs)
    {
        close = false;
        btnDownloadNow.setDisable (true);
        btnDownloadLater.setDisable (true);
        btnCancel.setDisable (true);

        Platform.runLater (() -> DownloadingController.Launch (url , filename , saveAs , chkCreateFolder.isSelected () , chkTheNameHasNoSuffix.isSelected () , chkToHttps.isSelected () , done ->
        {
            close = true;
            btnDownloadNow.setDisable (false);
            btnDownloadLater.setDisable (false);
            btnCancel.setDisable (false);

            saveToDownloadList (done);
            if (done) btnDownloadCancel ();
        }));
    }

    @FXML
    public void btnDownloadCancel ()
    {
        if (close)
        {
            Platform.runLater (() ->
            {
                downloadPreparation.close ();
                downloadPreparation.hide ();
                Main.getMainController ().refresh ();
                System.gc ();
            });
        }
    }

    @FXML
    public void onClickBtnDownloadLater ()
    {
        if (saveToDownloadList (false)) btnDownloadCancel ();
    }

    private boolean saveToDownloadList (final boolean completed)
    {
        final String url = txtURL.getText ();
        if (notEmpty (url) && notEmpty (txtPath.getText ()) && notEmpty (txtFilename.getText ()))
        {
            if (filesize > 0)
            {
                if (downloadList == null)
                {
                    downloadList = new DownloadList ();
                    downloadList.setLink (url);
                    downloadList.setSize (filesize);
                    downloadList.setPath (txtSaveAs.getText ());
                    downloadList.setDescription (txtDescription.getText ());
                    downloadList.setCreatedDir (chkCreateFolder.isSelected ());
                    downloadList.setStartedAt (LocalDateTime.now ());
                    downloadList.setCompleted (completed);
                    final boolean add = downloadListService.add (downloadList);
                    if (add) Main.getMainController ().refresh ();
                    return add;
                }
                else
                {
                    downloadListModify (completed);
                    final boolean modify = downloadListService.modify (downloadList);
                    if (modify) Main.getMainController ().refresh ();
                    return modify;
                }
            }
        }

        showAlert ("Error save" , "Error save to download list" , "Check download info");
        return false;
    }

    private boolean notEmpty (final String txt)
    {
        return (txt != null && !txt.isEmpty ());
    }

    public static void Launch (final String URL , final DownloadList _DownloadList)
    {
        Platform.runLater (() -> Main.Launch ("DownloadPreparation" , TITLE ,
                (Main.Controller <DownloadPreparationController>) (controller , stage) -> controller.load (URL , _DownloadList , stage)));
    }

    public static void LaunchFast (final String URL , final DownloadList _DownloadList)
    {
        Platform.runLater (() -> Main.Launch ("DownloadPreparation" , TITLE , (Main.Controller <DownloadPreparationController>) (controller , stage) ->
        {
            controller.load (URL , _DownloadList , stage);
            controller.onClickBtnDownloadNow ();
        }));
    }

    public static void Launch (final String URL , final ListUrlController.LinkInformation _LinkInformation , final int Index , final ForInfo _ForInfo)
    {
        Platform.runLater (() -> Main.Launch ("DownloadPreparation" , TITLE , (Main.Controller <DownloadPreparationController>) (controller , stage) ->
        {
            controller.load (URL , _LinkInformation , stage);
            controller.forInfo (Index , _LinkInformation , _ForInfo);
        }));
    }

    private void forInfo (final int index , final ListUrlController.LinkInformation _LinkInformation , final ForInfo forInfo)
    {
        btnDownloadNow.setText ("Apply changes");

        this.isForInfo = true;
        this.forInfo = forInfo;
        this.forInfo_index = index;
        this.forInfo_linkInformation = _LinkInformation;

        btnDownloadLater.setDisable (true);
    }

    public void onClickTxtConnectionMessage ()
    {
        if (url.contains ("http://") && chkToHttps.isSelected ()) setTxtURL (url);

        txtConnectionMessage.setText ("Connecting...");
        progress.setVisible (true);
        txtConnectionMessage.setDisable (true);
        new Download (url , new OnInfoLink ()
        {
            @Override
            public String OnEnterLink ()
            {
                return url;
            }

            @Override
            public boolean OnConnected (final long Size , final File Path)
            {
                txtConnectionMessage.setText ("Connected");

                if (downloadList != null) downloadList.setStartedAt (LocalDateTime.now ());

                filesize = Size;
                txtFilesize.setText ("Filesize: " + GetSize.Get (filesize));
                return true;
            }

            @Override
            public void OnFilename (final String Filename)
            {
                Platform.runLater (() ->
                {
                    txtFilename.clear ();
                    txtFilename.setText (Filename);

                    progress.setVisible (false);
                    txtConnectionMessage.setDisable (false);
                });

                if (txtConnectionMessage.getText ().contains ("Connected") && !txtFilename.getText ().isEmpty ())
                    setGroup ();

                downloadListModify (false);
            }

            @Override
            public void OnError (final Exception E)
            {
                txtConnectionMessage.setText (E.getMessage ());

                progress.setVisible (false);
                txtConnectionMessage.setDisable (false);
            }
        } , chkToHttps.isSelected ());
    }

    private void downloadListModify (final boolean completed)
    {
        if (downloadList != null && downloadList.getId () > 0)
        {
            downloadList.setCreatedDir (chkCreateFolder.isSelected ());
            downloadList.setLink (txtURL.getText ());
            downloadList.setPath (txtSaveAs.getText ());
            if (!downloadList.isCompleted ()) downloadList.setCompleted (completed);
        }
    }

    private void setGroup ()
    {
        if (groupsInfo != null && groupsInfo.size () > 0)
        {
            final String extension = FilenameUtils.getExtension (txtFilename.getText ());

            final String path = txtPath.getText ();
            for (int i = 0, len = groupsInfo.size (); i < len; i++)
            {
                final Groups group = groupsInfo.get (i);
                if (group.getExtensions ().contains (extension))
                {
                    final int finalI = i;
                    Platform.runLater (() -> this.groups.getSelectionModel ().select (finalI));
                    if (!notEmpty (path)) setTxtPath (group.getDefaultPath () , txtFilename.getText ());
                    return;
                }
            }
        }
    }

    private void setTxtPath (final String path , final String filename)
    {
        Platform.runLater (() -> txtFilename.setText (URLDecoder.decode (filename , StandardCharsets.UTF_8)));
        setTxtPath (path , true);
        setTxtSaveAs ();
    }

    public void onKeyReleasedTxtURL ()
    {
        url = txtURL.getText ();
        onClickTxtConnectionMessage ();
    }

    private void setTxtPath (final String path , final boolean set)
    {
        if (path == null) return;

        if (set) Platform.runLater (() -> txtPath.setText (path));
        final File file = new File (path);

        final boolean exists = file.exists ();
        Platform.runLater (() ->
        {
            txtPathStatus.setText (String.format ("Path is %s" , (exists ? "exists" : "not exists (Click to create a folder)")));
            txtPathStatus.setFill (((exists) ? Color.valueOf ("#0C8929") : Color.valueOf ("#B44410")));
        });
    }

    public void onClickBtnChoosePath ()
    {
        final FileChooser fileChooser = new FileChooser ();
        fileChooser.setTitle ("Save as");

        if (!txtPath.getText ().isEmpty ())
        {
            final File file;
            if ((file = new File (txtPath.getText ())).exists ())
                fileChooser.setInitialDirectory (file);
            else
                Log.N (new Exception ("File <" + file.getPath () + "> not exists"));
        }

        fileChooser.setInitialFileName (txtFilename.getText ());
        final File file = fileChooser.showSaveDialog (null);

        if (file != null)
            setTxtPath (file.getParent () , FilenameUtils.getName (file.getPath ()));

        System.gc ();
    }

    public void onKeyReleasedTxtFilename ()
    {
        chkTheNameHasNoSuffix.setSelected ((!notEmpty (FilenameUtils.getExtension (txtSaveAs.getText ()))));
        setTxtSaveAs ();
    }

    public void onKeyReleasedTxtPath ()
    {
        setTxtPath (txtPath.getText () , false);
        setTxtSaveAs ();
    }

    private void setTxtSaveAs ()
    {
        Platform.runLater (() -> txtSaveAs.setText (new File (String.format ("%s%s%s" , txtPath.getText () , File.separator , txtFilename.getText ())).getPath ()));
    }

    public void onClickTxtStatusPath ()
    {
        final String path = txtPath.getText ();

        if (path != null && !path.isEmpty ())
        {
            final File file = new File (path);
            if (!file.exists ())
            {
                if (!file.mkdirs ())
                {
                    Log.N (new Exception ("Can not create folder"));
                    showAlert ("Create dir" , path , "Unable to create folder (Check the path)");
                }
            }
        }
        setTxtPath (path , false);
    }

    private void load (final String url , final DownloadList downloadList , final Stage downloadPreparation)
    {
        downloadPreparation.setOnCloseRequest (Event::consume);
        downloadPreparation.getScene ().getWindow ().addEventFilter (WindowEvent.WINDOW_CLOSE_REQUEST , windowEvent -> btnDownloadCancel ());

        downloadListService = new DownloadListService ();

        if (downloadList != null)
        {
            this.downloadList = downloadList;

            if (downloadList.getPath () != null)
                setTxtPath (new File (downloadList.getPath ()).getParent () , FilenameUtils.getName (downloadList.getPath ()));

            setTxtURL (downloadList.getLink ());
            txtDescription.setText (downloadList.getDescription ());
            chkCreateFolder.setSelected (downloadList.isCreatedDir ());
        }
        else setTxtURL (url);

        this.downloadPreparation = downloadPreparation;

        final GroupsService groupsService = new GroupsService ();

        groupsInfo = groupsService.getGroups ();

        if (groupsInfo != null)
        {
            for (final Groups group : groupsInfo)
                this.groups.getItems ().addAll (String.format ("%s %s" , group.getName () , group.getExtensions ().toString ()));

        }

        onClickTxtConnectionMessage ();
    }

    private void setTxtURL (final String url)
    {
        this.url = url;
        Platform.runLater (() -> txtURL.setText (URLDecoder.decode (((chkToHttps.isSelected () ? url.replace ("http://" , "https://") : url)) , StandardCharsets.UTF_8)));
    }

    private void showAlert (final String title , final String headerText , final String content)
    {
        ShowMessage.Show (Alert.AlertType.ERROR , title , headerText , content);
    }

    public interface ForInfo
    {
        void GetDownloadList (final int Index , final ListUrlController.LinkInformation _LinkInformation);
    }

    @FXML
    public void onClickChkToHttps ()
    {
        setTxtURL (txtURL.getText ());
        onClickTxtConnectionMessage ();
    }
}
