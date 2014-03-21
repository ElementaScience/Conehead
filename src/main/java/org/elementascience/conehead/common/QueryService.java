package org.elementascience.conehead.common;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import org.apache.commons.configuration.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueryService {
  Configuration config;
  AmazonSimpleDB  sdb;
  private String registryName;

  QueryService(Configuration i_config) {
    config = i_config;

    BasicAWSCredentials creds = new BasicAWSCredentials(config.getString("accessKey"), config.getString("secretKey"));
    sdb = new AmazonSimpleDBClient(creds);
    Region usEast1 = Region.getRegion(Regions.US_EAST_1);
    sdb.setRegion(usEast1);

    registryName = config.getString("registryName");
  }



  public List<IngestJob> getJobs() {
    ArrayList<IngestJob> result = new ArrayList<IngestJob>();

    try {
      SelectRequest sr = new SelectRequest().withSelectExpression("select * from " + registryName);
      SelectResult jobs = sdb.select(sr);

      for (Item job : jobs.getItems() ) {
        result.add(new IngestJob(job.getName(), job.getAttributes()));
      }

    } catch (AmazonServiceException ase) {
      System.out.println("Caught Exception: " + ase.getMessage());
      System.out.println("Reponse Status Code: " + ase.getStatusCode());
      System.out.println("Error Code: " + ase.getErrorCode());
      System.out.println("Request ID: " + ase.getRequestId());
    }

    return result;
  }


  public class IngestJob {
    String name;
    JobState state;
    String timestamp;
    String article;

    private HashMap<JobState, ResultPair> stages = new HashMap<JobState, ResultPair>();

    public String toString() {
      return name;
    }

    public IngestJob(String name, List<Attribute> attributes) {
      this.name = name;
      for (Attribute attr : attributes) {
        if (attr.getName().equals("state")) {
          state = JobState.valueOf(attr.getValue());
        } else if (attr.getName().equals("timestamp")) {
          timestamp = attr.getValue();
        } else if (attr.getName().equals("article")) {
          article = attr.getValue();
        } else {
          String[] vals = attr.getName().split("_");
          if (vals.length == 2) {
            JobState js = JobState.valueOf(vals[0].toUpperCase());
            if (!stages.containsKey(js)) {
              stages.put(js, new ResultPair());
            }
            if (vals[1].equals("res")) {
              stages.get(js).setResultCode(Integer.parseInt(attr.getValue()));
            } else {
              stages.get(js).setResultText(attr.getValue());
            }
          } else {
            System.out.println("incorrect format of job attribute" + attr.getName());
          }
        }
      }
    }

    boolean stateReached(JobState state) {
      return stages.containsKey(state);
    }

    int stateCode(JobState state) {
      return stages.get(state).getResultCode();
    }

    String stateReport(JobState state) {
      return stages.get(state).getResultText();
    }

    public String getName() {
      return name;
    }

    public String getState() {
      return state.toString();
    }

    public int getCode() {
      return this.stages.get(state).getResultCode();
    }

    public String getReport() {
      return this.stages.get(state).getResultText();
    }
  }



}