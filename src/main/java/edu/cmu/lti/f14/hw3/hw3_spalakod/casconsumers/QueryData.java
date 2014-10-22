package edu.cmu.lti.f14.hw3.hw3_spalakod.casconsumers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QueryData {

  private int qid;
  private String queryText;
  private Map<String, Integer> queryVector;
  
  private ArrayList<DocData> docs;
  
  public QueryData() {
    queryVector = new HashMap<String, Integer>();
    docs = new ArrayList<DocData>();
  }
  
  public void setQueryText(String queryText) {
    this.queryText = queryText;
  }
  
  public void addDoc(DocData doc) {
    this.docs.add(doc);
  }
  
  public void setQid(int qid) {
    this.qid = qid;
  }
  
  public void setQueryVector(Map<String, Integer> queryVector) {
    this.queryVector = queryVector;
  }
  
  public String getQueryText() {
    return queryText;
  }
  
  public Map<String, Integer> getQueryVector() {
    return queryVector;
  }
  
  public ArrayList<DocData> getDocs() {
    return docs;
  }
  
  public int getQid() {
    return qid;
  }
}
