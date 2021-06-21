package com.bardiademon.bardiademon;

import java.io.File;

public final class Path
{
    public static final String DATA = Get (System.getProperty ("user.dir") , "data"),
            DB = Get (DATA , "db.sqlite"),

    R_IMAGES = Get ("Images"),
            R_IC_ADD = Get (R_IMAGES , "ic_add.png"),


    IMAGES = Get (DATA , "images"),
            IC_DOWNLOADED = Get (IMAGES , "ic_downloaded.png");

    public static String Get (String... paths)
    {
        final StringBuilder finalPath = new StringBuilder ();
        for (int i = 0, len = paths.length; i < len; i++)
        {
            finalPath.append (paths[i]);
            if ((i + 1) < len) finalPath.append (File.separator);
        }
        return finalPath.toString ();
    }

    private Path ()
    {
    }
}
