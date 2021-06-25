package com.bardiademon.controllers;

import com.bardiademon.bardiademon.Log;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public final class ManagementClipboard extends Thread implements Runnable
{
    private final Clipboard systemClipboard;

    public ManagementClipboard ()
    {
        systemClipboard = Toolkit.getDefaultToolkit ().getSystemClipboard ();
        start ();
    }

    @Override
    public void run ()
    {
        systemClipboard.addFlavorListener (event ->
        {
            try
            {
                final String clip = systemClipboard.getData (DataFlavor.stringFlavor).toString ();
                AddUrlController.Fast (clip , ok ->
                {
                    if (ok)
                    {
                        Log.N ("Find url: " + clip);
                        systemClipboard.setContents (new StringSelection ("") , null);
                    }
                });
            }
            catch (UnsupportedFlavorException | IOException e)
            {
                Log.N (e);
            }
        });
    }
}
