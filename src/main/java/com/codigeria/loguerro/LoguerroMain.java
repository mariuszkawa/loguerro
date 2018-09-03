package com.codigeria.loguerro;

import java.util.Arrays;

final class LoguerroMain
{
    public static void main(String[] args)
    {
        new LoguerroCliRunner(Arrays.asList(args)).run();
    }

    private LoguerroMain()
    {
        throw new UnsupportedOperationException();
    }
}
