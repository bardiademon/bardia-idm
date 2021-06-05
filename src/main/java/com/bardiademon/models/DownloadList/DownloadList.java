package com.bardiademon.models.DownloadList;

import java.time.LocalDateTime;

public final class DownloadList
{
    private long id;
    private String link, path;
    private LocalDateTime startedAt, endAt;

    // timei ke download start shode va tamom shode , modat zamani ke download tol keshide
    private LocalDateTime time;
    private boolean completed, createdDir;
    private long size;

    public long getId ()
    {
        return id;
    }

    public LocalDateTime getEndAt ()
    {
        return endAt;
    }

    public void setEndAt (final LocalDateTime endAt)
    {
        this.endAt = endAt;
    }

    public void setId (final long id)
    {
        this.id = id;
    }

    public String getLink ()
    {
        return link;
    }

    public void setLink (final String link)
    {
        this.link = link;
    }

    public String getPath ()
    {
        return path;
    }

    public void setPath (final String path)
    {
        this.path = path;
    }

    public LocalDateTime getStartedAt ()
    {
        return startedAt;
    }

    public void setStartedAt (final LocalDateTime startedAt)
    {
        this.startedAt = startedAt;
    }

    public LocalDateTime getTime ()
    {
        return time;
    }

    public void setTime (final LocalDateTime time)
    {
        this.time = time;
    }

    public boolean isCompleted ()
    {
        return completed;
    }

    public void setCompleted (final boolean completed)
    {
        this.completed = completed;
    }

    public boolean isCreatedDir ()
    {
        return createdDir;
    }

    public void setCreatedDir (final boolean createdDir)
    {
        this.createdDir = createdDir;
    }

    public long getSize ()
    {
        return size;
    }

    public void setSize (final long size)
    {
        this.size = size;
    }

    @Override
    public String toString ()
    {
        return "DownloadList{" +
                "id=" + id +
                ", link='" + link + '\'' +
                ", path='" + path + '\'' +
                ", startedAt=" + startedAt +
                ", endAt=" + endAt +
                ", time=" + time +
                ", completed=" + completed +
                ", createdDir=" + createdDir +
                ", size=" + size +
                '}';
    }
}
