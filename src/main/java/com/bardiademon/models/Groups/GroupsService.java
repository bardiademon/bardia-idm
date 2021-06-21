package com.bardiademon.models.Groups;

import com.bardiademon.Main;
import com.bardiademon.bardiademon.Log;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class GroupsService
{
    private static final String TABLE_NAME = "groups", TABLE_NAME_EXTENSIONS = "group_extensions";

    public enum ColumnsNames
    {
        id, name, default_path
    }

    public enum ColumnsNamesGroupExtensions
    {
        id, extension, group_id
    }

    public List <Groups> getGroups ()
    {
        if (Main.Database ().connected ())
        {
            if (countAll () > 0)
            {
                try (final Statement statement = Main.Database ().getConnection ().createStatement (ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY); ResultSet resultSet = statement.executeQuery (makeQueryFindAllGroup ()))
                {
                    final List <Groups> groups = new ArrayList <> ();

                    while (resultSet.next ())
                    {
                        final Groups group = new Groups ();
                        group.setId (resultSet.getLong (ColumnsNames.id.name ()));
                        group.setName (resultSet.getString (ColumnsNames.name.name ()));
                        group.setDefaultPath (resultSet.getString (ColumnsNames.default_path.name ()));
                        final List <String> allExtension = findAllExtension (group.getId ());
                        if (allExtension != null) group.setExtensions (allExtension);
                        groups.add (group);
                    }
                    return groups;
                }
                catch (final SQLException e)
                {
                    Log.N (e);
                }
            }
        }
        else Log.N (new Exception (Log.DATABASE_NOT_CONNECTED));

        return null;
    }

    public List <String> findAllExtension (final long groupId)
    {
        if (countFindAllExtension (groupId) > 0)
        {
            ResultSet resultSet = null;
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryFindAllExtension () , ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY))
            {
                statement.setLong (1 , groupId);
                resultSet = statement.executeQuery ();
                final List <String> extensions = new ArrayList <> ();

                while (resultSet.next ())
                    extensions.add (resultSet.getString (ColumnsNamesGroupExtensions.extension.name ()));

                return extensions;
            }
            catch (final SQLException e)
            {
                Log.N (e);
            }
            finally
            {
                if (resultSet != null)
                {
                    try
                    {
                        resultSet.close ();
                    }
                    catch (final SQLException e)
                    {
                        Log.N (e);
                    }
                }
            }
        }
        return null;
    }

    public long countAll ()
    {
        if (Main.Database ().connected ())
        {
            try (final Statement statement = Main.Database ().getConnection ().createStatement (ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY);
                 final ResultSet resultSet = statement.executeQuery (makeQueryCountFindAll ()))
            {
                return (resultSet.getInt (1));
            }
            catch (final SQLException e)
            {
                Log.N (e);
            }
        }
        else Log.N (new Exception (Log.DATABASE_NOT_CONNECTED));

        return 0;
    }

    public long countFindAllExtension (final long groupId)
    {
        if (Main.Database ().connected ())
        {
            ResultSet resultSet = null;
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryCountFindAllExtension () , ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY))
            {
                statement.setLong (1 , groupId);
                resultSet = statement.executeQuery ();
                return (resultSet.getInt (1));
            }
            catch (final SQLException e)
            {
                Log.N (e);
            }
            finally
            {
                if (resultSet != null)
                {
                    try
                    {
                        resultSet.close ();
                    }
                    catch (final SQLException e)
                    {
                        Log.N (e);
                    }
                }
            }
        }
        else Log.N (new Exception ("Database not connected"));

        return 0;
    }

    public long add (final Groups group)
    {
        if (Main.Database ().connected ())
        {
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryAdd ()))
            {
                statement.setString (1 , group.getName ());
                statement.setString (2 , group.getDefaultPath ());
                statement.executeUpdate ();

                try (final ResultSet generatedKeys = statement.getGeneratedKeys ())
                {
                    final long groupId = generatedKeys.getLong (1);

                    if (groupId > 0)
                    {
                        group.setId (groupId);

                        final List <String> extensions = group.getExtensions ();

                        final int size;
                        if (extensions != null && (size = extensions.size ()) > 0)
                        {
                            try (final PreparedStatement statementExtension = Main.Database ().getConnection ().prepareStatement (makeQueryAddExtension (size)))
                            {
                                int counter = 0;
                                for (int i = 0; i < size; i++)
                                {
                                    statementExtension.setString (++counter , extensions.get (i));
                                    statementExtension.setLong (++counter , groupId);
                                }
                                statementExtension.executeUpdate ();

                            }
                        }
                        return groupId;
                    }
                }
            }
            catch (final SQLException e)
            {
                Log.N (e);
            }
        }
        else Log.N (new Exception (Log.DATABASE_NOT_CONNECTED));

        return 0;
    }

    public long addExtension (final String extension , final long groupId)
    {
        if (Main.Database ().connected ())
        {
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryAddExtension ()))
            {
                statement.setString (1 , extension);
                statement.setLong (2 , groupId);

                if (statement.executeUpdate () > 0)
                {
                    try (final ResultSet generatedKeys = statement.getGeneratedKeys ())
                    {
                        return generatedKeys.getLong (1);
                    }
                }
            }
            catch (final SQLException e)
            {
                Log.N (e);
            }
        }
        else Log.N (new Exception (Log.DATABASE_NOT_CONNECTED));

        return 0;
    }

    public boolean removeExtension (final String extension , final long groupId)
    {
        if (Main.Database ().connected ())
        {
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryRemoveSingleExtension ()))
            {
                statement.setLong (1 , groupId);
                statement.setString (2 , extension);

                return (statement.executeUpdate () > 0);
            }
            catch (final SQLException e)
            {
                Log.N (e);
            }
        }
        else Log.N (new Exception (Log.DATABASE_NOT_CONNECTED));

        return false;
    }

    public String getPath (final String extension)
    {
        if (Main.Database ().connected ())
        {
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryGetPath ()))
            {
                statement.setString (1 , extension);
                try (final ResultSet resultSet = statement.executeQuery ())
                {
                    if (resultSet.next ())
                        return getPathGroup (resultSet.getLong (1));
                }
            }
            catch (final SQLException e)
            {
                Log.N (e);
            }
        }
        else Log.N (Log.DATABASE_NOT_CONNECTED);

        return null;
    }

    private String getPathGroup (final long groupId)
    {
        if (Main.Database ().connected ())
        {
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryGetPathGroup ()))
            {
                statement.setLong (1 , groupId);
                try (final ResultSet resultSet = statement.executeQuery ())
                {
                    if (resultSet.next ()) return resultSet.getString (1);
                }
            }
            catch (final SQLException e)
            {
                Log.N (e);
            }
        }
        else Log.N (Log.DATABASE_NOT_CONNECTED);

        return null;
    }

    public long getGroupId (final String groupname)
    {
        if (Main.Database ().connected ())
        {
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryGroupId ()))
            {
                statement.setString (1 , groupname);

                try (final ResultSet resultSet = statement.executeQuery ())
                {
                    if (resultSet.next ()) return resultSet.getLong (ColumnsNames.id.name ());
                }
            }
            catch (final SQLException e)
            {
                Log.N (e);
            }
        }
        else Log.N (Log.DATABASE_NOT_CONNECTED);

        return 0;
    }

    private String makeQueryGroupId ()
    {
        return String.format ("select \"%s\" from \"%s\" where \"%s\" = ?" ,
                ColumnsNames.id , TABLE_NAME , ColumnsNames.name);
    }

    private String makeQueryAddExtension ()
    {
        return String.format ("insert into \"%s\"(\"%s\",\"%s\",\"%s\") values (null,?,?)" ,
                TABLE_NAME_EXTENSIONS , ColumnsNamesGroupExtensions.id , ColumnsNamesGroupExtensions.extension , ColumnsNamesGroupExtensions.group_id);
    }

    private String makeQueryRemoveSingleExtension ()
    {
        return String.format ("delete from \"%s\" where \"%s\" = ? and \"%s\" = ?" ,
                TABLE_NAME_EXTENSIONS , ColumnsNamesGroupExtensions.group_id , ColumnsNamesGroupExtensions.extension);
    }

    private String makeQueryGetPath ()
    {
        return String.format ("select \"%s\" from \"%s\" where \"%s\" = ?" ,
                ColumnsNamesGroupExtensions.group_id , TABLE_NAME_EXTENSIONS , ColumnsNamesGroupExtensions.extension);
    }

    private String makeQueryGetPathGroup ()
    {
        return String.format ("select \"%s\" from \"%s\" where \"%s\" = ?" ,
                ColumnsNames.default_path , TABLE_NAME , ColumnsNames.id);
    }

    private String makeQueryAdd ()
    {
        return String.format ("insert into \"%s\"(\"%s\",\"%s\",\"%s\") values (null,?,?)" ,
                TABLE_NAME ,
                ColumnsNames.id , ColumnsNames.name , ColumnsNames.default_path
        );
    }

    private String makeQueryAddExtension (final int count)
    {
        final String values = "(mull,?,?)";
        final StringBuilder addExtension = new StringBuilder (String.format ("insert into \"%s\"(\"%s\",\"%s\",\"%s\") values " ,
                TABLE_NAME_EXTENSIONS ,
                ColumnsNamesGroupExtensions.id , ColumnsNamesGroupExtensions.extension , ColumnsNamesGroupExtensions.group_id
        ));
        if (count > 0)
        {
            for (int i = 0; i < count; i++)
            {
                addExtension.append (values);
                if (i + 1 < count) addExtension.append (",");
            }
        }

        return addExtension.toString ();
    }

    public boolean remove (final long groupId)
    {
        return remove (makeQueryRemoveGroup () , groupId) && (countFindAllExtension (groupId) == 0 || remove (makeQueryRemoveExtension () , groupId));
    }

    private boolean remove (final String query , final long groupId)
    {
        if (Main.Database ().connected ())
        {
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (query))
            {
                statement.setLong (1 , groupId);
                return statement.executeUpdate () > 0;
            }
            catch (final SQLException e)
            {
                Log.N (e);
            }
        }
        else Log.N (Log.DATABASE_NOT_CONNECTED);

        return false;
    }

    private String makeQueryRemoveGroup ()
    {
        return String.format ("delete from \"%s\" where \"%s\" = ?" ,
                TABLE_NAME , ColumnsNames.id);
    }

    private String makeQueryRemoveExtension ()
    {
        return String.format ("delete from \"%s\" where \"%s\" = ?" ,
                TABLE_NAME_EXTENSIONS , ColumnsNamesGroupExtensions.group_id);
    }

    private String makeQueryFindAllGroup ()
    {
        return String.format ("select * from \"%s\"" , TABLE_NAME);
    }

    private String makeQueryFindAllExtension ()
    {
        return String.format ("select * from \"%s\" where \"%s\"=?" , TABLE_NAME_EXTENSIONS , ColumnsNamesGroupExtensions.group_id.name ());
    }

    private String makeQueryCountFindAllExtension ()
    {
        return makeQueryFindAllExtension ().replace ("*" , "count(*)");
    }

    private String makeQueryCountFindAll ()
    {
        return makeQueryFindAllGroup ().replace ("*" , "count(*)");
    }
}
