
package io.leavesfly.textdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FileDBTool
{
    private FileProcess fileProcess;


    public FileDBTool()
    {
        this.fileProcess = new FileProcess();
    }


    public <T> ResultBean insert(T oT) throws IOException, SecurityException,
            IllegalArgumentException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException
    {
        ResultBean oResult = new ResultBean();

        boolean bRet = false;
        String sDescription = "";

        // get full text DB file name with full path
        String sFullFileName = this.getFullTableFileName(oT);

        // get sRowdata
        String sRowData = this.convertBeantoStr(oT, sFullFileName);

        PrintWriter oPWriter = null;
        try
        {
            // get writer
            oPWriter = fileProcess
                    .getAppendBufferedFilePrintWriter(sFullFileName);
            // write to file
            oPWriter.write(System.getProperty("line.separator") + sRowData);
            oPWriter.flush();

            // set return value as success
            bRet = true;
            sDescription = "the insert record is \"" + sRowData + "\". "
                    + ". It is saved in \"" + sFullFileName
                    + "\" successfully.";
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw e;
        }
        finally
        {
            // close the writer
            if (null != oPWriter)
            {
                oPWriter.close();
            }
        }
        // set final result.
        oResult.setDescription(sDescription);
        oResult.setResult(bRet);

        return oResult;
    }


    public <T> ResultBean delete(T oT) throws SecurityException,
            IllegalArgumentException, IOException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException
    {
        ResultBean oResult = new ResultBean();

        boolean bRet = false;
        String sDescription = "";

        // get full text DB file name with full path
        String sFullFileName = this.getFullTableFileName(oT);

        // get sRowdata
        String sRowData = this.convertBeantoStr(oT, sFullFileName);

        // delete from file
        bRet = this.deleteRecordFromFile(sRowData, sFullFileName);

        // set result description
        if (bRet)
        {
            sDescription = "The deleted record is \"" + sRowData + "\" . "
                    + "It is delete from \"" + sFullFileName
                    + "\" successfully.";
        }
        else
        {
            sDescription = "The deleting record \"" + sRowData
                    + "\" is not there in the file" + " \"" + sFullFileName
                    + "\".";
        }
        // set final result.
        oResult.setDescription(sDescription);
        oResult.setResult(bRet);

        return oResult;
    }

    public <T> ResultBean update(T oT, String sPrimaryKeyValue)
            throws SecurityException, IllegalArgumentException, IOException,
            NoSuchMethodException, IllegalAccessException,
            InvocationTargetException
    {
        ResultBean oResult = new ResultBean();

        boolean bRet = false;
        String sDescription = "";

        // get full text DB file name with full path
        String sFullFileName = this.getFullTableFileName(oT);

        // get sRowdata
        String sRowData = this.convertBeantoStr(oT, sFullFileName);

        // delete from file
        bRet = this.updateRecordToFile(sRowData, sPrimaryKeyValue,
                sFullFileName);

        // set result description
        if (bRet)
        {
            sDescription = "The updated record is \"" + sRowData + "\". "
                    + " It is update to \"" + sFullFileName
                    + "\" successfully.";
        }
        else
        {
            sDescription = "The updating record \"" + sRowData
                    + "\" is not there in the file" + " \"" + sFullFileName
                    + "\".";
        }
        // set final result.
        oResult.setDescription(sDescription);
        oResult.setResult(bRet);

        return oResult;
    }


    public <T> List<T> queryAll(T oT) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException,
            SecurityException, NoSuchMethodException, InstantiationException, IOException
    {
        List<T> oTList = new ArrayList<T>();

        // get full text DB file name with full path
        String sFullFileName = this.getFullTableFileName(oT);

        BufferedReader oBRead = null;
        try
        {
            oBRead = fileProcess.getBufferedFileReader(sFullFileName);
            String sReadLine;
            String[] sColumns = null;
            HashMap<String, String> oHM = null;
            String[] sColumnValues = null;
            int iNumOfColumns;
            Method oMethod;
            while (null != (sReadLine = oBRead.readLine()))
            {
                sReadLine = sReadLine.trim();

                // skip comments, empty line
                if (sReadLine.startsWith("#") || "".equals(sReadLine))
                {
                    continue;
                }

                // get set method names of the bean
                if (sReadLine.startsWith("ColumnSeq"))
                {
                    sColumns = sReadLine.split(":")[1].split("\\$_\\$");
                    oHM = this.getSetMethods(sColumns, oT);
                    continue;
                }

                // get values of the a record
                sColumnValues = sReadLine.split("\\$_\\$");

                // check the number of values is less than columns or not, the
                // smaller one will be used in the loop to avoid out of array
                // index exception
                iNumOfColumns = sColumnValues.length > sColumns.length ? sColumns.length
                        : sColumnValues.length;
                for (int i = 0; i < iNumOfColumns; i++)
                {
                    // get the get method
                    oMethod = oT.getClass().getMethod(oHM.get(sColumns[i]),String.class);

                    // execute the method to set the value into bean
                    oMethod.invoke(oT, sColumnValues[i]);
                }
                //add the bean into the bean list
                oTList.add(oT);
                //assign a new instance to the bean
                oT = (T) oT.getClass().newInstance();
            }
        }
        catch (IOException e)
        {
            // print e, and throw it, so the method which invoked it can process
            // it freely.
            e.printStackTrace();
            throw e;
        }
        finally
        {
            if (null != oBRead)
            {
                oBRead.close();
            }
        }
        return oTList;
    }


    private <T> HashMap<String, String> getSetMethods(String[] sColumns, T oT)
    {
        HashMap<String, String> oHM = new HashMap<String, String>();
        String sMethodName = "";
        String sFieldName = "";
        for (int i = 0; i < sColumns.length; i++)
        {
            // initialize the method name
            sMethodName = "set";

            // get the get method name of the current column
            sFieldName = sColumns[i].trim();
            if (sFieldName.length() > 1)
            {
                sMethodName = sMethodName
                        + sFieldName.substring(0, 1).toUpperCase()
                        + sFieldName.substring(1);
            }
            else
            {
                sMethodName = sMethodName + sFieldName.toUpperCase();
            }
            oHM.put(sColumns[i], sMethodName);
        }

        return oHM;
    }


    private boolean updateRecordToFile(String sRowData,
            String sPrimaryKeyValue, String sFullFileName) throws IOException
    {
        boolean bRet = false;

        StringBuffer sbFinalContent = new StringBuffer();

        PrintWriter oPWriter = null;
        BufferedReader oBRead = null;

        try
        {
            oBRead = fileProcess.getBufferedFileReader(sFullFileName);
            String sReadLine;
            String sLastLIne = "sLastLIne";
            String[] sColumnValues = null;
            while (null != (sReadLine = oBRead.readLine()))
            {
                sReadLine = sReadLine.trim();

                // make conjoined duplicated lines to one line
                // to avoid too many empty lines
                if (sLastLIne.equals(sReadLine))
                {
                    continue;
                }

                // handle the comments, empty line, ColumnSeq line
                if (sReadLine.startsWith("#") || "".equals(sReadLine)
                        || sReadLine.startsWith("ColumnSeq"))
                {
                    // cache in the buffer
                    sbFinalContent.append(sReadLine).append(
                            System.getProperty("line.separator"));
                    continue;
                }

                // handle changing record
                sColumnValues = sReadLine.split("\\$_\\$");
                if (sPrimaryKeyValue.trim().equals(sColumnValues[0].trim()))
                {
                    // cache in the buffer
                    sbFinalContent.append(sRowData).append(
                            System.getProperty("line.separator"));
                    continue;
                }

                // cache normal record in the buffer
                sbFinalContent.append(sReadLine).append(
                        System.getProperty("line.separator"));

                // set last line
                sLastLIne = sReadLine;
            }

            // get writer
            oPWriter = fileProcess.getBufferedFilePrintWriter(sFullFileName);

            // write the final content to file
            oPWriter.write(sbFinalContent.toString());
            oPWriter.flush();

            // set result
            bRet = true;

        }
        catch (IOException e)
        {
            // print e, and throw it, so the method which invoked it can process
            // it freely.
            e.printStackTrace();
            throw e;
        }
        finally
        {
            if (null != oBRead)
            {
                oBRead.close();
            }
            // close the writer
            if (null != oPWriter)
            {
                oPWriter.close();
            }
        }
        return bRet;
    }


    private boolean deleteRecordFromFile(String sRowData, String sFullFileName)
            throws IOException
    {
        boolean bRet = false;

        StringBuffer sbFinalContent = new StringBuffer();

        PrintWriter oPWriter = null;
        BufferedReader oBRead = null;
        // to get the columns' sequence
        try
        {
            oBRead = fileProcess.getBufferedFileReader(sFullFileName);
            String sReadLine;
            String sLastLIne = "sLastLIne";
            while (null != (sReadLine = oBRead.readLine()))
            {
                sReadLine = sReadLine.trim();

                // make conjoined duplicated lines to one line
                // to avoid too many empty lines
                if (sLastLIne.equals(sReadLine))
                {
                    continue;
                }

                // delete the target record
                if (sRowData.equals(sReadLine))
                {
                 // set result
                    bRet = true;
                    
                    continue;
                }

                // cache in the buffer
                sbFinalContent.append(sReadLine).append(
                        System.getProperty("line.separator"));
                // set last line
                sLastLIne = sReadLine;
            }

            // get writer
            oPWriter = fileProcess
                    .getBufferedFilePrintWriter(sFullFileName);

            // write the final content to file
            oPWriter.write(sbFinalContent.toString());
            oPWriter.flush();

        }
        catch (IOException e)
        {
            // print e, and throw it, so the method which invoked it can process
            // it freely.
            e.printStackTrace();
            throw e;
        }
        finally
        {
            if (null != oBRead)
            {
                oBRead.close();
            }
            // close the writer
            if (null != oPWriter)
            {
                oPWriter.close();
            }
        }
        return bRet;
    }


    private <T> String convertBeantoStr(T oT, String sFullFileName)
            throws IOException, SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        // define the return value
        String sRowdata = "";

        String[] sColumns = null;
        BufferedReader oBRead = null;
        // to get the columns' sequence
        try
        {
            oBRead = fileProcess.getBufferedFileReader(sFullFileName);
            String sReadLine;
            while (null != (sReadLine = oBRead.readLine()))
            {
                sReadLine = sReadLine.trim();
                // skip the comments and empty lines
                if ("".equals(sReadLine) || sReadLine.startsWith("#"))
                {
                    continue;
                }

                // find the columns' sequence
                if (sReadLine.startsWith("ColumnSeq"))
                {
                    sColumns = sReadLine.split(":")[1].split("\\$_\\$");
                    break;
                }
            }
        }
        catch (IOException e)
        {
            // print e, and throw it, so the method which invoked it can process
            // it freely.
            e.printStackTrace();
            throw e;
        }
        finally
        {
            // close the writer
            if (null != oBRead)
            {
                oBRead.close();
            }
        }

        // define field variable
        Method oMethod = null;
        String sFieldName = "";
        String sMethodName = "";
        // get the corresponding columns' value through the reflect mechanism
        for (int i = 0; i < sColumns.length; i++)
        {
            // initialize the method name
            sMethodName = "get";

            // get the get method name of the current column
            sFieldName = sColumns[i].trim();
            if (sFieldName.length() > 1)
            {
                sMethodName = sMethodName
                        + sFieldName.substring(0, 1).toUpperCase()
                        + sFieldName.substring(1);
            }
            else
            {
                sMethodName = sMethodName + sFieldName.toUpperCase();
            }
            // get the get method
            oMethod = oT.getClass().getMethod(sMethodName);
            // Execute the method to get the value of the current field
            // add the separator of the columns too
            sRowdata = sRowdata + oMethod.invoke(oT) + "$_$";
        }
        // remove the last separator
        if (!"".equals(sRowdata))
        {
            sRowdata = sRowdata.substring(0, sRowdata.lastIndexOf("$_$"));
        }
        return sRowdata;
    }


    private <T> String getFullTableFileName(T oT)
    {
        String sFullFileName = "";

        // the class name is like com.ross.filedb.tablebean.T_Home, only keep
        // T_Home
        String[] sTmps = oT.getClass().getName().split("\\.");
        sFullFileName = sTmps[sTmps.length - 1];

        sFullFileName = SysValues.DB_Path
                + sFullFileName.substring(0, sFullFileName
                        .indexOf(SysValues.DB_Bean_Suffix))
                + SysValues.DB_File_Suffix;
        return sFullFileName;
    }
}
