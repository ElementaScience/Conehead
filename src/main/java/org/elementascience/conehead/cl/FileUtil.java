package org.elementascience.conehead.cl;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * User: dgreen
 * Date: 13/03/2014
 */
public class FileUtil {
  final static   int    BUFFER = 2048;
  private static Logger log    = Logger.getLogger(FileUtil.class);

  public static File zipIt(String name) {
    File result = null;
    try {
      File srcFile = new File(name);
      long unixTime = System.currentTimeMillis() / 1000L;
      String newPath =  name + String.valueOf(unixTime) + ".zip";

      FileOutputStream fos = new FileOutputStream(newPath);
      ZipOutputStream zos = new ZipOutputStream(fos);
      zos.setLevel(9);

      addDirToArchive(zos, srcFile);

      // close the ZipOutputStream
      zos.close();
      result = new File(newPath);
    }
    catch (IOException ioe) {
      System.out.println("Error creating zip file: " + ioe);
    }
    return result;
  }

  private static void addDirToArchive(ZipOutputStream zos, File srcFile) {

    File[] files = srcFile.listFiles();

    System.out.println("zipping directory: " + srcFile.getName());

    for (int i = 0; i < files.length; i++) {

      // if the file is directory, use recursion
      if (files[i].isDirectory()) {
        addDirToArchive(zos, files[i]);
        continue;
      }

      try {

        System.out.println("Adding file: " + files[i].getName());

        // create byte buffer
        byte[] buffer = new byte[1024];

        FileInputStream fis = new FileInputStream(files[i]);

        zos.putNextEntry(new ZipEntry(files[i].getName()));

        int length;

        while ((length = fis.read(buffer)) > 0) {
          zos.write(buffer, 0, length);
        }

        zos.closeEntry();

        // close the InputStream
        fis.close();

      } catch (IOException ioe) {
        System.out.println("IOException :" + ioe);
      }

    }
  }


}
