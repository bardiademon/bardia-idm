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
        else Log.N (new Exception ("Database not connected"));

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
        else Log.N (new Exception ("Database not connected"));

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
