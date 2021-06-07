package com.bardiademon.models.DownloadList;

import com.bardiademon.Main;
import com.bardiademon.bardiademon.Log;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class DownloadListService
{
    private static final String TABLE_NAME = "download_list";

    public enum ColumnsNames
    {
        id, started_at, end_at, completed, path, created_dir, size, time, link
    }

    public boolean add (final DownloadList downloadList)
    {
        if (Main.Database ().connected ())
        {
            if (downloadList.getLink () != null && downloadList.getStartedAt () != null && downloadList.getByteSize () > 0)
            {
                try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryAdd ()))
                {
                    int counter = 0;
                    statement.setString (++counter , downloadList.getLink ());
                    statement.setString (++counter , downloadList.getPath ());
                    statement.setBoolean (++counter , downloadList.isCreatedDir ());
                    statement.setTimestamp (++counter , Timestamp.valueOf (downloadList.getStartedAt ()));
                    statement.setLong (++counter , downloadList.getByteSize ());

                    Log.N ("Added");

                    return (statement.executeUpdate () > 0);
                }
                catch (final SQLException e)
                {
                    Log.N (e);
                }
            }
            else Log.N (new Exception ("check (link,startedAt,size)"));
        }
        else Log.N (new Exception ("Database not connected"));

        return false;
    }

    public List <DownloadList> findAll ()
    {
        if (Main.Database ().connected ())
        {
            if (getCount () > 0)
            {
                try (final Statement statement = Main.Database ().getConnection ().createStatement (ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY); final ResultSet resultSet = statement.executeQuery (makeQueryFindAll ()))
                {
                    final List <DownloadList> downloadLists = new ArrayList <> ();
                    while (resultSet.next ()) downloadLists.add (getDownloadList (resultSet));

                    Log.N ("Get List<DownloadList> => size: " + downloadLists.size () + " toString(" + downloadLists + ")");

                    return downloadLists;
                }
                catch (final SQLException e)
                {
                    Log.N (e);
                }
            }
            else Log.N ("Is empty download list");
        }
        else Log.N (new Exception ("Database not connected"));

        return null;
    }

    private DownloadList getDownloadList (final ResultSet resultSet) throws SQLException
    {
        final DownloadList downloadList = new DownloadList ();

        downloadList.setId (resultSet.getLong (ColumnsNames.id.name ()));
        downloadList.setLink (resultSet.getString (ColumnsNames.link.name ()));
        downloadList.setPath (resultSet.getString (ColumnsNames.path.name ()));
        downloadList.setCreatedDir (resultSet.getBoolean (ColumnsNames.created_dir.name ()));

        final Timestamp startedAt = resultSet.getTimestamp (ColumnsNames.started_at.name ());
        downloadList.setStartedAt (((startedAt == null) ? null : startedAt.toLocalDateTime ()));

        downloadList.setCompleted (resultSet.getBoolean (ColumnsNames.completed.name ()));


        final Timestamp time = resultSet.getTimestamp (ColumnsNames.time.name ());
        downloadList.setTime (((time == null) ? null : time.toLocalDateTime ()));

        final Timestamp endAt = resultSet.getTimestamp (ColumnsNames.end_at.name ());
        downloadList.setEndAt (((endAt == null) ? null : endAt.toLocalDateTime ()));

        downloadList.setSize (resultSet.getLong (ColumnsNames.size.name ()));

        return downloadList;
    }

    public DownloadList findById (final long id)
    {
        if (Main.Database ().connected ())
        {
            ResultSet resultSet = null;
            if (getCountFindById (id) > 0)
            {
                try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryFindById () , ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY))
                {
                    statement.setLong (1 , id);
                    resultSet = statement.executeQuery ();
                    return getDownloadList (resultSet);
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
        }
        else Log.N (new Exception ("Database not connected"));

        return null;
    }

    public int getCount ()
    {
        if (Main.Database ().connected ())
        {
            try (final Statement statement = Main.Database ().getConnection ().createStatement (ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY); final ResultSet resultSet = statement.executeQuery (makeQueryCount ()))
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

    public int getCountFindById (final long id)
    {
        if (Main.Database ().connected ())
        {
            ResultSet resultSet = null;
            try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryCountFindById () , ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY))
            {
                statement.setLong (1 , id);
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

    public String makeQueryAdd ()
    {
        return String.format (

                "insert into \"%s\" (\"%s\",\"%s\",\"%s\",\"%s\",\"%s\") values (?,?,?,?,?)" ,
                TABLE_NAME ,
                ColumnsNames.link.name () , ColumnsNames.path.name () ,
                ColumnsNames.created_dir.name () , ColumnsNames.started_at.name () ,
                ColumnsNames.size.name ()
        );
    }

    public boolean modify (final DownloadList downloadList)
    {
        if (downloadList != null && downloadList.getId () > 0)
        {
            if (Main.Database ().connected ())
            {
                try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryModify () , ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_UPDATABLE))
                {
                    int counter = 0;
                    statement.setString (++counter , downloadList.getLink ());
                    statement.setString (++counter , downloadList.getPath ());
                    statement.setTimestamp (++counter , Timestamp.valueOf (downloadList.getEndAt ()));
                    statement.setBoolean (++counter , downloadList.isCompleted ());
                    statement.setBoolean (++counter , downloadList.isCreatedDir ());
                    statement.setTimestamp (++counter , Timestamp.valueOf (downloadList.getTime ()));

                    // where
                    statement.setLong (++counter , downloadList.getId ());

                    return (statement.executeUpdate () > 0);
                }
                catch (final SQLException e)
                {
                    Log.N (e);
                }
            }
            else Log.N (new Exception ("Database not connected"));
        }
        else Log.N (new Exception ("downloadList != null && downloadList.getId () > 0"));

        return false;
    }

    private String makeQueryModify ()
    {
        return String.format ("update \"%s\" set \"%s\"=? , \"%s\"=? , \"%s\"=? , \"%s\"=? , \"%s\"=? where \"%s\"=?" ,
                TABLE_NAME ,
                ColumnsNames.link , ColumnsNames.path , ColumnsNames.end_at , ColumnsNames.completed , ColumnsNames.created_dir , ColumnsNames.time ,
                ColumnsNames.id);
    }

    public String makeQueryFindAll ()
    {
        return String.format ("select * from \"%s\"" , TABLE_NAME);
    }

    public String makeQueryFindById ()
    {
        return String.format ("select * from \"%s\" where \"%s\"=?" , TABLE_NAME , ColumnsNames.id);
    }

    public String makeQueryCountFindById ()
    {
        return makeQueryFindById ().replace ("*" , "count(*)");
    }

    public String makeQueryCount ()
    {
        return makeQueryFindAll ().replace ("*" , "count(*)");
    }
}
