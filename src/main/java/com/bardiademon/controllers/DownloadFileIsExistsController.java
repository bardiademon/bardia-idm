package com.bardiademon.controllers;

import com.bardiademon.Downloder.Download.Download;
import com.bardiademon.Main;
import com.bardiademon.bardiademon.ShowMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;

public class DownloadFileIsExistsController
{
    private Result result;
    private Stage stage;

    @FXML
    public TextField txtFilename;

    @FXML
    public Button btnResume;

    @FXML
    public Button btnDelete;

    @FXML
    public Button btnRename;

    @FXML
    public Button btnCancel;

    private String filename;

    @FXML
    public void onClickBtnResume ()
    {
        result.Command (Download.FIEC_RESUME , null);
        stageClose ();
    }

    private void stageClose ()
    {
        Platform.runLater (() ->
        {
            stage.close ();
            stage.hide ();
        });
    }

    @FXML
    public void onClickBtnDelete ()
    {
        result.Command (Download.FIEC_DELETE , null);
        stageClose ();
    }

    @FXML
    public void onClickBtnRename ()
    {
        final String filename = txtFilename.getText ();
        if (filename != null && !filename.isEmpty ())
        {
            if (!FilenameUtils.getBaseName (this.filename).equals (filename))
            {
                System.out.println (this.filename);
                result.Command (Download.FIEC_RENAME , this.filename);
                stageClose ();
            }
            else
                ShowMessage.Show (Alert.AlertType.ERROR , "Error filename" , "The file name is duplicate" , "Filename: " + filename);
        }
        else ShowMessage.Show (Alert.AlertType.ERROR , "Filename error!" , "Filename is empty!" , "");
    }

    @FXML
    public void onClickBtnCancel ()
    {
        result.Command (Download.FIEC_CANCEL , null);
        stageClose ();
    }

    public static void Launch (final Result _Result , final String Filename , final boolean Resume)
    {
        Platform.runLater (() -> Main.Launch ("DownloadFileIsExists" , "Download file is exists" , (Main.Controller <DownloadFileIsExistsController>) (controller , stage) ->
        {
            controller.result = _Result;
            controller.stage = stage;
            controller.filename = Filename;
            Platform.runLater (() ->
            {
                controller.txtFilename.setText (FilenameUtils.getBaseName (controller.filename));
                controller.btnResume.setDisable (!Resume);
            });
        }));
    }


    public interface Result
    {
        void Command (final int command , final String Filename);
    }
}
