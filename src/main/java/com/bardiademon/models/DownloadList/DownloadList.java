package com.bardiademon.models.DownloadList;

import com.bardiademon.bardiademon.GetSize;

import java.time.LocalDateTime;

public class DownloadList
{
    private long id;
    private String link, path, description;
    private LocalDateTime startedAt, endAt;

    // timei ke download start shode va tamom shode , modat zamani ke download tol keshide
    private LocalDateTime time;
    private boolean completed, createdDir;
    private long size;

    private String filename;

    private boolean  theNameHasNoSuffix;

    private long listId;

    public DownloadList ()
    {
    }

    public long getId ()
    {
        return id;
    }

    public void setId (long id)
    {
        this.id = id;
    }

    public String getLink ()
    {
        return link;
    }

    public void setLink (String link)
    {
        this.link = link;
    }

    public String getPath ()
    {
        return path;
    }

    public void setPath (String path)
    {
        this.path = path;
    }

    public LocalDateTime getStartedAt ()
    {
        return startedAt;
    }

    public void setStartedAt (LocalDateTime startedAt)
    {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndAt ()
    {
        return endAt;
    }

    public void setEndAt (LocalDateTime endAt)
    {
        this.endAt = endAt;
    }

    public LocalDateTime getTime ()
    {
        return time;
    }

    public void setTime (LocalDateTime time)
    {
        this.time = time;
    }

    public boolean isCompleted ()
    {
        return completed;
    }

    public void setCompleted (boolean completed)
    {
        this.completed = completed;
    }

    public boolean isCreatedDir ()
    {
        return createdDir;
    }

    public void setCreatedDir (boolean createdDir)
    {
        this.createdDir = createdDir;
    }

    public String getSize ()
    {
        return GetSize.Get (size);
    }

    public long getByteSize ()
    {
        return size;
    }

    public void setSize (long size)
    {
        this.size = size;
    }

    public void setDescription (String description)
    {
        this.description = description;
    }

    public String getDescription ()
    {
        return description;
    }

    public boolean isTheNameHasNoSuffix ()
    {
        return theNameHasNoSuffix;
    }

    public void setTheNameHasNoSuffix (boolean theNameHasNoSuffix)
    {
        this.theNameHasNoSuffix = theNameHasNoSuffix;
    }

    public long getListId ()
    {
        return listId;
    }

    public void setListId (long listId)
    {
        this.listId = listId;
    }

    public String getFilename ()
    {
        return filename;
    }

    public void setFilename (String filename)
    {
        this.filename = filename;
    }

    @Override
    public String toString ()
    {
        return "DownloadList{" +
                "id=" + id +
                ", link='" + link + '\'' +
                ", path='" + path + '\'' +
                ", description='" + description + '\'' +
                ", startedAt=" + startedAt +
                ", endAt=" + endAt +
                ", time=" + time +
                ", completed=" + completed +
                ", createdDir=" + createdDir +
                ", size=" + size +
                '}';
    }
}
