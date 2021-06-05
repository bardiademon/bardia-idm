package com.bardiademon;

import com.bardiademon.bardiademon.Log;
import com.bardiademon.bardiademon.Path;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;


public final class GetInfoLink extends Thread implements Runnable
{
    private final String url;
    private final Callback callback;
    private String line;
    private PrintWriter writer;
    private boolean run = true;
    private boolean filename, filesize;

    public GetInfoLink (final String url , final Callback callback)
    {
        this.url = url;
        this.callback = callback;
        start ();
    }

    public static boolean isAlive (Process p)
    {
        try
        {
            p.exitValue ();
            return false;
        }
        catch (IllegalThreadStateException e)
        {
            return true;
        }
    }

    @Override
    public void run ()
    {
        Process process = null;
        try
        {
            final ProcessBuilder builder = new ProcessBuilder ("java" , "-jar" , Path.DOWNLOAD_JAR , "-ma" , "-q");
            builder.redirectErrorStream (true); // so we can ignore the error stream
            process = builder.start ();
            final InputStream out = process.getInputStream ();
            final OutputStream in = process.getOutputStream ();

            final byte[] buffer = new byte[4000];
            writer = new PrintWriter (in);

            while (isAlive (process) && run)
            {
                int no = out.available ();
                if (no > 0)
                {
                    int n = out.read (buffer , 0 , Math.min (no , buffer.length));
                    line = new String (buffer , 0 , n);
                    getInfoLink ();
                }
            }
            callback.OK ();
            process.destroy ();
            System.gc ();
        }
        catch (final IOException e)
        {
            if (process != null) process.destroy ();
            Log.N (e);
            System.gc ();
        }
    }

    private void getInfoLink ()
    {
        if (line != null)
        {
            if (line.contains ("Link:"))
            {
                writer.println (url);
                writer.flush ();
                line = "";
            }
            else if (!filesize && line.contains ("File size : "))
            {
                filesize = true;
                callback.Filesize (line.split ("File size : ")[1].trim ());
                line = "";
            }
            else if (!filename && line.contains ("Filename: "))
            {
                filename = true;
                callback.Filename (line.split ("Filename: ")[1].trim ());
                line = "";
            }
            else if (line.contains ("Download error =>"))
            {
                callback.Error (line);
                line = "";
                run = false;
            }
        }
        run = (!filename || !filesize);
    }

    public interface Callback
    {
        void Filename (final String Filename);

        void Filesize (final String Filesize);

        void Error (final String Error);

        void OK ();
    }
}
