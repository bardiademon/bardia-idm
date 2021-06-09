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

    private Process process;
    private InputStream out;
    private OutputStream in;

    public GetInfoLink (final String url , final Callback callback)
    {
        this.url = url;
        this.callback = callback;
        start ();
    }

    @Override
    public void run ()
    {
        try
        {
            final ProcessBuilder builder = new ProcessBuilder ("java" , "-jar" , Path.DOWNLOAD_JAR , "-ma" , "-q");
            builder.redirectErrorStream (true); // so we can ignore the error stream
            process = builder.start ();
            out = process.getInputStream ();
            in = process.getOutputStream ();

            final byte[] buffer = new byte[4000];
            writer = new PrintWriter (in);

            while (run)
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
            destroy ();
            Log.N (e);
            System.gc ();
        }
        finally
        {
            destroy ();
        }
    }

    private void destroy ()
    {
        try
        {
            if (process != null && !run)
            {
                process.destroy ();
                if (in != null)
                {
                    in.flush ();
                    in.close ();
                }
                if (out != null) out.close ();

                if (writer != null)
                {
                    writer.flush ();
                    writer.close ();
                }

                System.gc ();
            }
        }
        catch (final Exception e)
        {
            Log.N (e);
        }
    }

    private void getInfoLink ()
    {
        if (line != null)
        {
            line = line.trim ();
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
                filename = true;
                filesize = true;
                callback.Error (line);
                line = "";
                run = false;
            }
        }
        run = (!filename || !filesize);
        destroy ();
    }

    public interface Callback
    {
        void Filename (final String Filename);

        void Filesize (final String Filesize);

        void Error (final String Error);

        void OK ();
    }
}
