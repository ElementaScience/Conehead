package org.elementascience.conehead.cl;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * User: dgreen
 * Date: 18/03/2014
 */
public class ArticlePreparer {
  private boolean hasPrep;

  public ArticlePreparer(Configuration config) {

    hasPrep = false;
    ProcessBuilder pb = new ProcessBuilder("/usr/local/topaz/bin/prepare-sip");
    pb.directory(new File("/var/local/ingest/hold"));
    try {
      Process p = pb.start();
      StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");

      // start gobblers
      outputGobbler.start();
      p.waitFor();
      hasPrep = (p.exitValue() == 0);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  class OpResult {
    public int result;
    public String message;

    public OpResult(int result, String theText) {
      this.result = result;
      this.message = theText;
    }

  }

  OpResult doOne(String timestamp, String articleID) {
    int result = 0;
    String theText = "";

    ProcessBuilder pb = new ProcessBuilder("/usr/local/topaz/bin/prepare-sip",
                                           "-o",  "/var/local/ingest/prepped/"+ timestamp + "_" + articleID + ".zip",
                                           "/var/local/ingest/hold/" + timestamp + "_" + articleID + ".zip");
    pb.directory(new File("/var/local/ingest/hold"));

    try {
      Process p = pb.start();

      StringWriter writer = new StringWriter();
      IOUtils.copy(p.getInputStream(), writer, "UTF-8");
      theText = writer.toString();

      p.waitFor();
      result = p.exitValue();

    } catch (IOException e) {
      e.printStackTrace();
      result = 1;
    } catch (InterruptedException e) {
      e.printStackTrace();
      result = 1;
    }

    return new OpResult(result, theText);
  }

  public boolean hasScript() { return hasPrep; }

  public int deploy(String timestamp, String articleID) {
    int result = 0;
    ProcessBuilder pb = new ProcessBuilder("cp",
                                           "/var/local/ingest/prepped/"+ timestamp + "_" + articleID + ".zip",
                                           "/var/local/ingest/todo/elementa." + articleID + ".zip");

    pb.directory(new File("/var/local/ingest/hold"));

    try {
      Process p = pb.start();

      StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
      outputGobbler.start();

      p.waitFor();
      result = p.exitValue();

    } catch (IOException e) {
      e.printStackTrace();
      result = 1;
    } catch (InterruptedException e) {
      e.printStackTrace();
      result = 1;
    }

    return result;
  }


  private class StreamGobbler extends Thread {
    InputStream is;
    String      type;

    private StreamGobbler(InputStream is, String type) {
      this.is = is;
      this.type = type;
    }

    @Override
    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null)
          System.out.println(type + "> " + line);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }
}
