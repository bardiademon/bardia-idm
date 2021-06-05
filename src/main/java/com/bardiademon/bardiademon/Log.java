package com.bardiademon.bardiademon;

public class Log
{
    public static void N (final Exception e)
    {
        N (null , e);
    }

    public static void N (final String Message)
    {
        N (Message , null);
    }

    public static void N (final String Message , final Exception E)
    {
        if (Message != null) System.out.println (Message);
        if (E != null && E.getMessage () != null && !E.getMessage ().isEmpty ())
            System.out.println (E.getMessage ());
    }
}
