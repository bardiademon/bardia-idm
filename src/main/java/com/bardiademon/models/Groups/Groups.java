package com.bardiademon.models.Groups;

import java.util.List;

public final class Groups
{
    private long id;
    private String name, defaultPath;

    private List <Extensions> extensions;

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

    public List <Extensions> getExtensions ()
    {
        return extensions;
    }

    public void setExtensions (List <Extensions> extensions)
    {
        this.extensions = extensions;
    }

    @Override
    public String toString ()
    {
        return getName ();
    }

    public static final class Extensions
    {
        private long id;
        private String extension;

        public Extensions ()
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

        public String getExtension ()
        {
            return extension;
        }

        public void setExtension (String extension)
        {
            this.extension = extension;
        }

        @Override
        public String toString ()
        {
            return getExtension ();
        }
    }
}
