package org.elementascience.conehead.cl;

import com.amazonaws.services.sqs.model.Message;
import org.apache.commons.cli.*;
import org.apache.commons.configuration.Configuration;
import org.elementascience.conehead.common.JobState;
import org.elementascience.conehead.common.UploadService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;

public class Minion {
  UploadService serv;
  Configuration config;

  Minion() {
    ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
    serv = context.getBean("service", UploadService.class);
    config = context.getBean("projectProperties", Configuration.class);
  }

  public static void main(String[] args) {

    CommandLineParser parser = new BasicParser();

    try {
      Options opts = getOptions();
      CommandLine cmd = parser.parse(opts, args);

      new Minion().run(cmd);

    } catch (ParseException e) {
      System.err.println(e.getMessage());
      usage();
    }

  }

  public static Options getOptions() {
    Options options = new Options();

    return options;
  }

  public static void usage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("minion", getOptions());
  }

  void run(CommandLine cmd) {


    ArticlePreparer prep = new ArticlePreparer(config);
    if (!prep.hasScript()) {
      System.out.println("no script on server");
      return;
    }

    while (true) {
      System.out.println("Minion ready and waiting for jobs");
      Message msg = serv.pollQueue();

      if (msg != null) {
        System.out.println("minion awakes!");
        String[] split = msg.getBody().split("_");
        String timestamp = split[0];
        String articleNumber = split[1];
        serv.updatePackageStatus(msg.getBody(), JobState.ONDECK, 0, "");

        System.out.println("downloading item Article: " +  articleNumber + " timestamp: " + timestamp);
        File outputFile = new File("/var/local/ingest/hold", msg.getBody() + ".zip");
        int downloadRes = serv.downloadFile(msg.getBody(), outputFile);
        serv.updatePackageStatus(msg.getBody(), JobState.UNPACKING, downloadRes, "");


        if (downloadRes == 0) {
          System.out.println("running prep-scripts and image generation for article: " + articleNumber);
          serv.deleteFile(msg.getBody());

          ArticlePreparer.OpResult opResult = prep.doOne(timestamp, articleNumber);
          serv.updatePackageStatus(msg.getBody(), JobState.PREPARING, opResult.result, opResult.message);

          if (opResult.result == 0) {
            // TODO use file operator to do this
            // copy file from prepped to todo
            System.out.println("moving package to IngestDirectory on server");
            int resultCode = prep.deploy(timestamp, articleNumber);
            if (resultCode == 0) {

              System.out.println("ingesting package :" + articleNumber);
              String errorString = RhinoSubmitter.attemptIngest(articleNumber);
              serv.updatePackageStatus(msg.getBody(), JobState.INGESTING, (errorString.length() == 0 ? 0 :1), errorString);

              if (errorString.length() == 0) {
                System.out.println("Publish article :" + articleNumber);
                int finalCode = RhinoSubmitter.publish(articleNumber);
                serv.updatePackageStatus(msg.getBody(), JobState.STAGING, finalCode, "");

                if (finalCode == 0) {
                  System.out.println("article is now fully published");
                } else {
                  System.out.println("failed to publish the article after ingest");
                }
              } else {
                System.out.println("article failed to ingest");
              }
            } else {
              System.out.println("failed to copy file into TODO directory");
            }
          } else {
            System.out.println("failed to pass prepare-sip");
          }
        } else {
          System.out.println("failed to download from s3");
        }

        serv.deleteMessage(msg);
        System.out.println("\ndeleted message: " + msg.getBody());
        System.out.println();

        // capture output and store in DB

      }
    }
  }
}
