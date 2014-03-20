package org.elementascience.conehead.cl;


import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.entity.ContentType.*;

/**
 * User: dgreen
 * Date: 19/03/2014
 */
public class RhinoSubmitter {

  public static void main(String[] args) {


    if (0 == attemptIngest("000007") ) {
      publish("000007");
    }


  }

  static int attemptIngest(String articleNumber) {
    int result = 0;
    String name = "elementa." + articleNumber + ".zip";

    if (isIngestible(name)) {
      boolean overwrite = queryIfLoaded(articleNumber);
      System.out.println("ready to ingest");
      if (overwrite) {
        System.out.println("article exists in system, requesting overwrite on ingest");
      }

      int resultCode = ingest(name, overwrite);

      if (resultCode == 0) {
        System.out.println("Ingest successful: " + name);
      } else {
        System.out.println("FAILED to ingest: " + name);
        result = 1;
      }
    } else {
      System.out.println("article was not found in list of ingestible packages : " + name);
      result = 1;
    }
    return result;
  }


  static boolean isIngestible(String item) {
    boolean result = false;

    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpget = new HttpGet("http://localhost:8080/rhino/ingestibles");

    HttpResponse response = null;
    try {
      response = httpclient.execute(httpget);
      StatusLine sl = response.getStatusLine();
      if (sl.getStatusCode() == 200) {
        HttpEntity entity = response.getEntity();
        JSONParser p = new JSONParser();
        if (entity != null) {
          JSONArray a = (JSONArray)p.parse(new InputStreamReader(entity.getContent()));
          for (Object o: a) {
            if (item.equals(o)) {
              result = true;
            }
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    } finally {
    }

    return result;
  }


  static int ingest(String name, boolean overwrite) {
    int result = 0;

    HttpClient httpclient = new DefaultHttpClient();

    HttpResponse response = null;
    try {
      List<NameValuePair> formparams = new ArrayList<NameValuePair>();
      formparams.add(new BasicNameValuePair("name", name));
      formparams.add(new BasicNameValuePair("force_reingest", String.valueOf(overwrite)));
      UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
      HttpPost httpPost = new HttpPost("http://localhost:8080/rhino/ingestibles");
      httpPost.setEntity(entity);

      response = httpclient.execute(httpPost);
      StatusLine sl = response.getStatusLine();
      if (sl.getStatusCode() != 201) {
        for (String s: IOUtils.readLines(response.getEntity().getContent())) {
          System.out.println(s);
        }
        result = 1;
      }

    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      result = 1;
    } catch (ClientProtocolException e) {
      e.printStackTrace();
      result = 1;
    } catch (IOException e) {
      e.printStackTrace();
      result = 1;
    }

    return result;
  }

  static boolean queryIfLoaded(String articleNumber) {
    boolean result = false;

    HttpClient httpclient = new DefaultHttpClient();
    HttpGet httpget = new HttpGet("http://localhost:8080/rhino/articles");

    HttpResponse response = null;
    try {
      response = httpclient.execute(httpget);

      StatusLine sl = response.getStatusLine();

      if (sl.getStatusCode() == 200) {
        HttpEntity entity = response.getEntity();
        JSONParser p = new JSONParser();
        if (entity != null) {
          InputStream instream = null;
          try {
            instream = entity.getContent();
            java.util.Map m = (java.util.Map)p.parse(new InputStreamReader(instream));
            result = m.containsKey("10.12952/journal.elementa." + articleNumber);
          } catch (ParseException e) {
            e.printStackTrace();
          } finally {
            instream.close();
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
    }

    return result;
  }

  static int publish(String articleNumber) {
    int result = 0;

    // curl --data "{"state":"published"}"
    // --request PATCH localhost:8080/rhino/articles/info:doi/10.12952/journal.elementa.000007

    HttpClient httpclient = new DefaultHttpClient();
    HttpPatch httpPatch = new HttpPatch("http://localhost:8080/rhino/articles/info:doi/10.12952/journal.elementa." + articleNumber);

    StringEntity se = new StringEntity("{\"state\":\"published\"}", APPLICATION_JSON);
    httpPatch.setEntity(se);

    HttpResponse response = null;
    try {
      response = httpclient.execute(httpPatch);

      StatusLine sl = response.getStatusLine();
      if (sl.getStatusCode() != 200) {
        for (String s: IOUtils.readLines(response.getEntity().getContent())) {
          System.out.println(s);
        }
        result = 1;
      }

      // TODO store the result in the database for later viewing

    } catch (IOException e) {
      e.printStackTrace();
      result = 1;
    }


    return result;
  }
}
