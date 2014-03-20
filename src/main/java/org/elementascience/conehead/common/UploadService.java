package org.elementascience.conehead.common;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UploadService {

  private final String queueURL;
  Configuration config;

  AmazonS3        s3;
  TransferManager tx;
  AmazonSimpleDB  sdb;
  AmazonSQS       sqs;

  private String bucketName;
  private String registryName;

  UploadService(Configuration i_config) {
    config = i_config;
    BasicAWSCredentials creds = new BasicAWSCredentials(config.getString("accessKey"), config.getString("secretKey"));
    Region usEast1 = Region.getRegion(Regions.US_EAST_1);


    s3 = new AmazonS3Client(creds);
    s3.setRegion(usEast1);

    sdb = new AmazonSimpleDBClient(creds);
    sdb.setRegion(usEast1);

    tx = new TransferManager(s3);

    sqs = new AmazonSQSClient(creds);
    sqs.setRegion(usEast1);

    bucketName = config.getString("uploadBucket");
    registryName = config.getString("registryName");
    queueURL = config.getString("queueURL");
  }


  public void uploadFile(File f) {

    if (f != null) {
      PutObjectRequest request = new PutObjectRequest(bucketName, f.getName(), f);
      Upload upload = tx.upload(request);

      System.out.println("uploading = " + f.getName());
      while (!upload.isDone()) {}
      System.out.println("upload complete");

    } else {
      System.err.println("failed to zip input[" + f + "]");
    }
  }


  public void deleteFile(String key) {
    try {
        s3.deleteObject(bucketName, key);
    } catch (AmazonServiceException ase) {
      System.out.println("Error Message:    " + ase.getMessage());
      System.out.println("HTTP Status Code: " + ase.getStatusCode());
      System.out.println("AWS Error Code:   " + ase.getErrorCode());
      System.out.println("Error Type:       " + ase.getErrorType());
      System.out.println("Request ID:       " + ase.getRequestId());
    } catch (AmazonClientException ace) {
      System.out.println("Error Message: " + ace.getMessage());
    }
  }


  public int downloadFile(String key, File f) {
    int result = 0;
    if (f != null) {
      S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
      System.out.println("Content-Type: " + object.getObjectMetadata().getContentType());

      if (!f.exists()) {
        try {
          f.createNewFile();
        } catch (IOException e) {
        }
      }

      OutputStream os = null;
      InputStream is = null;
      try {
        is = object.getObjectContent();
        os = new FileOutputStream(f);
        IOUtils.copy(is, os);
      } catch (IOException e) {
        e.printStackTrace();
        result = 1;
      } finally {
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(os);
      }
      System.err.println("download[" + f + "]");
    } else {
      result = 1;
    }
    return result;
  }


  public void listUploads() {
    System.out.println("Bucket listing");

    try {

      ObjectListing objects = s3.listObjects(bucketName);
      do {
        for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
          System.out.println("  " + objectSummary.getKey());
        }
        objects = s3.listNextBatchOfObjects(objects);
      } while (objects.isTruncated());


      // should also check the database content


    } catch (AmazonServiceException ase) {
      System.out.println("Error Message:    " + ase.getMessage());
      System.out.println("HTTP Status Code: " + ase.getStatusCode());
      System.out.println("AWS Error Code:   " + ase.getErrorCode());
      System.out.println("Error Type:       " + ase.getErrorType());
      System.out.println("Request ID:       " + ase.getRequestId());
    } catch (AmazonClientException ace) {
      System.out.println("Error Message: " + ace.getMessage());
    }


  }


  public Upload uploadWithListener(File target, String destName, ProgressListener progressListener) {
    PutObjectRequest request = new PutObjectRequest(bucketName,
                                                    destName,
                                                    target).withGeneralProgressListener(progressListener);
    return tx.upload(request);
  }


  public void createAmazonS3Bucket() {
    try {
      if (tx.getAmazonS3Client().doesBucketExist(bucketName) == false) {
        tx.getAmazonS3Client().createBucket(bucketName);
      }
    } catch (AmazonClientException ace) {
      System.out.println("Unable to create a new Amazon S3 bucket: " + ace.getMessage());
    }
  }

  public int registerPackage(String tstamp, String id, int opRes, String opRep) {
    int result = 0;

    try {
      List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>();
      attrs.add(new ReplaceableAttribute("state", "new", true));
      attrs.add(new ReplaceableAttribute("timestamp", tstamp, true));
      attrs.add(new ReplaceableAttribute("article", id, true));
      attrs.add(new ReplaceableAttribute("new_res", String.valueOf(opRes), true));
      attrs.add(new ReplaceableAttribute("new_rep", opRep, true));

      PutAttributesRequest request = new PutAttributesRequest()
          .withDomainName(registryName)
          .withAttributes(attrs)
          .withItemName(tstamp + "_" + id);

      sdb.putAttributes(request);

    } catch (AmazonServiceException ase) {
      System.out.println("Caught Exception: " + ase.getMessage());
      System.out.println("Reponse Status Code: " + ase.getStatusCode());
      System.out.println("Error Code: " + ase.getErrorCode());
      System.out.println("Request ID: " + ase.getRequestId());
      result = 1;
    }

    return result;
  }

  public int updatePackageStatus(String key, String newStatus, int result, String report) {
    int returnVal = 0;

    try {

      ArrayList<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>();
      attrs.add(new ReplaceableAttribute("state", newStatus, true));
      attrs.add(new ReplaceableAttribute(newStatus + "_res", String.valueOf(result), true));
      attrs.add(new ReplaceableAttribute(newStatus + "_rep", report, true));

      PutAttributesRequest request = new PutAttributesRequest().withDomainName(registryName).withAttributes(attrs).withItemName(key);

      sdb.putAttributes(request);

    } catch (AmazonServiceException ase) {
      System.out.println("Caught Exception: " + ase.getMessage());
      System.out.println("Reponse Status Code: " + ase.getStatusCode());
      System.out.println("Error Code: " + ase.getErrorCode());
      System.out.println("Request ID: " + ase.getRequestId());
      returnVal = 1;
    }

    return returnVal;
  }


  public int notifyMinion(String timestamp, String articleID) {
    int result = 0;
    try {
      SendMessageRequest mRequest = new SendMessageRequest(queueURL, timestamp + "_" + articleID);

      SendMessageResult sendMessageResult = sqs.sendMessage(mRequest);
      sendMessageResult.getMessageId();
    } catch (AmazonServiceException ase) {
      System.out.println("Caught Exception: " + ase.getMessage());
      System.out.println("Reponse Status Code: " + ase.getStatusCode());
      System.out.println("Error Code: " + ase.getErrorCode());
      System.out.println("Request ID: " + ase.getRequestId());
      result = 1;
    }
    return result;
  }


  public Message pollQueue() {
    ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueURL).withWaitTimeSeconds(20).withMaxNumberOfMessages(1);

    List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
    for (Message message : messages) {
      return message;
    }

    return null;

  }

  public void deleteMessage(Message msg) {
    String messageRecieptHandle = msg.getReceiptHandle();
    sqs.deleteMessage(new DeleteMessageRequest(queueURL, messageRecieptHandle));
  }


}
