package org.elementascience.conehead.common;

import com.amazonaws.AmazonClientException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.transfer.Upload;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * User: dgreen
 * Date: 15/03/2014
 */
public class DirectoryPublishTask extends SwingWorker<Integer, String> {
  File      articleDir;
  JTextPane ta;
  JButton   toEnable;
  private UploadService serv;
  private JProgressBar  pb;
  private Upload        upload;
  private volatile String        tstamp;
  private volatile String aID;

  public DirectoryPublishTask(UploadService serv, JTextPane textPane, JProgressBar progressBar1, JButton uploadButton, File theSelection) {
    this.serv = serv;
    pb = progressBar1;
    articleDir = theSelection;
    ta = textPane;
    toEnable = uploadButton;
    upload = null;

    HTMLDocument doc = (HTMLDocument) ta.getDocument();
    try {
      doc.replace(0, doc.getLength(), "", null);
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void process(List<String> chunks) {
    HTMLDocument doc = (HTMLDocument) ta.getDocument();
    HTMLEditorKit editorKit = (HTMLEditorKit) ta.getEditorKit();
    for (String msg : chunks) {
      try {
        editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
      } catch (BadLocationException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected void done() {
    try {
      Integer result = get();

      if (result == 0) {
        process(Collections.singletonList("Job completed successfully"));
      } else {
        process(Collections.singletonList("return code = " + result + "\n"));
      }

      HTMLDocument doc = (HTMLDocument) ta.getDocument();
      HTMLEditorKit editorKit = (HTMLEditorKit) ta.getEditorKit();
      String text = "";
      try {
        text = doc.getText(0, doc.getLength());
      } catch (BadLocationException e) {
        e.printStackTrace();
      }
      int resultCode = serv.registerPackage(tstamp, aID, result, text);

    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    toEnable.setEnabled(true);
  }

  void sectionMessage(String msg) {
    publish("<h2>" + msg + "</h2>");
  }

  void errorMessage(String msg) {
    publish("<b><span style=\"color:#ff0000\">ERROR</span></b>: " + msg);
  }

  void warningMessage(String msg) {
    publish("<b><span style=\"color:#461B7E\">WARNING</span></b>: " + msg);
  }

  @Override
  protected Integer doInBackground() {
    if (articleDir == null) {
      errorMessage("Invalid input directory [null].");
      return 1;
    }

    if (!articleDir.exists()) {
      errorMessage("Input directory does not exist. [" + articleDir.getAbsolutePath() + "]");
      return 1;
    }

    if (!articleDir.isDirectory()) {
      errorMessage("Input directory is not actually a directory. [" + articleDir.getAbsolutePath() + "]");
      return 1;
    }

    sectionMessage("Checking filename conventions.");
    if (filesViolateNaming(articleDir)) {
      return 1;
    } else {
      publish("\nFiles are named properly.");
    }

    //TODO Must implement graphic file validation
    // publish("\n<h2>Confirming filetypes.</h2>");

    publish("\n\n");
    sectionMessage("Zipping directory contents for upload.");

    File result = zipDir(articleDir);
    if (result == null) {
      errorMessage("failed to zip input[" + result + "]");
      return 1;
    }

    publish("\n\n");
    sectionMessage("Uploading to server.");

    String articleID = getIdFromFileConsensus(articleDir);

    long unixTime = System.currentTimeMillis() / 1000L;
    String timestamp = String.valueOf(unixTime);

    int resultCode = uploadFile(result, timestamp + "_" + articleID);
    if (resultCode != 0) {
      errorMessage("Upload failed[" + result.getName() + "]");
      return 1;
    }

    sectionMessage("Notify minion to prep and stage article.");
    resultCode = serv.notifyMinion(timestamp, articleID);
    if (resultCode != 0) {
      errorMessage("queue insertion failed[" + timestamp + "_" + articleID + "]");
      return 1;
    }

    this.tstamp = timestamp;
    this.aID = articleID;
    return 0;
  }

  private String getIdFromFileConsensus(File theDir) {
    for (File f : theDir.listFiles()) {
      if (f.isFile()) {
        String name = f.getName();
        if (name.matches("elementa\\.\\d\\d\\d\\d\\d\\d\\.xml")) {
          return name.substring(9, 15);
        }
      }
    }
    return null;
  }


  private boolean filesViolateNaming(File theDir) {

    // locate primary xml and take article prefix from it
    String prefix = "";
    boolean hasEPUB = false;
    boolean hasMOBI = false;
    boolean hasPDF = false;
    boolean hasJSON = false;

    for (File f : theDir.listFiles()) {
      if (f.isFile()) {
        String name = f.getName();
        if (name.matches("elementa\\.\\d\\d\\d\\d\\d\\d\\.xml")) {
          prefix = name.substring(0, 15);
        }
      }
    }

    if (prefix == "") {
      errorMessage("No main XML found.");
      return true;
    }

    // each item in submission must be a file and have a name beginning with common prefix "elementa.xxxxxx"
    for (File f : theDir.listFiles()) {
      // . invisible get a free pass
      String name = f.getName();
      if (name.startsWith(".")) {
        continue;
      }

      if (f.isDirectory()) {
        errorMessage("Submission cannot have sub-directories [" + f.getName() + "]");
        return true;
      }

      if (!name.startsWith(prefix)) {
        errorMessage("File found with mis-matching name prefix [" + name + "] prefix=" + prefix);
        return true;
      } else {
        if (name.length() < 16) {
          errorMessage("File found with no file type [" + name + "]");
          return true;
        }

        String tail = name.substring(15);
        if (tail.equals(".xml")) {
          // xml gets a free pass
        } else if (tail.equals(".epub")) {
          hasEPUB = true;
        } else if (tail.equals(".mobi")) {
          hasMOBI = true;
        } else if (tail.equals(".pdf")) {
          hasPDF = true;
        } else if (tail.equals(".json")) {
          hasJSON = true;
        } else if (tail.matches("\\.e\\d\\d\\d\\.tif")) {
          // equation
        } else if (tail.matches("\\.f\\d\\d\\d\\.tif")) {
          // figure
        } else if (tail.matches("\\.t\\d\\d\\d\\.tif")) {
          // its a table image
        } else if (tail.matches("\\.s\\d\\d\\d\\..*")) {
          // its a supplemental
        } else {
          errorMessage("Directory contains file that is not a supported type [" + f.getName() + "]");
          publish("Files must be either epub,mobi,pdf,json,xml, or tif or supplemental sXXX.*");
          return true;
        }
      }

    }

    if (!hasEPUB) {
      warningMessage("No elmenta.xxxxxx.epub present in package.");
    }
    if (!hasPDF) {
      warningMessage("No elmenta.xxxxxx.pdf present in package.");
    }
    if (!hasJSON) {
      warningMessage("No elmenta.xxxxxx.json present in package.");
    }
    if (!hasMOBI) {
      warningMessage("No elmenta.xxxxxx.mobi present in package.");
    }

    return false;
  }


  public int uploadFile(File f, String destName) {
    ProgressListener progressListener = new ProgressListener() {
      @Override
      public void progressChanged(ProgressEvent progressEvent) {
        if (upload == null) return;

        pb.setValue((int) upload.getProgress().getPercentTransferred());

        switch (progressEvent.getEventCode()) {
          case ProgressEvent.COMPLETED_EVENT_CODE:
            pb.setValue(100);
            break;
          case ProgressEvent.FAILED_EVENT_CODE:
            try {
              AmazonClientException e = upload.waitForException();
              publish("Unable to upload file to Amazon S3: " + e.getMessage());
            } catch (InterruptedException e) {}
            break;
        }

      }
    };

    upload = serv.uploadWithListener(f, destName, progressListener);
    try {
      upload.waitForCompletion();
      return 0;
    } catch (InterruptedException e) {
      e.printStackTrace();
      return 1;
    }
  }

  public File zipDir(File input) {
    File result = null;
    try {
      File temp = File.createTempFile("AmbraUploader", ".zip");
      temp.deleteOnExit();

      FileOutputStream fos = new FileOutputStream(temp);
      ZipOutputStream zos = new ZipOutputStream(fos);
      zos.setLevel(9);

      addDirToArchive(zos, input);

      // close the ZipOutputStream
      zos.close();
      result = temp;
    } catch (IOException ioe) {
      publish("Error creating zip file: " + ioe);
    }
    return result;
  }

  private void addDirToArchive(ZipOutputStream zos, File srcFile) {

    File[] files = srcFile.listFiles();

    publish("zipping directory: " + srcFile.getName());

    for (int i = 0; i < files.length; i++) {

      if (files[i].getName().startsWith(".")) {
        continue;
      }

      // if the file is directory, use recursion
      if (files[i].isDirectory()) {
        addDirToArchive(zos, files[i]);
        continue;
      }

      try {
        publish("Adding file: " + files[i].getName());

        byte[] buffer = new byte[2048];
        FileInputStream fis = new FileInputStream(files[i]);
        zos.putNextEntry(new ZipEntry(files[i].getName()));

        int length;
        while ((length = fis.read(buffer)) > 0) {
          zos.write(buffer, 0, length);
        }

        zos.closeEntry();
        fis.close();

      } catch (IOException ioe) {
        publish("IOException :" + ioe);
      }

    }
  }

}
