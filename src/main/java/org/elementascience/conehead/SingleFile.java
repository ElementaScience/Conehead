package org.elementascience.conehead;

import org.elementascience.conehead.common.DirectoryPublishTask;
import org.elementascience.conehead.common.UploadService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * User: dgreen
 * Date: 08/04/2014
 */
public class SingleFile
{
	UploadService serv;

	private JTextPane progressTextPane;
	private JButton selectFileButton;
	private JPanel jpanel1;
	private JLabel dirNameLabel;
	private File ingestDirectory = null;
	private JProgressBar progressBar;
	private JLabel statusLabel;
	private JTextField stagingComment;
	private JButton submitButton;
	private JFrame frame;
	private String publishedURLPrefix;

	private WaitLayerUI layerUI;


	public SingleFile()
	{
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
		serv = context.getBean("service", UploadService.class);
		publishedURLPrefix = (String) context.getBean("publishedURLPrefix");

		selectFileButton.addActionListener(new SelectFileListener());
		submitButton.addActionListener(new SubmitButtonListener());
		progressTextPane.setContentType("text/html");

		installHyperlinkListener();
	}

	private void installHyperlinkListener()
	{
		progressTextPane.setEditable(false);

		ToolTipManager.sharedInstance().registerComponent(progressTextPane);
		HyperlinkListener hyperlinkListener = new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				if (HyperlinkEvent.EventType.ACTIVATED == e.getEventType())
				{
					try
					{
						if (Desktop.isDesktopSupported())
						{
							Desktop.getDesktop().browse(e.getURL().toURI());
						}
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
					catch (URISyntaxException e1)
					{
						e1.printStackTrace();
					}
				}

			}

		};
		progressTextPane.addHyperlinkListener(hyperlinkListener);
	}

	public static void main(String[] args)
	{
		new SingleFile().run();
	}


	private void run()
	{
		frame = new JFrame("Elementa Ingest Client - Version 2.1");

		layerUI = new WaitLayerUI();
		JLayer<JPanel> jlayer = new JLayer<JPanel>(jpanel1, layerUI);
		frame.add(jlayer);

//		frame.setContentPane(new SingleFile().jpanel1);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}


	protected void display(List<String> chunks)
	{
		HTMLDocument doc = (HTMLDocument) progressTextPane.getDocument();
		HTMLEditorKit editorKit = (HTMLEditorKit) progressTextPane.getEditorKit();
		for (String msg : chunks)
		{
			try
			{
				editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
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

	private class SelectFileListener implements ActionListener
	{

		public void actionPerformed(ActionEvent ae)
		{
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int showOpenDialog = fileChooser.showOpenDialog(frame);
			if (showOpenDialog != JFileChooser.APPROVE_OPTION)
			{
				return;
			}

			ingestDirectory = fileChooser.getSelectedFile();
			dirNameLabel.setText(ingestDirectory.getAbsolutePath());
		}

	}

	private class SubmitButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if (validateInput())
			{
				System.out.println("Ingesting...");
				display(Collections.singletonList("Ingest Directory: " + dirNameLabel.getText() + "\n"));
				display(Collections.singletonList("Ingest Comment:   " + stagingComment.getText() + "\n"));
				display(Collections.singletonList("Uploading to:     " + serv.getUploadBucket() + "\n"));
				DirectoryPublishTask task = new DirectoryPublishTask(
						serv,
						progressTextPane,
						progressBar,
						statusLabel,
						ingestDirectory,
						stagingComment.getText(),
						publishedURLPrefix,
						layerUI,
						selectFileButton);
				task.execute();
			}
		}
	}

	private boolean validateInput()
	{
		if (ingestDirectory == null)
		{
			JOptionPane.showMessageDialog(null, "Please select a directory to stage.");
			return false;
		}
		else if (stagingComment.getText().length() == 0)
		{
			JOptionPane.showMessageDialog(null, "Please enter a meaningful \"Staging Comment\".");
			return false;
		}
		else
		{
			return true;
		}
	}
}
