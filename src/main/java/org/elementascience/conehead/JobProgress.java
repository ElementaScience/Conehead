package org.elementascience.conehead;

import org.elementascience.conehead.common.QueryService;
import org.elementascience.conehead.common.UploadService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * User: dgreen
 * Date: 20/03/2014
 */
public class JobProgress {
  private QueryService qs;

  private JFrame  frame;
  private JPanel  panel1;
  private JButton button1;
  private JList   list1;
  private JButton button2;
  private JPanel  jobPanel;
  private JLabel  stateLabel;
  private JLabel  codeLabel;
  private JTextPane jobReportTxt;
  private DefaultListModel aModel;

  public JobProgress() {
    ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
    qs = context.getBean("qservice", QueryService.class);
    button1.addActionListener(new myListener());
    list1.addListSelectionListener(new ListSelectionListener() {

      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (list1.getSelectedValue() != null) {
            QueryService.IngestJob job = (QueryService.IngestJob) list1.getSelectedValue();

            stateLabel.setText(job.getState());
            codeLabel.setText(String.valueOf(job.getCode()));
            jobReportTxt.setText(job.getReport());
        }
      }
    });
  }


  public static void main(String[] args) {
    new JobProgress().run();
  }

  private void run() {
    frame = new JFrame("JobProgress");
    frame.setContentPane(new JobProgress().panel1);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);

  }

  class myListener implements ActionListener{
    @Override
    public void actionPerformed(ActionEvent e) {new WorkerThread().start();}
  }

  class WorkerThread extends Thread {
    public WorkerThread() {
    }

    public void run() {
      while (true) {
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            int previousSelection = list1.getMinSelectionIndex();

            DefaultListModel model = new DefaultListModel();
            for (QueryService.IngestJob job : qs.getJobs()) {
              model.addElement(job);
            }
            list1.setModel(model);
            list1.setSelectedIndex(previousSelection);
            aModel = model;
          }
        } );
        try {
          sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        yield();
      }
    }

  }

}
