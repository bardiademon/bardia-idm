package com.bardiademon.bardiademon;

public final class NT
{
    public static void N (final Runnable _Runnable)
    {
        new Thread (_Runnable).start ();
    }
}
