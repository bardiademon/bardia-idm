package com.bardiademon.controllers;

import com.bardiademon.Downloder.Download.Download;
import com.bardiademon.Downloder.Download.OnInfoLink;
import com.bardiademon.Main;
import com.bardiademon.bardiademon.Log;
import com.bardiademon.bardiademon.ShowMessage;
import com.bardiademon.models.DownloadList.DownloadList;
import com.bardiademon.models.DownloadList.DownloadListService;
import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public final class ListUrlController implements Initializable
{
    @FXML
    public TableView <LinkInformation> list;

    @FXML
    public Button btnRemoveSelected;

    @FXML
    public TextField txtPath;

    @FXML
    public Button btnDownloadNow;

    private Stage stage;

    private DownloadListService downloadListService;

    private File fileChooser;

    private List <LinkInformation> linkInformation;

    private ManagementDownloading managementDownloading;

    @Override
    public void initialize (final URL url , final ResourceBundle resourceBundle)
    {
        downloadListService = new DownloadListService ();

        final TableColumn <LinkInformation, String> filename = new TableColumn <> ("Filename");
        filename.setCellValueFactory (new PropertyValueFactory <> ("filename"));

        final TableColumn <LinkInformation, String> size = new TableColumn <> ("Size");
        size.setCellValueFactory (new PropertyValueFactory <> ("size"));

        final TableColumn <LinkInformation, String> status = new TableColumn <> ("Status");
        status.setCellValueFactory (new PropertyValueFactory <> ("status"));

        Platform.runLater (() -> list.getColumns ().addAll (Arrays.asList (filename , size , status)));
        linkInformation = new ArrayList <> ();
    }

    private void refresh ()
    {
        Platform.runLater (() ->
        {
            list.getItems ().clear ();
            list.getItems ().addAll (linkInformation);
        });
    }

    @FXML
    public void onClickBtnAdd ()
    {
        AddUrlController.Launch (this::addToList);
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
                        addToList (Arrays.asList (urls.toArray (new String[] { })).toArray (new String[] { }));
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

    private boolean hasLink (final String link)
    {
        for (final LinkInformation information : linkInformation)
            if (information.getLink ().equals (link)) return true;

        return false;
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
                if (!urls.contains (fullUrl)) urls.add (fullUrl);
            }
        }
        System.gc ();
        return urls;
    }

    @FXML
    public void onClickBtnRemoveSelected ()
    {
        if (list.getItems ().size () > 0)
        {
            final int selectedIndex = list.getSelectionModel ().getSelectedIndex ();
            list.getItems ().remove (selectedIndex);
            linkInformation.remove (selectedIndex);
        }
    }

    @FXML
    public void onClickBtnCancel ()
    {
        if (managementDownloading != null)
            managementDownloading.stop ();

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
        if (linkInformation.size () > 0)
        {
            final String path = txtPath.getText ();
            if (path != null && !path.isEmpty ())
            {
                String status;
                for (final LinkInformation information : linkInformation)
                {
                    if ((status = information.getStatus ().toLowerCase (Locale.ROOT)).contains ("please wait") || status.contains ("connecting"))
                    {
                        ShowMessage.Show (Alert.AlertType.ERROR , "not connected" , "Wait until all links are connected" , "Please wait...");
                        return;
                    }
                }
                onClickBtnDeleteTheUnconnected ();

                for (final LinkInformation information : linkInformation)
                {
                    if (information.getPath () == null || information.getPath ().isEmpty ())
                        information.setPath (String.format ("%s%s%s" , path , File.separator , information.getFilename ()));
                }

                managementDownloading = new ManagementDownloading (new ArrayList <> (linkInformation));

                btnDownloadNow.setDisable (true);
            }
            else ShowMessage.Show (Alert.AlertType.ERROR , "is empty" , "Path is empty" , "Please enter path");
        }
    }

    @FXML
    public void onClickBtnDownloadLater ()
    {
        if (list.getItems ().size () > 0)
        {
            new Thread (() ->
            {
                final ObservableList <LinkInformation> items = list.getItems ();
                for (final LinkInformation item : items)
                {
                    final DownloadList downloadList = new DownloadList ();
                    downloadList.setLink (item.getLink ());
                    downloadListService.add (downloadList);
                }
                Main.getMainController ().refresh ();
            }).start ();
            onClickBtnCancel ();
        }
    }

    @FXML
    public void onClickBtnRemoveAll ()
    {
        list.getItems ().clear ();
        linkInformation.clear ();
    }

    public static void Launch ()
    {
        Main.Launch ("ListUrl" , "Add list" , (Main.Controller <ListUrlController>) (controller , stage) -> controller.stage = stage);
    }

    private void addToList (final String... links)
    {
        new Thread (() ->
        {
            final int start = (linkInformation.size () == 0 ? 0 : linkInformation.size () - 1);

            for (final String link : links)
            {
                if (!hasLink (link))
                {
                    final LinkInformation linkInformation = new LinkInformation (link);
                    linkInformation.setFilename (link);
                    linkInformation.setStatus ("Please wait...");
                    ListUrlController.this.linkInformation.add (linkInformation);
                    list.getItems ().add (linkInformation);
                }
            }

            System.gc ();

            if (start + 1 >= linkInformation.size ()) return;

            final Download download = new Download ();
            try
            {
                for (int i = start, len = links.length; i < len; i++) connect (i , download , links[i]);
            }
            catch (final Exception e)
            {
                Log.N (e);
                synchronized (ListUrlController.this)
                {
                    ListUrlController.this.notify ();
                    ListUrlController.this.notifyAll ();
                }
            }

            System.gc ();

        }).start ();

    }


    @FXML
    public void onClickBtnChoosePath ()
    {
        final DirectoryChooser directoryChooser = new DirectoryChooser ();
        directoryChooser.setTitle ("Save as");

        if (!txtPath.getText ().isEmpty ())
        {
            final File file;
            if ((file = new File (txtPath.getText ())).exists ())
                directoryChooser.setInitialDirectory (file);
            else
                Log.N (new Exception ("File <" + file.getPath () + "> not exists"));
        }

        final File file = directoryChooser.showDialog (null);

        if (file != null) txtPath.setText (file.getAbsolutePath ());

        System.gc ();
    }

    @FXML
    public void onClickBtnDeleteTheUnconnected ()
    {
        if (this.linkInformation.size () > 0)
        {
            for (int i = 0, len = this.linkInformation.size (); i < len; i++)
            {
                try
                {
                    if (!this.linkInformation.get (i).status.toLowerCase (Locale.ROOT).contains ("connected"))
                    {
                        try
                        {
                            final int finalI = i;
                            Platform.runLater (() ->
                            {
                                try
                                {
                                    list.getItems ().remove (finalI);
                                }
                                catch (final Exception e)
                                {
                                    Log.N (e);
                                }
                            });
                            this.linkInformation.remove (i);
                        }
                        catch (final Exception e)
                        {
                            Log.N (e);
                            break;
                        }
                        i--;
                    }
                }
                catch (final Exception e)
                {
                    Log.N (e);
                    break;
                }
            }
            System.gc ();
        }
    }

    @FXML
    public void onCLickList ()
    {
        if (linkInformation.size () > 0)
        {
            try
            {
                final int selectedIndex = list.getSelectionModel ().getSelectedIndex ();
                if (selectedIndex >= 0)
                {
                    final LinkInformation linkInformation = this.linkInformation.get (selectedIndex);
                    DownloadPreparationController.Launch (linkInformation.getLink () , linkInformation , selectedIndex , (index , information) ->
                    {
                        this.linkInformation.set (index , information);
                        new Thread (ListUrlController.this::refresh).start ();
                        new Thread (() -> connect (index , new Download () , information.getLink ()));
                    });
                }
            }
            catch (final Exception e)
            {
                Log.N (e);
            }
        }
    }

    private void connect (final int index , final Download download , final String link)
    {
        linkInformation.get (index).setStatus ("Downloading...");
        Platform.runLater (() -> list.getItems ().set (index , linkInformation.get (index)));
        new Thread (ListUrlController.this::refresh).start ();
        download.newDownload (link , new OnInfoLink ()
        {
            @Override
            public String OnEnterLink ()
            {
                return link;
            }

            @Override
            public boolean OnConnected (final long Size , final File Path)
            {
                try
                {
                    ListUrlController.this.linkInformation.get (index).setSize (Size);
                    ListUrlController.this.linkInformation.get (index).setStatus ("Connected");
                }
                catch (final Exception e)
                {
                    ListUrlController.this.linkInformation.get (index).setStatus (e.getMessage ());
                    Log.N (e);
                }
                Platform.runLater (() -> list.getItems ().set (index , linkInformation.get (index)));
                new Thread (ListUrlController.this::refresh).start ();

                return true;
            }

            @Override
            public void OnFilename (final String Filename)
            {
                try
                {
                    ListUrlController.this.linkInformation.get (index).setFilename (Filename);
                    Platform.runLater (() -> list.getItems ().set (index , linkInformation.get (index)));
                    new Thread (ListUrlController.this::refresh).start ();
                }
                catch (final Exception e)
                {
                    Log.N (e);
                }

                synchronized (ListUrlController.this)
                {
                    ListUrlController.this.notify ();
                    ListUrlController.this.notifyAll ();
                }
            }

            @Override
            public void OnError (final Exception E)
            {
                try
                {
                    ListUrlController.this.linkInformation.get (index).setFilename (link + " (ERROR [" + E.getMessage () + "])");
                    ListUrlController.this.linkInformation.get (index).setSize (0);
                    ListUrlController.this.linkInformation.get (index).setStatus ("Error [" + E.getMessage () + "]");
                    Platform.runLater (() -> list.getItems ().set (index , linkInformation.get (index)));
                    new Thread (ListUrlController.this::refresh).start ();
                }
                catch (final Exception e)
                {
                    Log.N (e);
                }

                synchronized (ListUrlController.this)
                {
                    ListUrlController.this.notify ();
                    ListUrlController.this.notifyAll ();
                }
            }
        });
        synchronized (ListUrlController.this)
        {
            try
            {
                ListUrlController.this.wait ();
            }
            catch (final InterruptedException e)
            {
                Log.N (e);
            }
        }
        new Thread (this::refresh).start ();
        System.gc ();
    }

    public final static class LinkInformation extends DownloadList
    {
        private final String link;
        private String filename;
        private String status;

        public LinkInformation (final String link)
        {
            this.link = link;
        }

        public String getLink ()
        {
            return link;
        }

        public String getFilename ()
        {
            return filename;
        }

        public void setFilename (String filename)
        {
            this.filename = filename;
        }

        public String getStatus ()
        {
            return status;
        }

        public void setStatus (String status)
        {
            this.status = status;
        }
    }

}
