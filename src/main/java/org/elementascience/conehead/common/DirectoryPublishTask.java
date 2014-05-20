package org.elementascience.conehead.common;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.transfer.Upload;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * User: dgreen
 * Date: 15/03/2014
 */
public class DirectoryPublishTask extends SwingWorker<Integer, String> implements Publisher {
  File      articleDir;
  JTextPane ta;
  JLabel statusLabel;

  private UploadService uploadService;
  private JProgressBar  pb;
  private Upload        upload;
  private volatile String timestamp;
  private volatile String articleID;
	private String publishedURLPrefix;

  public DirectoryPublishTask(UploadService uploadService, JTextPane textPane, JProgressBar progressBar1, JLabel label, File theSelection, String publishedURLPrefix) {
    this.uploadService = uploadService;
    pb = progressBar1;
    statusLabel = label;
    articleDir = theSelection;
    ta = textPane;
    upload = null;
	  this.publishedURLPrefix = publishedURLPrefix;

    HTMLDocument doc = (HTMLDocument) ta.getDocument();
//    try {
//      doc.replace(0, doc.getLength(), "", null);
//    } catch (BadLocationException e) {
//      e.printStackTrace();
//    }
  }

  @Override
  protected void process(List<String> chunks) {
    HTMLDocument doc = (HTMLDocument) ta.getDocument();
    HTMLEditorKit editorKit = (HTMLEditorKit) ta.getEditorKit();
    for (String msg : chunks) {
      if (msg.startsWith("status")) {
        statusLabel.setText(msg.substring(6));
      }
      else
      {
	      try
	      {
		      editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
		      scrollToBottom(ta);
	      }
	      catch (BadLocationException e)
	      {
		      e.printStackTrace();
	      }
	      catch (IOException e)
	      {
		      e.printStackTrace();
	      }
      }
    }
  }

	public static void scrollToBottom(JComponent component) {
		Rectangle visibleRect = component.getVisibleRect();
		visibleRect.y = component.getHeight() - visibleRect.height;
		component.scrollRectToVisible(visibleRect);
	}

	@Override
	protected void done()
	{
		try
		{
			Integer result = get();

			if (result == 0)
			{
				sectionMessage("Job completed successfully; view article <a href=\"" + publishedURLPrefix + articleID + "\">here.</a>");
				statusMessage("Processing complete.");
			}
			else
			{
				errorMessage("Job failed:  return code = " + result + "\n");
				statusMessage("Processing failed.");
			}

			logResultsToRegistry(result);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
		}

		publish("<hr>");
	}

	private void logResultsToRegistry(Integer result)
	{
		String text = getDocumentText();
		logToRegistry(result, text);
	}

