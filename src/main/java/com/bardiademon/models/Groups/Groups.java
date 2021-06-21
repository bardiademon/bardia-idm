package com.bardiademon.models.Groups;

import java.util.List;

public final class Groups
{
    private long id;
    private String name, defaultPath;

    private List <String> extensions;

    public Groups ()
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

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getDefaultPath ()
    {
        return defaultPath;
    }

    public void setDefaultPath (String defaultPath)
    {
        this.defaultPath = defaultPath;
    }

    public List <String> getExtensions ()
    {
        return extensions;
    }

    public void setExtensions (List <String> extensions)
    {
        this.extensions = extensions;
    }

    @Override
    public String toString ()
    {
        return getName ();
    }
}
