package com.bardiademon.controllers;

import com.bardiademon.bardiademon.Log;
import com.bardiademon.models.DownloadList.DownloadList;
import com.bardiademon.models.DownloadList.DownloadListService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
    public TableView <DownloadList> downloadList;
    private List <DownloadList> downloadLists;

    @Override
    public void initialize (final URL url , final ResourceBundle resourceBundle)
    {

        final TableColumn <DownloadList, Long> id = new TableColumn <> ("id");
        id.setCellValueFactory (new PropertyValueFactory <> ("id"));

        final TableColumn <DownloadList, String> link = new TableColumn <> ("Link");
        link.setCellValueFactory (new PropertyValueFactory <> ("link"));

        final TableColumn <DownloadList, String> path = new TableColumn <> ("Path");
        path.setCellValueFactory (new PropertyValueFactory <> ("path"));

        final TableColumn <DownloadList, String> size = new TableColumn <> ("Size");
        size.setCellValueFactory (new PropertyValueFactory <> ("size"));

        final TableColumn <DownloadList, LocalDateTime> startedAt = new TableColumn <> ("Started At");
        startedAt.setCellValueFactory (new PropertyValueFactory <> ("startedAt"));

        final TableColumn <DownloadList, LocalDateTime> endAt = new TableColumn <> ("End At");
        endAt.setCellValueFactory (new PropertyValueFactory <> ("endAt"));

        final TableColumn <DownloadList, LocalDateTime> time = new TableColumn <> ("Time");
        time.setCellValueFactory (new PropertyValueFactory <> ("time"));

        final TableColumn <DownloadList, Boolean> downloaded = new TableColumn <> ("Downloaded");
        downloaded.setCellValueFactory (new PropertyValueFactory <> ("completed"));

        final TableColumn <DownloadList, Boolean> createdDir = new TableColumn <> ("Created Dir");
        createdDir.setCellValueFactory (new PropertyValueFactory <> ("createdDir"));

        final TableColumn <DownloadList, Boolean> description = new TableColumn <> ("Description");
        description.setCellValueFactory (new PropertyValueFactory <> ("description"));

        downloadList.getColumns ().addAll (Arrays.asList (id , link , path , size , startedAt , endAt , time , downloaded , createdDir , description));

        new Thread (() ->
        {
            final DownloadListService downloadListService = new DownloadListService ();
            downloadLists = downloadListService.findAll ();
            if (downloadLists != null) downloadList.getItems ().addAll (downloadLists);
        }).start ();
    }

    public void onClickAddUrl ()
    {
        AddUrlController.Launch (URL -> DownloadPreparation.Launch (URL , null));
    }

    public void onClickTableView ()
    {
        final int selectedIndex = downloadList.getSelectionModel ().getSelectedIndex ();
        if (selectedIndex >= 0)
        {
            try
            {
                final DownloadList downloadList = downloadLists.get (selectedIndex);
                DownloadPreparation.Launch (null , downloadList);
            }
            catch (final Exception e)
            {
                Log.N (e);
            }

        }
    }
}
