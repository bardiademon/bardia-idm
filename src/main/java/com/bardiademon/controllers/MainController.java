package com.bardiademon.controllers;

import com.bardiademon.Main;
import com.bardiademon.bardiademon.ShowMessage;
import com.bardiademon.models.DownloadList.DownloadList;
import com.bardiademon.models.DownloadList.DownloadListService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
    public TableView <DownloadList> downloadList;

    @FXML
    public Button btnResume;

    @FXML
    public Button btnResumeAll;

    @FXML
    public Button btnClearList;

    @FXML
    public Button btnDeleteCompleted;

    @FXML
    public Button btnDownload;

    @FXML
    public Button btnRedownload;

    @FXML
    public CheckBox chkClipboardListener;

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
        onClickChkClipboardListener ();
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
                if (isNullDownloadedList && !btnDeleteCompleted.isDisabled ())
                    btnDeleteCompleted.setDisable (true);
            });

            if (!isNullDownloadedList)
            {
                boolean btnDeleteCompletedDisableFalse = false;

                btnResumeAll.setDisable (true);
                for (final DownloadList download : downloadLists)
                {
                    if (download.isCompleted ())
                    {
                        if (!btnDeleteCompletedDisableFalse)
                        {
                            btnDeleteCompletedDisableFalse = true;
                            Platform.runLater (() -> btnDeleteCompleted.setDisable (false));
                        }
                    }
                    else if (btnResumeAll.isDisabled ()) btnResumeAll.setDisable (false);

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
        AddUrlController.Launch (URL -> DownloadPreparationController.Launch (URL , null));
    }


    @FXML
    public void onClickBtnAddListUrl ()
    {
        ListUrlController.Launch ();
    }

    @FXML
    public void onClickBtnResume ()
    {
        final int selectedIndex = downloadList.getSelectionModel ().getSelectedIndex ();
        if (selectedIndex >= 0)
        {
            final DownloadList downloadList = downloadLists.get (selectedIndex);
            if (!downloadList.isCompleted ()) DownloadPreparationController.LaunchFast (null , downloadList);
        }
    }

    @FXML
    public void onClickBtnResumeAll ()
    {
        if (downloadLists != null && downloadLists.size () > 0)
        {
            for (final DownloadList list : downloadLists)
            {
                if (!list.isCompleted ())
                    Platform.runLater (() -> DownloadPreparationController.LaunchFast (list.getLink () , list));
            }
        }
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

    @FXML
    public void onClickTableView (final MouseEvent mouseEvent)
    {
        final int selectedIndex = downloadList.getSelectionModel ().getSelectedIndex ();
        if (selectedIndex >= 0)
        {
            final DownloadList downloadList = downloadLists.get (selectedIndex);
            btnDownload.setDisable (false);

            btnRedownload.setDisable (false);

            if (!downloadList.isCompleted ()) btnResume.setDisable (false);

            if (mouseEvent.getClickCount () == 2) onClickBtnDownload ();
        }
        else
        {
            btnResume.setDisable (true);
            btnDownload.setDisable (true);
            btnRedownload.setDisable (true);
        }
    }

    @FXML
    public void onClickBtnDownload ()
    {
        final int selectedIndex = downloadList.getSelectionModel ().getSelectedIndex ();
        if (selectedIndex >= 0)
            DownloadPreparationController.Launch (null , downloadLists.get (selectedIndex));
    }

    @FXML
    public void onClickBtnRedownload ()
    {
        final int selectedIndex = downloadList.getSelectionModel ().getSelectedIndex ();
        if (selectedIndex >= 0)
            DownloadPreparationController.LaunchFast2 (null , downloadLists.get (selectedIndex));
    }

    @FXML
    public void onClickChkClipboardListener ()
    {
        Main.SetActiveManagementClipboard (chkClipboardListener.isSelected ());
    }
}
