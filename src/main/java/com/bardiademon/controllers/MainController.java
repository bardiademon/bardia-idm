package com.bardiademon.controllers;

import com.bardiademon.Main;
import com.bardiademon.bardiademon.Log;
import com.bardiademon.bardiademon.ShowMessage;
import com.bardiademon.models.DownloadList.DownloadList;
import com.bardiademon.models.DownloadList.DownloadListService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
    public TableView <DownloadList> downloadList;

    @FXML
    public Button btnResume;

    @FXML
    public Button btnStop;

    @FXML
    public Button btnStopAll;

    @FXML
    public Button btnResumeAll;

    @FXML
    public Button btnClearList;

    @FXML
    public Button btnDeleteCompleted;


    private List <DownloadList> downloadLists;

    private DownloadListService downloadListService;

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

        refresh ();
    }

    public void refresh ()
    {
        new Thread (() ->
        {
            Main.Database ().reconnect ();
            clear ();
            if (downloadListService == null) downloadListService = new DownloadListService ();

            downloadLists = downloadListService.findAll ();

            final boolean isNullDownloadedList = downloadLists == null;

            Platform.runLater (() ->
            {
                btnClearList.setDisable (isNullDownloadedList);
                btnDeleteCompleted.setDisable (isNullDownloadedList);
            });

            if (!isNullDownloadedList)
            {
                boolean btnDeleteCompletedDisableFalse = false;
                for (final DownloadList download : downloadLists)
                {
                    if (download.isCompleted () && !btnDeleteCompletedDisableFalse)
                    {
                        btnDeleteCompletedDisableFalse = true;
                        Platform.runLater (() -> btnDeleteCompleted.setDisable (false));
                    }
                    Platform.runLater (() -> downloadList.getItems ().add (download));
                }
                Platform.runLater (() -> btnClearList.setDisable (false));
            }
        }).start ();
    }

    private void clear ()
    {
        Platform.runLater (() -> downloadList.getItems ().clear ());
        if (downloadLists != null) downloadLists.clear ();
        System.gc ();
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

    @FXML
    public void onClickBtnAddListUrl ()
    {
        ListUrlController.Launch ();
    }

    @FXML
    public void onClickBtnResume ()
    {

    }

    @FXML
    public void onClickBtnStop ()
    {

    }

    @FXML
    public void onClickBtnStopAll ()
    {

    }

    @FXML
    public void onClickBtnResumeAll ()
    {

    }

    @FXML
    public void onClickBtnClearList ()
    {
        new Thread (() ->
        {
            final String textBtnClearList = btnClearList.getText ();

            Platform.runLater (() ->
            {
                btnClearList.setText ("Please wait...");
                btnClearList.setDisable (true);
            });

            final int size = downloadList.getItems ().size ();
            if (size > 0 && downloadListService.removeAll ())
            {
                ShowMessage.Show (Alert.AlertType.INFORMATION , "Clear list" , "The list was cleared" , "Number of deleted: " + size);
                refresh ();
            }
            Platform.runLater (() -> btnClearList.setText (textBtnClearList));
        }).start ();
    }

    @FXML
    public void onClickBtnDeleteCompleted ()
    {
        new Thread (() ->
        {
            final String textBtnDeleteCompleted = btnDeleteCompleted.getText ();
            Platform.runLater (() ->
            {
                btnDeleteCompleted.setText ("Please wait...");
                btnDeleteCompleted.setDisable (true);
            });

            final int numberOfDeleted;
            if (downloadList.getItems ().size () > 0 && (numberOfDeleted = downloadListService.removeCompleted ()) > 0)
            {
                ShowMessage.Show (Alert.AlertType.INFORMATION , "Clear completed" , "Completions were deleted" , "Number of deleted: " + numberOfDeleted);
                refresh ();
            }
            Platform.runLater (() -> btnDeleteCompleted.setText (textBtnDeleteCompleted));
        }).start ();
    }
}
