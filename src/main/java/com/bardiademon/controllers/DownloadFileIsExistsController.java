package com.bardiademon.controllers;

import com.bardiademon.Downloder.Download.Download;
import com.bardiademon.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
            result.Command (Download.FIEC_RENAME , filename);
            stageClose ();
        }
        else
        {
            final Alert alert = new Alert (Alert.AlertType.ERROR);
            alert.setContentText ("Filename is empty!");
            alert.setHeaderText (filename);
            alert.setTitle ("Filename error!");
            alert.show ();
        }
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
            Platform.runLater (() ->
            {
                controller.txtFilename.setText (Filename);
                controller.btnResume.setDisable (!Resume);
            });
        }));
    }


    public interface Result
    {
        void Command (final int command , final String Filename);
    }
}
