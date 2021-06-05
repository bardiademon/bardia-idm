package com.bardiademon.controllers;

import com.bardiademon.GetInfoLink;
import com.bardiademon.Main;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public final class DownloadPreparation implements Initializable
{
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
    public Text txtSaveAs;

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
    public ProgressBar progress;

    private static Stage downloadPreparation;
    private static String url;

    @Override
    public void initialize (final URL url , final ResourceBundle resourceBundle)
    {
        txtURL.setText (DownloadPreparation.url);
        onClickTxtConnectionMessage ();
    }

    @FXML
    public void onClickBtnDownloadNow ()
    {

    }

    @FXML
    public void btnDownloadCancel ()
    {

    }

    @FXML
    public void onClickBtnDownloadLater ()
    {

    }

    public static void Launch (final String URL)
    {
        url = URL;
        downloadPreparation = Main.Launch ("DownloadPreparation" , "Downloaded Preparation - Bardia IDM");
    }

    public void onClickTxtConnectionMessage ()
    {
        txtConnectionMessage.setText ("Connecting...");
        progress.setVisible (true);
        txtConnectionMessage.setDisable (true);
        new GetInfoLink (DownloadPreparation.url , new GetInfoLink.Callback ()
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
                txtConnectionMessage.setText ("Connected: " + Filesize);
            }

            @Override
            public void Error (final String Error)
            {
                txtConnectionMessage.setText (Error);
            }

            @Override
            public void OK ()
            {
                progress.setVisible (false);
                txtConnectionMessage.setDisable (false);
            }
        });
    }

    public void onKeyReleasedTxtURL ()
    {
        url = txtURL.getText ();
    }
}
