package org.elementascience.conehead;

import org.elementascience.conehead.common.DirectoryPublishTask;
import org.elementascience.conehead.common.UploadService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * User: dgreen
 * Date: 08/04/2014
 */
public class SingleFile {
  UploadService serv;

  private JTextPane    progressTextPane;
  private JButton      selectFileButton;
  private JPanel       jpanel1;
  private JLabel       dirNameLabel;
  private JProgressBar progressBar1;
  private JLabel statusLabel;
  private JFrame frame;

  public SingleFile() {
    ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
    serv = context.getBean("service", UploadService.class);

    selectFileButton.addActionListener(new UploadButtonListener());
    progressTextPane.setContentType("text/html");

  }

  public static void main(String[] args) {

    new SingleFile().run();

  }


  private void run() {
    frame = new JFrame("Singlefile");
    frame.setContentPane(new SingleFile().jpanel1);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }


  protected void display(List<String> chunks) {
    HTMLDocument doc = (HTMLDocument) progressTextPane.getDocument();
    HTMLEditorKit editorKit = (HTMLEditorKit) progressTextPane.getEditorKit();
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

  private class UploadButtonListener implements ActionListener {

    public void actionPerformed(ActionEvent ae) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      int showOpenDialog = fileChooser.showOpenDialog(frame);
      if (showOpenDialog != JFileChooser.APPROVE_OPTION) return;

      selectFileButton.setEnabled(false);

      String name = fileChooser.getSelectedFile().getAbsolutePath();
      dirNameLabel.setText(name);

      display(Collections.singletonList("Directory set for upload: " + name + "\n"));
      DirectoryPublishTask task = new DirectoryPublishTask(serv, progressTextPane, progressBar1, statusLabel, fileChooser.getSelectedFile());
      task.execute();
    }

  }
}
