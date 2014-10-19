package edu.cmu.lti.f14.hw3.hw3_spalakod.casconsumers;

import java.util.Map;

public class QueryData {

  private Map<String, Integer> queryVector;
  private Map<String, Integer> relDocVector;
  private Map<String, Integer> irRelDocVector;
  private String relDocText;
  private String irRelDocText;
  
  public QueryData() {
    
  }
  
  public void setQueryVector(Map<String, Integer> queryVector) {
    this.queryVector = queryVector;
  }
  
  public void setRelDocVector(Map<String, Integer> relDocVector) {
    this.relDocVector = relDocVector;
  }
  
  public void setIRRelDocVector(Map<String, Integer> irRelDocVector) {
    this.irRelDocVector = irRelDocVector;
  }
  
  public void setRelDocText(String relDocText) {
    this.relDocText = relDocText;
  }
  
  public void setIrRelDocText(String irRelDocText) {
    this.irRelDocText = irRelDocText;
  }
  
  public Map<String, Integer> getQueryVector() {
    return this.queryVector;
  }
  
  public Map<String, Integer> getRelDocVector() {
    return this.relDocVector;
  }
  
  public Map<String, Integer> getIRRelDocVector() {
    return this.irRelDocVector;
  }
  
  public String getRelDocText() {
    return this.relDocText;
  }
  
  public String getIrRelDocText() {
    return this.irRelDocText;
  }
}
