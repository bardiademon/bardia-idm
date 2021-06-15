package com.bardiademon.controllers;

import com.bardiademon.Main;
import com.bardiademon.bardiademon.ShowMessage;
import com.bardiademon.models.DownloadList.DownloadList;
import com.bardiademon.models.DownloadList.DownloadListService;
import com.jfoenix.controls.JFXListView;
import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public final class ListUrlController implements Initializable
{
    @FXML
    public JFXListView <String> list;

    @FXML
    public Button btnRemoveSelected;

    private Stage stage;

    private DownloadListService downloadListService;

    private File fileChooser;

    @Override
    public void initialize (final URL url , final ResourceBundle resourceBundle)
    {
        downloadListService = new DownloadListService ();
    }

    @FXML
    public void onClickBtnAdd ()
    {
        AddUrlController.Launch (URL -> list.getItems ().add (URL));
    }

    @FXML
    public void onClickBtnAddWithFile ()
    {
        final FileChooser chooser = new FileChooser ();

        if (fileChooser != null && fileChooser.exists ())
            chooser.setInitialDirectory (fileChooser.getParentFile ());

        chooser.getExtensionFilters ().add (new FileChooser.ExtensionFilter ("Text and html file" , "*.txt" , "*.htm" , "*.html"));

        chooser.setTitle ("Selected file");

        fileChooser = chooser.showOpenDialog (null);

        if (fileChooser != null && fileChooser.exists ())
        {
            final String extension = FilenameUtils.getExtension (fileChooser.getPath ());
            if (!extension.isEmpty () && (extension.equals ("txt") || extension.equals ("htm") || extension.equals ("html")))
                findUrl ();
            else
                ShowMessage.Show (Alert.AlertType.ERROR , "Invalid extension" , "This extension is invalid" , "Valid extension [*.txt,*.htm,*.html]");
        }
    }

    private void findUrl ()
    {
        try
        {
            final List <String> lines = Files.readAllLines (fileChooser.toPath ());
            if (lines.size () > 0)
            {
                final List <String> urls = new ArrayList <> ();
                for (final String line : lines) urls.addAll (find (line));

                if (urls.size () > 0)
                {
                    ShowMessage.Show (Alert.AlertType.INFORMATION , "Find" , "Several addresses were found from the selected file" , "Number of links found: " + urls.size ());
                    Platform.runLater (() ->
                    {
                        list.getItems ().addAll (urls);
                        urls.clear ();
                        System.gc ();
                    });
                }
                else
                {
                    ShowMessage.Show (Alert.AlertType.ERROR , "Not find" , "There was no link in the selected file" , "No link found");
                    System.gc ();
                }
            }
        }
        catch (final IOException e)
        {
            ShowMessage.Show (Alert.AlertType.ERROR , "Error" , "Error read file" , e.getMessage ());
            System.gc ();
        }
    }

    private List <String> find (final String line)
    {
        final List <Url> detect = (new UrlDetector (line , UrlDetectorOptions.Default)).detect ();
        final List <String> urls = new LinkedList <> ();

        if (detect != null && detect.size () > 0)
        {
            for (final Url url : detect)
            {
                final String fullUrl = url.getFullUrl ();
                if (!urls.contains (fullUrl) && !list.getItems ().contains (fullUrl)) urls.add (fullUrl);
            }
        }
        System.gc ();
        return urls;
    }

    @FXML
    public void onClickBtnRemoveSelected ()
    {
        if (list.getItems ().size () > 0) list.getItems ().remove (list.getSelectionModel ().getSelectedIndex ());
    }

    @FXML
    public void onClickBtnCancel ()
    {
        Platform.runLater (() ->
        {
            stage.hide ();
            stage.close ();
            System.gc ();
        });
    }

    @FXML
    public void onClickBtnDownloadNow ()
    {
        
    }

    @FXML
    public void onClickBtnDownloadLater ()
    {
        if (list.getItems ().size () > 0)
        {
            final ObservableList <String> items = list.getItems ();
            for (final String item : items)
            {
                final DownloadList downloadList = new DownloadList ();
                downloadList.setLink (item);
                downloadListService.add (downloadList);
            }
            onClickBtnCancel ();
        }
    }

    @FXML
    public void onClickBtnRemoveAll ()
    {
        list.getItems ().clear ();
    }

    public static void Launch ()
    {
        Main.Launch ("ListUrl" , "Add list" , (Main.Controller <ListUrlController>) (controller , stage) -> controller.stage = stage);
    }


}