	private String getDocumentText()
	{
		HTMLDocument doc = (HTMLDocument) ta.getDocument();
		String text = "";
		try
		{
			text = doc.getText(0, doc.getLength());
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		return text;
	}

	private void logToRegistry(Integer result, String text)
	{
		int iteration = 0;
		int index = 0;
		int lengthToLog = Math.min(1024, text.substring(index).length());
		do
		{
			String key = timestamp + "_" + articleID;
			uploadService.logFinalState(key, result, text.substring(index, index + lengthToLog), iteration++);

			index += lengthToLog;
			lengthToLog = Math.min(1024, text.substring(index).length());

		} while (lengthToLog > 0);
	}


	// This keyword at the beginning updates the "Status" label as the top of the panel.

  void statusMessage(String msg) {
    publish("status" + msg);
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
  protected Integer doInBackground() throws InterruptedException
  {
	  statusMessage("In Progress");

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

	  sectionMessage("Analyzing filenames.");
	  ArticleDirectory articleDirectory;
	  try
	  {
		  articleDirectory = new ArticleDirectory(articleDir, this);
		  String statusMsg = articleDirectory.analyzeFilenames();
		  publish(statusMsg);
	  }
	  catch (RuntimeException e)
	  {
		  errorMessage(e.getMessage());
		  return 1;
	  }

    //TODO Must implement graphic file validation
    // publish("\n<h2>Confirming filetypes.</h2>");

	  sectionMessage("Zipping directory contents for upload.");

	  File result = articleDirectory.BuildZipFile();

    if (result == null) {
      errorMessage("failed to zip input[" + result + "]");
      return 1;
    }

	  sectionMessage("Uploading to server.");
    pb.setVisible(true);

    long unixTime = System.currentTimeMillis() / 1000L;
    String timestamp = String.valueOf(unixTime);

	  String articleID = articleDirectory.getArticleID();
	  String jobName = timestamp + "_" + articleID;
	  publish("Job Name: " + jobName);

    int resultCode = uploadFile(result, jobName);
    if (resultCode != 0) {
      errorMessage("Upload failed[" + result.getName() + "]");
      return 1;
    } else {
      publish("Upload completed in " + String.valueOf((System.currentTimeMillis() / 1000L) - unixTime) + " seconds.");
    }

	  sectionMessage("Notifying server to prepare and load article.");
    resultCode = uploadService.notifyMinion(jobName);
    if (resultCode != 0) {
      errorMessage("queue insertion failed[" + jobName + "]");
      return 1;
    }

	  UploadService.IngestJob job = waitForJobToAppear(jobName);

	  JobState js = JobState.valueOf(job.getState());
	  publishJobOutput(job);

	  job = monitorJobProgress(jobName, job, js);

	  this.timestamp = timestamp;
	  this.articleID = articleID;
	  return job.getCode();
  }

	private UploadService.IngestJob monitorJobProgress(String jobName, UploadService.IngestJob job, JobState oldState) throws InterruptedException
	{
		JobState jobState = oldState;
		while (!jobState.equals(JobState.PUBLISHING) && wasSuccessful(job))
		{
			Thread.sleep(1000);

			System.out.println("Querying job status...");
			job = uploadService.getJob(jobName);

			jobState = job.getJobState();
			if (oldState != jobState)
			{
				publishJobOutput(job);
				oldState = jobState;
			}
		}
		return job;
	}

	private void publishJobOutput(UploadService.IngestJob job)
	{
		JobState jobState = job.getJobState();
		sectionMessage("Phase: " + jobState + " complete.\n");

		if (wasSuccessful(job))
		{
			publish("This phase completed successfully.");
		}
		else
		{
			publish("This phase encountered an error.");
		}


		String jobOutput = job.getReport();
		if (jobOutput.length() > 0)
		{
			publish("Detailed output from this phase: <br><br>");
			for (String item : jobOutput.split("\n"))
			{
				publish(item);
			}
		}
		else
		{
			publish("No detailed output from this phase.");
		}

		if (wasSuccessful(job))
		{
			JobState nextState = jobState.next();
			if (nextState != null)
			{
				sectionMessage("Starting phase: " + nextState + ".");
			}
		}
	}

	private boolean wasSuccessful(UploadService.IngestJob job)
	{
		return job.getCode() == 0;
	}

	private UploadService.IngestJob waitForJobToAppear(String jobName) throws InterruptedException
	{
		sectionMessage("Waiting for server.");
		UploadService.IngestJob job = null;

		while (null == job)
		{
			System.out.println("Querying job status: waiting for job to appear.");
			Thread.sleep(1000);
			job = uploadService.getJob(jobName);
		}
		return job;
	}

	public int uploadFile(File f, String destName)
	{
		ProgressListener progressListener = new ProgressListener()
		{
			@Override
			public void progressChanged(ProgressEvent progressEvent)
			{
				if (upload == null)
				{
					return;
				}

				pb.setValue((int) upload.getProgress().getPercentTransferred());

				switch (progressEvent.getEventCode())
				{
					case ProgressEvent.COMPLETED_EVENT_CODE:
						pb.setValue(100);
						pb.setVisible(false);
						break;
					case ProgressEvent.FAILED_EVENT_CODE:
						try
						{
							AmazonClientException e = upload.waitForException();
							publish("Unable to upload file to Amazon S3: " + e.getMessage());
							hideProgressBar();
						}
						catch (InterruptedException e)
						{
						}
						break;
				}
			}
		};

		upload = uploadService.uploadWithListener(f, destName, progressListener);
		try
		{
			upload.waitForCompletion();
			return 0;
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			return 1;
		}
		catch (AmazonServiceException e)
		{
			System.out.println("Amazon Service Exception");
			e.printStackTrace();
			return 1;
		}
		catch (AmazonClientException e)
		{
			System.out.println("Amazon Client Exception");
			e.printStackTrace();
			return 1;
		}
	}

	private void hideProgressBar()
	{
		pb.setVisible(false);
	}


	public void publishMessage(String message)
	{
		publish(message);
	}
}
