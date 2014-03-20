package org.elementascience.conehead.cl;

import org.apache.commons.cli.*;
import org.elementascience.conehead.common.UploadService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

public class ConeHead {
  UploadService serv;

  ConeHead() {
    ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
    serv = context.getBean("service", UploadService.class);
  }

  void run(CommandLine cmd) {
    if (cmd.hasOption('u')) {
      File zipFile = FileUtil.zipIt(cmd.getOptionValue('u'));

      if (zipFile != null) {
        serv.uploadFile(zipFile);
        }
      }

      if(cmd.hasOption('l')) {
        serv.listUploads();
      }
    }

    public static void main(String[] args) {

      CommandLineParser parser = new BasicParser();

      try {
        Options opts = getOptions();
        CommandLine cmd = parser.parse(opts, args);

        new ConeHead().run(cmd);

      } catch (ParseException e) {
        System.err.println(e.getMessage());
        usage();
      }
    }

    public static Options getOptions() {
      Options options = new Options();

      OptionGroup og = new OptionGroup();
      og.addOption( new Option("u", true, "upload provided file"));
      og.addOption( new Option("l", "list the current uploads"));
      og.setRequired(true);

      return options.addOptionGroup(og);
    }

    public static void usage() {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp( "ch", getOptions() );
    }
}
