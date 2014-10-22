package edu.cmu.lti.f14.hw3.hw3_spalakod.casconsumers;

import java.util.HashMap;
import java.util.Map;

public class DocData {

  public String docText;
  public Map<String, Integer> docVector;
  public int rel;
  
  public DocData() {
    docVector = new HashMap<String, Integer>();
  }
  
  public void setDocText(String docText) {
    this.docText = docText;
  }
  
  public void setDocVector(Map<String, Integer> docVector) {
    this.docVector = docVector;
  }
  
  public void setRel(int rel) {
    this.rel = rel;
  }
  
  public String getDocText() {
    return docText;
  }
  
  public Map<String, Integer> getDocVector() {
    return docVector;
  }
  
  public int getRel() {
    return rel;
  }
}
