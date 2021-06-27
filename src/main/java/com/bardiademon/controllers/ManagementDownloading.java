package com.bardiademon.controllers;

import com.bardiademon.Main;
import com.bardiademon.bardiademon.Log;
import com.bardiademon.bardiademon.NT;
import com.bardiademon.models.DownloadList.DownloadList;
import com.bardiademon.models.DownloadList.DownloadListService;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;

public final class ManagementDownloading
{
    private final List <DownloadList> downloadLists;
    private final Result result;

    private int index = 0;

    private static final int NUMBER_OF_SIMULTANEOUS_DOWNLOADS = 2;

    private boolean stop;

    private final List <DownloadingController> downloadingController;

    private final DownloadListService downloadListService;


    public ManagementDownloading (final List <DownloadList> _DownloadLists , final Result _Result)
    {
        this.downloadLists = _DownloadLists;
        this.result = _Result;
        this.downloadingController = new ArrayList <> ();
        this.downloadListService = new DownloadListService ();
        start (getMultipleDownloads ());
    }

    public void stop ()
    {
        new Thread (() ->
        {
            stop = true;
            if (downloadingController != null && downloadingController.size () > 0)
            {
                for (final DownloadingController controller : downloadingController) controller.close (false);
                downloadingController.clear ();
            }
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
        if (stop)
        {
            result.OnCompleted ();
            System.gc ();
            return;
        }

        Platform.runLater (() -> DownloadingController.Launch (downloadList , done ->
        {
            try
            {
                downloadList.setCompleted (done);
                downloadListService.modify (downloadList);
                NT.N (Main.getMainController ()::refresh);
                launch (downloadLists.get (++index));
                System.gc ();
            }
            catch (final Exception e)
            {
                Log.N (e);
                result.OnCompleted ();
                System.gc ();
            }
        } , true , downloadingController::add));
    }

    public interface Result
    {
        void OnCompleted ();
    }

}
