package com.bardiademon;

import com.bardiademon.bardiademon.Log;
import com.bardiademon.bardiademon.Path;
import com.bardiademon.controllers.DownloadFileIsExistsController;
import javafx.scene.control.Alert;
import org.apache.commons.io.FilenameUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Download extends Thread implements Runnable
{
    // FIEC => File Is Exists Command
    public static final int FIEC_RESUME = 1, FIEC_DELETE = 2, FIEC_RENAME = 3, FIEC_CANCEL = 4;

    private final String url;
    private final String path;
    private final Callback callback;
    private final boolean createDir;
    private final boolean theNameHasNoSuffix;
    private String line;
    private PrintWriter writer;
    private boolean run = true;
    private final String filename;

    private Process process;
    private InputStream out;
    private OutputStream in;

    private boolean filenameIsExists, fullNotDownloaded;

    private boolean downloading;

    public Download (final String URL , final String Path , Callback _Callback , final boolean CreateDir , final boolean TheNameHasNoSuffix) throws Exception
    {
        if (notEmpty (URL) && notEmpty (Path) && _Callback != null)
        {
            this.url = URL;
            this.path = Path;
            this.filename = FilenameUtils.getName (path);
            this.callback = _Callback;
            this.createDir = CreateDir;
            this.theNameHasNoSuffix = TheNameHasNoSuffix;
            start ();
        }
        else throw new Exception ("Empty info");
    }

    private boolean notEmpty (final String val)
    {
        return (val != null && !val.isEmpty ());
    }

    @Override
    public void run ()
    {
        download ();
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

    private void download ()
    {
        try
        {
            final List <String> command = new ArrayList <> ();

            command.add ("java");
            command.add ("-jar");
            command.add (Path.DOWNLOAD_JAR);
            command.add ("-l");
            command.add (url);
            command.add ("-p");
            command.add (path);
            command.add ("-q");

            if (createDir) command.add ("-m");
            if (theNameHasNoSuffix) command.add ("-f");

            final ProcessBuilder builder = new ProcessBuilder (command);


            builder.redirectErrorStream (true); // so we can ignore the error stream
            process = builder.start ();
            out = process.getInputStream ();
            in = process.getOutputStream ();

            final byte[] buffer = new byte[4000];
            writer = new PrintWriter (in);

            while (run)
            {
                final int no = out.available ();
                if (no > 0)
                {
                    int n = out.read (buffer , 0 , Math.min (no , buffer.length));
                    line = new String (buffer , 0 , n);
                    analysis ();
                }
            }
            destroy ();
            System.gc ();
        }
        catch (final IOException e)
        {
            Log.N (e);
            System.gc ();
        }
    }

    private void analysis ()
    {
        line = line.trim ();

        if (filenameIsExists || line.contains (String.format ("This file<%s> is exists." , filename)))
        {
            filenameIsExists = true;
            if (!fullNotDownloaded) fullNotDownloaded = line.contains ("Full not downloaded");

            if (line.contains ("Enter number:"))
            {
                filenameIsExists = false;
                DownloadFileIsExistsController.Launch ((command , Filename) ->
                {
                    if (command == FIEC_RESUME || command == FIEC_RENAME || command == FIEC_DELETE || command == FIEC_CANCEL)
                    {
                        if (command == FIEC_RESUME)
                        {
                            if (fullNotDownloaded)
                            {
                                writer.println (command);
                                writer.flush ();
                            }
                            else
                            {
                                final Alert alert = new Alert (Alert.AlertType.ERROR);
                                alert.setContentText ("Resume is disable!");
                                alert.setHeaderText ("Wrong choice");
                                alert.setTitle ("Wrong choice");
                                alert.show ();
                            }
                        }
                        else
                        {
                            writer.println (command);
                            writer.flush ();
                        }
                    }
                } , filename , fullNotDownloaded);
            }
        }
        else if (downloading || line.contains ("||"))
        {
            try
            {
                final String[] split = line.split (Pattern.quote ("||"));
                final String progress = find (find (line , "[0-9]*%" , "0") , "[0-9]*" , "0");
                final String time = find (line , "[0-9][0-9]:[0-9][0-9]:[0-9][0-9]" , "00:00:00");

                callback.OnDownloading (progress , split[1].trim () , split[2].trim () , time);
            }
            catch (final Exception e)
            {
                downloading = false;
                Log.N (e);
            }
        }

        run = !line.contains ("Download complete.");
        if (!run) callback.OnDownloading ("101" , null , null , null);

    }

    private String find (final String str , final String regex , final String defaultReturn)
    {
        final Matcher matcher = Pattern.compile (regex).matcher (str);
        if (matcher.find ()) return matcher.group ();
        else return defaultReturn;
    }

    public interface Callback
    {
        void OnDownloading (final String Progress , final String Downloaded , final String Speed , final String Time);
    }
}
