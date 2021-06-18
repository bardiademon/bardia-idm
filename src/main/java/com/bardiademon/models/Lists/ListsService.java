package com.bardiademon.models.Lists;

import com.bardiademon.Main;
import com.bardiademon.bardiademon.Log;
import com.bardiademon.models.DownloadList.DownloadListService;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ListsService
{
    private static final String TABLE_NAME = "lists";

    private final DownloadListService downloadListService;

    public ListsService ()
    {
        this.downloadListService = new DownloadListService ();
    }

    public enum ColumnsNames
    {
        id, name, path, created_at,
    }

    public long addGetId (final Lists lists)
    {
        if (Main.Database ().connected ())
        {
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryAdd () , Statement.RETURN_GENERATED_KEYS))
            {
                int counter = 0;
                statement.setString (++counter , lists.getName ());
                statement.setString (++counter , lists.getPath ());
                statement.setTimestamp (++counter , Timestamp.valueOf (LocalDateTime.now ()));
                final boolean add = statement.executeUpdate () > 0;
                if (add)
                {
                    try (final ResultSet generatedKeys = statement.getGeneratedKeys ())
                    {
                        return generatedKeys.getLong (1);
                    }
                    catch (final SQLException e)
                    {
                        Log.N (e);
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

    public boolean modify (final Lists lists)
    {
        if (Main.Database ().connected ())
        {
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryModify ()))
            {
                int counter = 0;
                statement.setString (++counter , lists.getName ());
                statement.setString (++counter , lists.getPath ());

                // where
                statement.setLong (++counter , lists.getId ());

                return statement.executeUpdate () > 0;
            }
            catch (final SQLException e)
            {
                Log.N (e);
            }
        }
        else Log.N (new Exception (Log.DATABASE_NOT_CONNECTED));

        return false;
    }

    public boolean remove (final long id)
    {
        if (Main.Database ().connected ())
        {
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryRemove ()))
            {
                // where
                statement.setLong (1 , id);
                return statement.executeUpdate () > 0;
            }
            catch (final SQLException e)
            {
                Log.N (e);
            }
        }
        else Log.N (new Exception (Log.DATABASE_NOT_CONNECTED));

        return false;
    }

    public List <NameId> getNameIds ()
    {
        if (Main.Database ().connected ())
        {
            if (getCount (makeQueryCountGetNameIds ()) > 0)
            {
                try (final Statement statement = Main.Database ().getConnection ().createStatement (ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY); final ResultSet resultSet = statement.executeQuery (makeQueryGetNameIds ()))
                {
                    final List <NameId> nameIds = new ArrayList <> ();
                    while (resultSet.next ())
                    {
                        final NameId nameId = new NameId ();
                        nameId.setId (resultSet.getLong (ColumnsNames.id.name ()));
                        nameId.setName (resultSet.getString (ColumnsNames.name.name ()));
                        nameIds.add (nameId);
                    }

                    return nameIds;
                }
                catch (final SQLException e)
                {
                    Log.N (e);
                }
            }
            else Log.N ("Not found list");
        }
        else Log.N (new Exception (Log.DATABASE_NOT_CONNECTED));

        return null;
    }

    public Lists getLists (final long id)
    {
        if (Main.Database ().connected ())
        {
            System.out.println (makeQueryGetLists ().replace ("?" , String.valueOf (id)));
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryGetLists () , ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY))
            {
                statement.setLong (1 , id);

                try (final ResultSet resultSet = statement.executeQuery ())
                {
                    if (resultSet.next ())
                    {
                        final Lists list = new Lists ();
                        list.setId (resultSet.getLong (ColumnsNames.id.name ()));
                        list.setName (resultSet.getString (ColumnsNames.name.name ()));

                        list.setPath (resultSet.getString (ColumnsNames.path.name ()));
                        list.setCreatedAt (resultSet.getTimestamp (ColumnsNames.created_at.name ()).toLocalDateTime ());

                        list.setDownloadLists (downloadListService.findByListId (list.getId ()));

                        return list;
                    }
                }
            }
            catch (final SQLException e)
            {
                e.printStackTrace ();
                Log.N (e);
            }
        }
        else Log.N (new Exception (Log.DATABASE_NOT_CONNECTED));

        return null;
    }

    public boolean hasName (final String name)
    {
        if (Main.Database ().connected ())
        {
            ResultSet resultSet = null;
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryHasName ()))
            {
                statement.setString (1 , name);
                resultSet = statement.executeQuery ();
                return resultSet.getLong (1) > 0;
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
        else Log.N ("Not found list");

        return false;
    }

    private String makeQueryAdd ()
    {
        return String.format ("insert into \"%s\" (\"%s\",\"%s\",\"%s\",\"%s\") values (null,?,?,?)" ,
                TABLE_NAME , ColumnsNames.id.name () , ColumnsNames.name.name () , ColumnsNames.path.name () , ColumnsNames.created_at.name ());
    }

    private String makeQueryModify ()
    {
        return String.format ("update \"%s\" set \"%s\" = ? , \"%s\" = ? where \"%s\" = ?" ,
                TABLE_NAME , ColumnsNames.name.name () , ColumnsNames.path.name () , ColumnsNames.id.name ());
    }

    private String makeQueryRemove ()
    {
        return String.format ("delete from \"%s\" where \"%s\" = ?" ,
                TABLE_NAME , ColumnsNames.id.name ());
    }

    private long getCount (final String query)
    {
        if (Main.Database ().connected ())
        {
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (query); final ResultSet resultSet = statement.executeQuery ())
            {
                return resultSet.getLong (1);
            }
            catch (final SQLException e)
            {
                Log.N (e);
            }
        }
        else Log.N (new Exception (Log.DATABASE_NOT_CONNECTED));

        return 0;
    }

    private String makeQueryGetNameIds ()
    {
        return String.format ("select \"%s\",\"%s\" from \"%s\"" , ColumnsNames.id , ColumnsNames.name , TABLE_NAME);
    }

    private String makeQueryGetLists ()
    {
        return String.format ("select * from \"%s\" where \"%s\" = ?" , TABLE_NAME , ColumnsNames.id.name ());
    }

    private String makeQueryCountGetLists ()
    {
        return makeQueryGetLists ().replace ("*" , "count(*)");
    }

    private String makeQueryHasName ()
    {
        return String.format ("select count(\"%s\") from \"%s\" where \"%s\" = ?" , ColumnsNames.id , TABLE_NAME , ColumnsNames.name);
    }

    private String makeQueryCountGetNameIds ()
    {
        return String.format ("select count(\"%s\") from \"%s\"" , ColumnsNames.id , TABLE_NAME);
    }
}
