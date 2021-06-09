package com.bardiademon.controllers;

import com.bardiademon.GetInfoLink;
import com.bardiademon.Main;
import com.bardiademon.bardiademon.Default;
import com.bardiademon.bardiademon.Log;
import com.bardiademon.models.DownloadList.DownloadList;
import com.bardiademon.models.DownloadList.DownloadListService;
import com.bardiademon.models.Groups.Groups;
import com.bardiademon.models.Groups.GroupsService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public final class DownloadPreparation implements Initializable
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

    private Stage downloadPreparation;
    private String url;

    private List <Groups> groupsInfo;

    private DownloadListService downloadListService;

    private DownloadList downloadList;

    @Override
    public void initialize (final URL url , final ResourceBundle resourceBundle)
    {

    }

    @FXML
    public void onClickBtnDownloadNow ()
    {
        DownloadingController.Launch (url , txtFilename.getText () , txtSaveAs.getText () , chkCreateFolder.isSelected () , chkTheNameHasNoSuffix.isSelected () , done ->
        {
            if (done)
            {
                saveToDownloadList ();
                btnDownloadCancel ();
            }
        });
    }

    @FXML
    public void btnDownloadCancel ()
    {
        downloadPreparation.close ();
        downloadPreparation.hide ();
        System.gc ();
    }

    @FXML
    public void onClickBtnDownloadLater ()
    {
        if (saveToDownloadList ()) btnDownloadCancel ();
    }

    private boolean saveToDownloadList ()
    {
        final String url = txtURL.getText ();
        if (notEmpty (url) && notEmpty (txtPath.getText ()) && notEmpty (txtFilename.getText ()))
        {
            long filesize;
            try
            {
                filesize = (long) Math.abs (Default.ONE_BYTE * Double.parseDouble ((txtFilesize.getText ().split (" ")[1])));
            }
            catch (final Exception e)
            {
                filesize = 0;
                Log.N (e);
            }

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
                    return downloadListService.add (downloadList);
                }
                else
                {
                    downloadListModify ();
                    return downloadListService.modify (downloadList);
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
        Main.Launch ("DownloadPreparation" , TITLE ,
                (Main.Controller <DownloadPreparation>) (controller , stage) -> controller.load (URL , _DownloadList , stage));
    }

    public void onClickTxtConnectionMessage ()
    {
        txtConnectionMessage.setText ("Connecting...");
        progress.setVisible (true);
        txtConnectionMessage.setDisable (true);
        new GetInfoLink (url , new GetInfoLink.Callback ()
        {
            @Override
            public void Filename (final String Filename)
            {
                txtFilename.clear ();
                txtFilename.setText (Filename);
            }

            @Override
            public void Filesize (final String Filesize)
            {
                txtConnectionMessage.setText ("Connected");
                txtFilesize.setText ("Filesize: " + Filesize);
            }

            @Override
            public void Error (final String Error)
            {
                txtConnectionMessage.setText (Error);

                progress.setVisible (false);
                txtConnectionMessage.setDisable (false);
            }

            @Override
            public void OK ()
            {
                progress.setVisible (false);
                txtConnectionMessage.setDisable (false);

                if (txtConnectionMessage.getText ().contains ("Connected") && !txtFilename.getText ().isEmpty ())
                    setGroup ();

                downloadListModify ();
            }
        });
    }

    private void downloadListModify ()
    {
        if (downloadList != null && downloadList.getId () > 0)
        {
            downloadList.setCreatedDir (chkCreateFolder.isSelected ());
            downloadList.setLink (txtURL.getText ());
            downloadList.setPath (txtSaveAs.getText ());
        }
    }

    private void setGroup ()
    {
        if (groupsInfo != null && groupsInfo.size () > 0)
        {
            final String extension = FilenameUtils.getExtension (txtFilename.getText ());
            for (int i = 0, len = groupsInfo.size (); i < len; i++)
            {
                final Groups group = groupsInfo.get (i);
                if (group.getExtensions ().contains (extension))
                {
                    final int finalI = i;
                    Platform.runLater (() -> this.groups.getSelectionModel ().select (finalI));
                    setTxtPath (group.getDefaultPath () , txtFilename.getText ());
                    return;
                }
            }
        }
    }

    private void setTxtPath (final String path , final String filename)
    {
        txtFilename.setText (filename);
        setTxtPath (path , true);
        setTxtSaveAs ();
    }

    public void onKeyReleasedTxtURL ()
    {
        url = txtURL.getText ();
    }

    private void setTxtPath (final String path , final boolean set)
    {
        if (set) txtPath.setText (path);
        final File file = new File (path);

        final boolean exists = file.exists ();
        txtPathStatus.setText (String.format ("Path is %s" , (exists ? "exists" : "not exists (Click to create a folder)")));
        txtPathStatus.setFill (((exists) ? Color.valueOf ("#0C8929") : Color.valueOf ("#B44410")));
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
        txtSaveAs.setText (new File (String.format ("%s%s%s" , txtPath.getText () , File.separator , txtFilename.getText ())).getPath ());
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
        downloadListService = new DownloadListService ();

        if (downloadList != null)
        {
            this.downloadList = downloadList;

            setTxtPath (new File (downloadList.getPath ()).getParent () , FilenameUtils.getName (downloadList.getPath ()));
            this.url = downloadList.getLink ();
            txtURL.setText (this.url);
            txtDescription.setText (downloadList.getDescription ());
            chkCreateFolder.setSelected (downloadList.isCreatedDir ());
        }
        else
        {
            this.url = url;
            txtURL.setText (this.url);
        }

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

    private void showAlert (final String title , final String headerText , final String content)
    {
        final Alert alert = new Alert (Alert.AlertType.ERROR);
        alert.setTitle (title);
        alert.setHeaderText (headerText);
        alert.setContentText (content);
        alert.showAndWait ();
    }
}
