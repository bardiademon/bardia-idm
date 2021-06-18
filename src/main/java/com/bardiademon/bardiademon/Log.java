package com.bardiademon.bardiademon;

public class Log
{
    public static final String DATABASE_NOT_CONNECTED = "Database not connected";

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
        {
            final StackTraceElement traceElement = E.getStackTrace ()[0];
            System.out.printf ("%s [%s{%d}]\n" , E.getMessage () , traceElement.getClassName () , traceElement.getLineNumber ());
        }
    }

    public static void NOT_CONNECTED ()
    {

    }
}
