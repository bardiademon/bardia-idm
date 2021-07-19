package com.bardiademon.controllers;

import com.bardiademon.Downloder.Download.Download;
import com.bardiademon.Downloder.Download.OnInfoLink;
import com.bardiademon.Main;
import com.bardiademon.bardiademon.GetSize;
import com.bardiademon.bardiademon.Log;
import com.bardiademon.bardiademon.ShowMessage;
import com.bardiademon.models.DownloadList.DownloadList;
import com.bardiademon.models.DownloadList.DownloadListService;
import com.bardiademon.models.Lists.Lists;
import com.bardiademon.models.Lists.ListsService;
import com.bardiademon.models.Lists.NameId;
import com.jfoenix.controls.JFXComboBox;
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
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

    @FXML
    public TextField txtListname;

    @FXML
    public Button btnSaveList;

    @FXML
    public JFXComboBox <NameId> listsName;

    @FXML
    public Button btnRemoveList;

    @FXML
    public Button btnGetList;


    private static final String TEXT_TOTAL_FILE_SIZE = "Total file size: :SIZE";
    private long totalFileSize = 0;
    @FXML
    public Text txtTotalFileSize;

    private Stage stage;

    private DownloadListService downloadListService;

    private File fileChooser;

    private List <LinkInformation> linkInformation;

    private ManagementDownloading managementDownloading;

    private ListsService listsService;

    private List <NameId> nameIds;

    private Lists lists;

    private int selectedItemNameId = -1;

    @Override
    public void initialize (final URL url , final ResourceBundle resourceBundle)
    {
        downloadListService = new DownloadListService ();
        listsService = new ListsService ();

        final TableColumn <LinkInformation, String> filename = new TableColumn <> ("Filename");
        filename.setCellValueFactory (new PropertyValueFactory <> ("filename"));

        final TableColumn <LinkInformation, String> size = new TableColumn <> ("Size");
        size.setCellValueFactory (new PropertyValueFactory <> ("size"));

        final TableColumn <LinkInformation, String> status = new TableColumn <> ("Status");
        status.setCellValueFactory (new PropertyValueFactory <> ("status"));

        Platform.runLater (() -> list.getColumns ().addAll (Arrays.asList (filename , size , status)));
        linkInformation = new ArrayList <> ();

        refreshNameIds ();

        txtPath.textProperty ().addListener ((observable , oldValue , newValue) -> btnSaveListSetDisable ());
        txtListname.textProperty ().addListener ((observable , oldValue , newValue) -> btnSaveListSetDisable ());
    }


    private void setTotalFileSize_Increase (final long size)
    {
        if (size <= 0) totalFileSize = 0;
        else totalFileSize += size;
        setTotalFileSize ();
    }

    private void setTotalFileSize_LowOff (final long size)
    {
        if (size >= totalFileSize) totalFileSize = 0;
        else totalFileSize = Math.abs (totalFileSize - size);
        setTotalFileSize ();
    }

    private void setTotalFileSize ()
    {
        Platform.runLater (() -> txtTotalFileSize.setText (TEXT_TOTAL_FILE_SIZE.replace (":SIZE" , GetSize.Get (totalFileSize))));
    }

    private void refresh ()
    {
        Platform.runLater (() ->
        {
            list.getItems ().clear ();
            list.getItems ().addAll (linkInformation);
        });
    }

    private void refreshNameIds ()
    {
        nameIds = listsService.getNameIds ();
        if (nameIds != null && nameIds.size () > 0)
        {
            Platform.runLater (() ->
            {
                listsName.getItems ().clear ();
                listsName.getItems ().addAll (nameIds);

                if (selectedItemNameId >= 0) listsName.getSelectionModel ().select (selectedItemNameId);
            });
        }
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
                for (final String line : lines)
                {
                    final List <String> strings = find (line);
                    if (!urls.containsAll (strings)) urls.addAll (strings);
                }

                if (urls.size () > 0)
                {
                    ShowMessage.Show (Alert.AlertType.INFORMATION , "Find" , "Several addresses were found from the selected file" , "Number of links found: " + urls.size ());
                    Platform.runLater (() ->
                    {
                        addToList (urls);
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
            try
            {
                setTotalFileSize_LowOff (linkInformation.get (selectedIndex).getByteSize ());
                linkInformation.remove (selectedIndex);
            }
            catch (final Exception e)
            {
                Log.N (e);
            }
            list.getItems ().remove (selectedIndex);
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

                managementDownloading = new ManagementDownloading (new ArrayList <> (linkInformation) , () ->
                {
                    Main.getMainController ().refresh ();
                    btnDownloadNow.setDisable (false);
                });

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
                    item.setLink (item.getLink ());
                    if (item.getId () > 0) downloadListService.modify (item);
                    else downloadListService.add (item);
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
        setTotalFileSize_LowOff (totalFileSize);
    }

    public static void Launch ()
    {
        Main.Launch ("ListUrl" , "Add list" , (Main.Controller <ListUrlController>) (controller , stage) -> controller.stage = stage);
    }

    private void addToList (final String link)
    {
        addToList (true , link);
    }

    private void addToList (final List <String> links)
    {
        addToList (true , links.toArray (new String[]{}));
    }

    private void addToList (final boolean save , final String... links)
    {
        new Thread (() ->
        {
            int start = (linkInformation.size () == 0 || !save ? 0 : linkInformation.size ());

            if (save)
            {
                for (final String link : links)
                {
                    if (!hasLink (link))
                    {
                        final LinkInformation linkInformation = new LinkInformation (link);
                        linkInformation.setFilename (link);
                        linkInformation.setStatus ("Please wait...");
                        ListUrlController.this.linkInformation.add (linkInformation);
                        Platform.runLater (() -> list.getItems ().add (linkInformation));
                    }
                }
            }
            else refresh ();

            System.gc ();

//            if (!save && start + 1 >= linkInformation.size ()) return;

            final Download download = new Download ();
            try
            {
                for (final String link : links) connect (save , start++ , download , link);
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

        if (file != null) setTxtPath (file.getAbsolutePath ());

        System.gc ();
    }

    private void setTxtPath (final String path)
    {
        final String oldPath = txtPath.getText ();

        Platform.runLater (() -> txtPath.setText (path));
        for (int i = 0, len = linkInformation.size (); i < len; i++)
        {
            final LinkInformation linkInformation = this.linkInformation.get (i);
            if (linkInformation.getPath () == null || linkInformation.getPath ().contains (oldPath))
            {
                linkInformation.setPath (String.format ("%s%s%s" , path , File.separator , linkInformation.getFilename ()));
                this.linkInformation.set (i , linkInformation);
            }
        }
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
                                    setTotalFileSize_LowOff (this.linkInformation.get (finalI).getByteSize ());
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
                        new Thread (() -> connect (false , index , new Download () , information.getLink ()));
                    });
                }
            }
            catch (final Exception e)
            {
                Log.N (e);
            }
        }
    }

    private void connect (final boolean save , final int index , final Download download , final String link)
    {
        linkInformation.get (index).setStatus ("Connecting...");
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
                    setTotalFileSize_Increase (Size);
                    ListUrlController.this.linkInformation.get (index).setSize (Size);
                    ListUrlController.this.linkInformation.get (index).setStatus ("Connected");
                }
                catch (final Exception e)
                {
                    ListUrlController.this.linkInformation.get (index).setStatus (e.getMessage ());
                    Log.N (e);
                }
                Platform.runLater (() -> list.getItems ().set (index , linkInformation.get (index)));
                return true;
            }

            @Override
            public void OnFilename (final String Filename)
            {
                try
                {
                    final String filename = URLDecoder.decode (Filename , StandardCharsets.UTF_8);
                    ListUrlController.this.linkInformation.get (index).setFilename (filename);
                    Platform.runLater (() -> list.getItems ().set (index , linkInformation.get (index)));
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
            public void OnPrint (final String Message)
            {
            }

            @Override
            public void OnCancelDownload ()
            {

            }

            @Override
            public void OnNewLink (final String Link)
            {

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
        if (save)
        {
            final LinkInformation linkInformation = this.linkInformation.get (index);
            if (linkInformation.getStatus ().toLowerCase (Locale.ROOT).contains ("connected"))
            {
                linkInformation.setId (downloadListService.addGetId (linkInformation));
                this.linkInformation.set (index , linkInformation);
            }
        }

        Main.getMainController ().refresh ();

        System.gc ();
    }

    @FXML
    public void onClickBtnSaveList ()
    {
        final String listname = txtListname.getText ();
        if (listname != null && !listname.isEmpty ())
        {
            if (linkInformation.size () > 0)
            {
                if ((lists != null && lists.getName ().equals (listname)) || !listsService.hasName (listname))
                {
                    if (lists == null)
                    {
                        lists = new Lists ();
                        lists.setPath (txtPath.getText ());
                        lists.setName (listname);

                        final long id = listsService.addGetId (lists);
                        lists.setId (id);

                        if (id > 0)
                        {
                            new Thread (() ->
                            {
                                for (int i = 0, len = ListUrlController.this.linkInformation.size (); i < len; i++)
                                {
                                    final LinkInformation linkInformation = ListUrlController.this.linkInformation.get (i);
                                    linkInformation.setListId (id);
                                    ListUrlController.this.linkInformation.set (i , linkInformation);
                                    ListUrlController.this.downloadListService.modify (linkInformation);
                                }
                            }).start ();
                            refreshNameIds ();
                            ShowMessage.Show (Alert.AlertType.INFORMATION , "Added" , "List added" , "List name: " + listname);
                        }
                    }
                    else
                    {
                        lists.setPath (txtPath.getText ());
                        lists.setName (listname);

                        if (listsService.modify (lists))
                        {
                            refreshNameIds ();
                            ShowMessage.Show (Alert.AlertType.INFORMATION , "Modify" , "The list changed" , "List name: " + listname);
                        }
                    }
                }
                else
                    ShowMessage.Show (Alert.AlertType.ERROR , "Listname error" , "The name of the list is duplicate" , "Please enter another name");
            }
            else
                ShowMessage.Show (Alert.AlertType.ERROR , "is empty" , "List is a empty" , "Please add list");
        }
        else ShowMessage.Show (Alert.AlertType.ERROR , "is empty" , "List name is empty" , "Please enter list name");
    }

    private void btnSaveListSetDisable ()
    {
        btnSaveList.setDisable (isEmpty (this.txtListname.getText ()) || isEmpty (this.txtPath.getText ()));
    }

    private boolean isEmpty (final String val)
    {
        return (val == null || val.isEmpty ());
    }

    @FXML
    public void onClickBtnGetList ()
    {
        if (nameIds != null && nameIds.size () > 0)
        {
            selectedItemNameId = listsName.getSelectionModel ().getSelectedIndex ();

            if (selectedItemNameId >= 0)
            {
                final NameId nameId = nameIds.get (selectedItemNameId);

                final Lists lists = listsService.getLists (nameId.getId ());

                if (lists != null)
                {
                    this.lists = lists;

                    Platform.runLater (list.getItems ()::clear);

                    linkInformation.clear ();
                    setTotalFileSize_LowOff (totalFileSize);
                    System.gc ();

                    setTxtPath (lists.getPath ());
                    Platform.runLater (() -> txtListname.setText (lists.getName ()));

                    final List <DownloadList> downloadLists = lists.getDownloadLists ();

                    if (downloadLists != null && downloadLists.size () > 0)
                    {
                        final String[] urls = new String[downloadLists.size ()];
                        for (int i = 0, downloadListsSize = downloadLists.size (); i < downloadListsSize; i++)
                        {
                            final DownloadList downloadList = downloadLists.get (i);
                            urls[i] = downloadList.getLink ();
                            linkInformation.add (new LinkInformation (downloadList));
                        }


                        addToList (false , urls);
                    }

                }
            }
        }
    }

    @FXML
    public void onClickBtnRemoveList ()
    {
        selectedItemNameId = listsName.getSelectionModel ().getSelectedIndex ();
        if (selectedItemNameId >= 0 && lists != null && lists.getName ().equals (listsName.getItems ().get (selectedItemNameId).getName ()))
        {
            final String listname = lists.getName ();

            if (listsService.remove (lists.getId ()) && downloadListService.removeList (lists.getId ()))
            {
                linkInformation.clear ();
                listsName.getItems ().remove (selectedItemNameId);
                selectedItemNameId = -1;
                lists = null;
                list.getItems ().clear ();
                setTxtPath ("");
                txtListname.setText ("");

                setTotalFileSize_LowOff (totalFileSize);

                Main.getMainController ().refresh ();

                System.gc ();

                ShowMessage.Show (Alert.AlertType.INFORMATION , "Removed" , "The list has been deleted" , '"' + listname + "\" was removed");
            }
            else
                ShowMessage.Show (Alert.AlertType.ERROR , "Error" , "Error deleting list" , "List name: \"" + listname + '"');

        }
    }

    @FXML
    public void onClickListname ()
    {
        selectedItemNameId = listsName.getSelectionModel ().getSelectedIndex ();

        final boolean disable = nameIds == null || nameIds.size () == 0 || selectedItemNameId < 0;
        btnRemoveList.setDisable (disable);
        btnGetList.setDisable (disable);
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

        public LinkInformation (final DownloadList downloadList)
        {
            this.link = downloadList.getLink ();
            setId (downloadList.getId ());
            setFilename (FilenameUtils.getBaseName (downloadList.getPath ()));
            setPath (downloadList.getPath ());
            setCreatedDir (downloadList.isCreatedDir ());
            setTheNameHasNoSuffix (downloadList.isTheNameHasNoSuffix ());
            setSize (downloadList.getByteSize ());
            setStatus ("Please wait...");
            setCompleted (downloadList.isCompleted ());
            setDescription (downloadList.getDescription ());
            setListId (downloadList.getListId ());
            setEndAt (downloadList.getEndAt ());
            setTime (downloadList.getTime ());
            setStartedAt (downloadList.getStartedAt ());
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
