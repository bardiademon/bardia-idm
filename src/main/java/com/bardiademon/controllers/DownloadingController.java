package com.bardiademon.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class DownloadingController
{
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
}
