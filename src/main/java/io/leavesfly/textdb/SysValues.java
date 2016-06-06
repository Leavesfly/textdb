/**
 *Author: Jiangtao He; Email: ross.jiangtao.he@gmail.com
 *File Name: SysValues.java
 *Date: 2011-12-30
 *Copyright: All right reserved by author - Jiangtao He
 *Version: MyJavaExpert v1.0
 *Description: 
 */
package io.leavesfly.textdb;


public class SysValues
{
    public static final String DB_Path = System.getProperty("user.dir")
            + System.getProperty("file.separator") + "db"
            + System.getProperty("file.separator");
    public static final String DB_Bean_Suffix = "Bean";
    public static final String DB_File_Suffix = ".db";

}