package com.bardiademon.controllers;

import com.bardiademon.Main;
import com.bardiademon.bardiademon.Log;
import com.bardiademon.bardiademon.ShowMessage;
import com.bardiademon.models.Groups.Groups;
import com.bardiademon.models.Groups.GroupsService;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public final class GroupsController implements Initializable
{
    @FXML
    public JFXComboBox <Groups> listGroupname;

    @FXML
    public JFXListView <String> listExtension;

    @FXML
    public Button btnAddSelectedGroup;

    @FXML
    public Button btnRemoveSelectedItemGroup;

    @FXML
    public Button btnChangeSelectedItemGroup;

    @FXML
    public Button btnAddGroup;

    @FXML
    public Button btnChangeGroup;

    @FXML
    public Button btnRemoveGroup;

    @FXML
    public TextField txtSelectedExtension;

    @FXML
    public TextField txtSelectedGroupname;

    private List <Groups> groups;
    private List <String> extensions;

    private GroupsService groupsService;
    private int selectedGroupname = -1;

    private static final String TXT_ADD_NEW_GROUP_NAME = "ADD NEW:";

    private Stage stage;

    @Override
    public void initialize (final URL url , final ResourceBundle resourceBundle)
    {
        groupsService = new GroupsService ();
    }

    @FXML
    public void onClickBtnAddSelectedGroup ()
    {
        if (selectedGroupname >= 0)
        {
            final String extension = txtSelectedExtension.getText ();
            if (extension != null && !extension.isEmpty ())
            {
                if (groupsService.getPath (extension) == null)
                {
                    final Groups group = this.groups.get (selectedGroupname);
                    final long extensionId = groupsService.addExtension (extension , group.getId ());
                    if (extensionId > 0)
                    {
                        List <String> extensions = group.getExtensions ();
                        if (extensions == null) extensions = new ArrayList <> ();
                        extensions.add (extension);
                        group.setExtensions (extensions);
                        groups.set (selectedGroupname , group);
                        selectedGroupname = -1;
                        refreshGroupname ();
                        ShowMessage.Show (Alert.AlertType.INFORMATION , "Added extension" , "Extension was added" , "Extension: " + extension);
                    }
                    else errorAddRemoveExtension (extension , "adding");
                }
                else
                    ShowMessage.Show (Alert.AlertType.ERROR , "Error add extension" , "This extension is exists" , "Extension: " + extension);
            }
        }
    }

    @FXML
    public void onClickBtnRemoveSelectedItemGroup ()
    {
        final int selectedIndex = listExtension.getSelectionModel ().getSelectedIndex ();
        if (selectedIndex >= 0 && selectedGroupname >= 0)
        {
            final Groups group = this.groups.get (selectedGroupname);

            final String extension = group.getExtensions ().get (selectedIndex);
            if (groupsService.removeExtension (extension , group.getId ()))
            {
                group.getExtensions ().remove (selectedIndex);
                selectedGroupname = -1;
                refreshExtension ();
                ShowMessage.Show (Alert.AlertType.INFORMATION , "Removed extension" , "Extension was removed" , "Extension: " + extension);
            }
            else errorAddRemoveExtension (extension , "Removing");
        }
    }

    private void errorAddRemoveExtension (final String extension , final String addRemove)
    {
        ShowMessage.Show (Alert.AlertType.ERROR , "Error " + addRemove + " extension" , "An error occurred" , "Extension: " + extension);
    }

    @FXML
    public void onClickBtnChangeSelectedItemGroup ()
    {

    }

    @FXML
    public void onClickBtnAddGroup ()
    {
        String groupname = txtSelectedGroupname.getText ();
        if (groupname.startsWith (TXT_ADD_NEW_GROUP_NAME))
        {
            try
            {
                groupname = groupname.split (TXT_ADD_NEW_GROUP_NAME)[1];
                if (groupname != null && !(groupname = groupname.trim ()).isEmpty ())
                {
                    if (groupsService.getGroupId (groupname) == 0)
                    {
                        final DirectoryChooser chooser = new DirectoryChooser ();
                        chooser.setTitle ("Choose default path");
                        final File file = chooser.showDialog (null);
                        if (file != null && file.exists ())
                        {
                            final Groups group = new Groups ();
                            group.setName (groupname);
                            group.setDefaultPath (file.getAbsolutePath ());

                            final long groupId;
                            if ((groupId = groupsService.add (group)) > 0)
                            {
                                group.setId (groupId);

                                groups.add (group);

                                refreshGroupname (true);

                                ShowMessage.Show (Alert.AlertType.INFORMATION , "Added groupname" , "New group added" , "Group name: " + groupname);
                            }
                            else
                                ShowMessage.Show (Alert.AlertType.ERROR , "Error added groupname" , "An error occurred while adding the group" , "Group name: " + groupname);
                        }
                        else throw new Exception ("Error selected file " + file);
                    }
                    else
                        ShowMessage.Show (Alert.AlertType.INFORMATION , "Exists group name" , "This group name has exists" , "Group name: " + groupname);
                }
                else throw new Exception ("Group name is empty");
            }
            catch (final Exception e)
            {
                Log.N (e);
            }
        }
        else
        {
            ShowMessage.Show (Alert.AlertType.INFORMATION , "Not started " + TXT_ADD_NEW_GROUP_NAME , "To add, first enter the phrase opposite, then enter the name: " + TXT_ADD_NEW_GROUP_NAME ,
                    "Example : " + TXT_ADD_NEW_GROUP_NAME + groupname);

            Platform.runLater (() -> txtSelectedGroupname.setText (TXT_ADD_NEW_GROUP_NAME + " "));
        }
    }

    @FXML
    public void onClickBtnChangeGroup ()
    {

    }

    @FXML
    public void onClickBtnRemoveGroup ()
    {
        if (selectedGroupname >= 0)
        {
            final Groups group = this.groups.get (selectedGroupname);
            if (groupsService.remove (group.getId ()))
            {
                this.groups.remove (selectedGroupname);

                if (extensions != null && extensions.size () > 0) extensions.clear ();

                Platform.runLater (() ->
                {
                    listGroupname.getItems ().remove (selectedGroupname);
                    selectedGroupname = -1;
                    refreshGroupname ();
                });

                ShowMessage.Show (Alert.AlertType.INFORMATION , "Delete group" , "Group was removed" , "Group name: " + group.getName ());
            }
            else
                ShowMessage.Show (Alert.AlertType.ERROR , "Error deleting group" , "There was an error deleting the group" , "Group name: " + group.getName ());

        }
    }

    @FXML
    public void onClickBtnCLose ()
    {
        Platform.runLater (() ->
        {
            stage.close ();
            stage.hide ();
            System.gc ();
        });
    }

    public static void Launch (final List <Groups> groups)
    {
        Platform.runLater (() -> Main.Launch ("Groups" , "Groups" , (Main.Controller <GroupsController>) (controller , stage) ->
        {
            controller.groups = groups;
            controller.stage = stage;
            controller.load ();
        }));
    }

    private void load ()
    {
        if (groups == null) groups = groupsService.getGroups ();
        if (groups != null && groups.size () > 0)
        {
            refreshGroupname ();
        }
    }

    private void refreshGroupname ()
    {
        refreshGroupname (false);
    }

    private void refreshGroupname (final boolean add)
    {
        Platform.runLater (() ->
        {
            int selectedIndex;
            selectedIndex = listGroupname.getSelectionModel ().getSelectedIndex ();

            listGroupname.getItems ().clear ();

            final boolean isNullGroup = groups == null || groups.size () == 0;

            btnAddSelectedGroup.setDisable (isNullGroup);
            btnChangeGroup.setDisable (isNullGroup);
            btnRemoveGroup.setDisable (isNullGroup);

            if (!isNullGroup)
            {
                listGroupname.getItems ().addAll (groups);

                if (add) selectedIndex = listGroupname.getItems ().size () - 1;
                else if (selectedIndex < 0) selectedIndex = 0;

                listGroupname.getSelectionModel ().select (selectedIndex);

                onClickListGroupname ();
            }
        });
    }

    private void refreshExtension ()
    {
        Platform.runLater (() ->
        {
            listExtension.getItems ().clear ();
            txtSelectedExtension.setText ("");
            if (extensions != null && extensions.size () > 0)
                listExtension.getItems ().addAll (extensions);
        });
    }

    @FXML
    public void onClickListGroupname ()
    {
        final int selectedIndex = listGroupname.getSelectionModel ().getSelectedIndex ();
        if (selectedIndex >= 0 && selectedIndex != selectedGroupname)
        {
            btnRemoveSelectedItemGroup.setDisable (true);
            btnChangeSelectedItemGroup.setDisable (true);
            try
            {
                selectedGroupname = selectedIndex;
                extensions = groups.get (selectedGroupname).getExtensions ();

                Platform.runLater (() -> txtSelectedGroupname.setText (groups.get (selectedGroupname).getName ()));

                refreshExtension ();
            }
            catch (final Exception e)
            {
                Log.N (e);
            }
        }
    }

    @FXML
    public void onClickListExtension ()
    {
        final int selectedIndex = listExtension.getSelectionModel ().getSelectedIndex ();
        final boolean selected = selectedIndex >= 0;

        btnRemoveSelectedItemGroup.setDisable (!selected);
        btnChangeSelectedItemGroup.setDisable (!selected);

        if (selected) Platform.runLater (() -> txtSelectedExtension.setText (extensions.get (selectedIndex)));
    }
}
