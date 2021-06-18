package com.bardiademon.models.Lists;

import com.bardiademon.models.DownloadList.DownloadList;
import java.time.LocalDateTime;
import java.util.List;

public final class Lists extends NameId
{
    private String path;

    private LocalDateTime createdAt;

    private List <DownloadList> downloadLists;

    public String getPath ()
    {
        return path;
    }

    public void setPath (String path)
    {
        this.path = path;
    }

    public LocalDateTime getCreatedAt ()
    {
        return createdAt;
    }

    public void setCreatedAt (LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public List <DownloadList> getDownloadLists ()
    {
        return downloadLists;
    }

    public void setDownloadLists (List <DownloadList> downloadLists)
    {
        this.downloadLists = downloadLists;
    }


}
