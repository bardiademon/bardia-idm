package com.bardiademon.controllers;

import com.bardiademon.bardiademon.Log;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.Timer;
import java.util.TimerTask;

public final class ManagementClipboard
{
    private final Clipboard systemClipboard;
    private String oldClip;

    private Timer timer;

    public ManagementClipboard ()
    {
        systemClipboard = Toolkit.getDefaultToolkit ().getSystemClipboard ();
    }

    public TimerTask getTimerTask ()
    {
        return new TimerTask ()
        {
            @Override
            public void run ()
            {
                try
                {
                    final String clip = systemClipboard.getData (DataFlavor.stringFlavor).toString ();
                    if (clip != null && !clip.isEmpty () && (oldClip == null || !oldClip.equals (clip)))
                    {
                        oldClip = clip;
                        AddUrlController.Fast (clip , ok ->
                        {
                            if (ok)
                            {
                                Log.N ("Find url: " + clip);
                                systemClipboard.setContents (new StringSelection ("") , null);
                            }
                        });
                    }
                }
                catch (final Exception e)
                {
                    Log.N (e , false);
                }
            }
        };
    }

    public void setActive (final boolean active)
    {
        try
        {
            if (active) (timer = new Timer ()).schedule (getTimerTask () , 500 , 500);
            else
            {
                if (timer != null)
                {
                    timer.cancel ();
                    timer = null;
                    System.gc ();
                }
            }
        }
        catch (final Exception e)
        {
            Log.N (e);
        }
    }
}
