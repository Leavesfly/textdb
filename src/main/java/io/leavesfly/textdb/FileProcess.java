
package io.leavesfly.textdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class FileProcess
{

    public PrintWriter getAppendBufferedFilePrintWriter(String sFullFileName)
            throws IOException
    {
        PrintWriter oPWriter = new PrintWriter(new BufferedWriter(
                new FileWriter(sFullFileName, true)));
        return oPWriter;
    }


    public PrintWriter getBufferedFilePrintWriter(String sFullFileName)
            throws IOException
    {
        PrintWriter oPWriter = new PrintWriter(new BufferedWriter(
                new FileWriter(sFullFileName)));
        return oPWriter;
    }
    

    public BufferedReader getBufferedFileReader(String sFullFileName)
            throws IOException
    {
        BufferedReader oBufferedReader = new BufferedReader(new FileReader(
                sFullFileName));
        return oBufferedReader;
    }

}
