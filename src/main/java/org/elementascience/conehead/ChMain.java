package org.elementascience.conehead;

import org.elementascience.conehead.common.DirectoryPublishTask;
import org.elementascience.conehead.common.UploadService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: dgreen
 * Date: 15/03/2014
 */
public class ChMain {
  UploadService serv;
  private JButton      uploadButton;
  private JPanel       panel1;
  private JProgressBar progressBar1;
  private JTextPane    progressTextPane;
  private JFrame       frame;

  public ChMain() {
    ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
    serv = context.getBean("service", UploadService.class);

    uploadButton.addActionListener(new UploadButtonListener());
    progressTextPane.setContentType("text/html");


  }

  public static void main(String[] args) {

    new ChMain().run();

  }

  private void run() {
    frame = new JFrame("ChMain");
    frame.setContentPane(new ChMain().panel1);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }

  private class UploadButtonListener implements ActionListener {

    public void actionPerformed(ActionEvent ae) {
      JFileChooser fileChooser = new JFileChooser("/Users/gdave/tarticles");
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      int showOpenDialog = fileChooser.showOpenDialog(frame);
      if (showOpenDialog != JFileChooser.APPROVE_OPTION) return;

      uploadButton.setEnabled(false);
      DirectoryPublishTask task = new DirectoryPublishTask(serv, progressTextPane, progressBar1, null, fileChooser.getSelectedFile(), null, null, null);
      task.execute();
    }

  }

}
