package com.bardiademon.controllers;

import com.bardiademon.models.DownloadList.DownloadList;
import com.bardiademon.models.DownloadList.DownloadListRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public final class MainController implements Initializable
{
    @FXML
    public Button btnAddUrl;

    @FXML
    public Button btnResume;

    @FXML
    public Button btnStop;

    @FXML
    public Button btnStopAll;

    @FXML
    public Button btnResumeAll;

    @FXML
    public Button btnDeleteCompleted;

    @FXML
    public Button btnAddListUrl;

    @FXML
    public TableView <String> downloadList;

    @Override
    public void initialize (final URL url , final ResourceBundle resourceBundle)
    {
        final TableColumn <String, String> id = new TableColumn <> ("id");
        final TableColumn <String, String> name = new TableColumn <> ("name");
        final TableColumn <String, String> family = new TableColumn <> ("family");


        downloadList.getColumns ().addAll (Arrays.asList (id , name , family));
    }

    public void onClickAddUrl (final ActionEvent actionEvent)
    {
        final DownloadListRepository downloadListRepository = new DownloadListRepository ();
        final DownloadList downloadList = new DownloadList ();

        System.out.println (downloadListRepository.findAll ().toString ());
    }
}
