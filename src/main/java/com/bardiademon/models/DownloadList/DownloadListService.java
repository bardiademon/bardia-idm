package com.bardiademon.models.DownloadList;

import com.bardiademon.Main;
import com.bardiademon.bardiademon.Log;
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
            if (downloadList.getLink () != null && downloadList.getStartedAt () != null && downloadList.getSize () > 0)
            {
                try (final PreparedStatement statement = Main.Database ().getConnection ().prepareStatement (makeQueryAdd ()))
                {
                    int counter = 0;
                    statement.setString (++counter , downloadList.getLink ());
                    statement.setString (++counter , downloadList.getPath ());
                    statement.setBoolean (++counter , downloadList.isCreatedDir ());
                    statement.setTimestamp (++counter , Timestamp.valueOf (downloadList.getStartedAt ()));
                    statement.setLong (++counter , downloadList.getSize ());

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
                    while (resultSet.next ())
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

                        downloadLists.add (downloadList);
                    }

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

    public String makeQueryFindAll ()
    {
        return String.format ("select * from %s" , TABLE_NAME);
    }

    public String makeQueryCount ()
    {
        return makeQueryFindAll ().replace ("*" , "count(*)");
    }
}
