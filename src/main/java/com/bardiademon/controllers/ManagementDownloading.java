package com.bardiademon.controllers;


import com.bardiademon.bardiademon.Log;
import com.bardiademon.models.DownloadList.DownloadList;
import java.util.ArrayList;
import java.util.List;

public final class ManagementDownloading
{
    private final List <DownloadList> downloadLists;

    private int index = 0;

    private static final int NUMBER_OF_SIMULTANEOUS_DOWNLOADS = 4;

    private boolean stop;

    private final List <DownloadingController> downloadingController;

    public ManagementDownloading (final List <DownloadList> downloadLists)
    {
        this.downloadLists = downloadLists;
        downloadingController = new ArrayList <> ();
        start (getMultipleDownloads ());
    }

    public void stop ()
    {
        new Thread (() ->
        {
            stop = true;
            for (final DownloadingController controller : downloadingController) controller.close (false);
            downloadingController.clear ();
            System.gc ();
        }).start ();
    }

    private List <DownloadList> getMultipleDownloads ()
    {
        final List <DownloadList> downloadList = new ArrayList <> ();
        try
        {
            for (; index < NUMBER_OF_SIMULTANEOUS_DOWNLOADS; index++) downloadList.add (downloadLists.get (index));
        }
        catch (final Exception e)
        {
            Log.N (e);
        }
        return downloadList;
    }

    private void start (List <DownloadList> downloadLists)
    {
        for (final DownloadList multipleDownload : downloadLists) launch (multipleDownload);
    }

    private void launch (final DownloadList downloadList)
    {
        if (stop) return;

        downloadingController.add (DownloadingController.Launch (downloadList , done ->
        {
            try
            {
                launch (downloadLists.get (index++));
            }
            catch (final Exception e)
            {
                Log.N (e);
            }
        }));
    }

}
