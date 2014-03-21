package org.elementascience.conehead;

import org.elementascience.conehead.common.QueryService;
import org.elementascience.conehead.common.UploadService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;
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
  private DefaultListModel myModel;

  public JobProgress() {
    ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
    qs = context.getBean("qservice", QueryService.class);
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

    List<QueryService.IngestJob> jobs = qs.getJobs();
    for (QueryService.IngestJob job : jobs) {
      myModel.addElement(job.getName());
    }

  }

  private void createUIComponents() {
    myModel = new DefaultListModel();
    list1 = new JList(myModel);
  }
}
