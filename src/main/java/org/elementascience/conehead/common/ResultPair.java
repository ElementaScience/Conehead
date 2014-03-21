package org.elementascience.conehead.common;

/**
 * User: dgreen
 * Date: 21/03/2014
 */
public class ResultPair {

  private int    resultCode;
  private String resultText;

  ResultPair() {
  }

  public String getResultText() {
    return resultText;
  }

  public void setResultText(String resultText) {
    this.resultText = resultText;
  }

  public int getResultCode() {
    return resultCode;
  }

  public void setResultCode(int resultCode) {
    this.resultCode = resultCode;
  }

}
