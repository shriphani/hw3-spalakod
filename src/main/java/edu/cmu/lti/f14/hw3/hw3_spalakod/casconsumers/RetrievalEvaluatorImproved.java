package edu.cmu.lti.f14.hw3.hw3_spalakod.casconsumers;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.print.Doc;
import javax.swing.plaf.basic.BasicToolBarUI.DockingListener;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f14.hw3.hw3_spalakod.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_spalakod.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_spalakod.utils.Utils;


public class RetrievalEvaluatorImproved extends CasConsumer_ImplBase {
  
  /** Queries **/
  public Map<Integer, QueryData> queries;
  
  public void initialize() throws ResourceInitializationException {
    queries = new HashMap<Integer, QueryData>();
  }

  /**
   * TODO :: 1. construct the global word dictionary 2. keep the word
   * frequency for each sentence
   */
  @Override
  public void processCas(CAS aCas) throws ResourceProcessException {

    JCas jcas;
    try {
      jcas =aCas.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

    FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
  
    if (it.hasNext()) {
      Document doc = (Document) it.next();
      
      Map<String, Integer> docTFVector = new HashMap<String, Integer>();
      FSList fsTokenList = doc.getTokenList();
      ArrayList<Token>tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class);
      
      for (Token token : tokenList) {
        docTFVector.put(token.getText(), token.getFrequency());
      }
      
      if (doc.getRelevanceValue() == 99) {
        QueryData queryData = new QueryData();
        queryData.setQueryText(doc.getText());
        queryData.setQueryVector(docTFVector);
        queryData.setQid(doc.getQueryID());
        queries.put(doc.getQueryID(), queryData);
        
      } else {
        
        DocData docData = new DocData();
        docData.setDocText(doc.getText());
        docData.setDocVector(docTFVector);
        docData.setRel(doc.getRelevanceValue());
        
        QueryData queryObj = queries.get(doc.getQueryID());
        queryObj.addDoc(docData);
      }
    }
  }
  
  public List<Entry<DocData, Double>> rankDocs(Map<String, Integer> docVector, List<DocData> docs) {
    
    Map<DocData, Double> similarities = new HashMap<DocData, Double>();
    
    for (DocData doc : docs) {
       Map<String, Integer> docTFVector = doc.getDocVector();
       similarities.put(doc, computeJaccardSimilarity(docVector, docTFVector));
    }
    
    List<Entry<DocData, Double>> sortedEntries = new ArrayList<Entry<DocData,Double>>(similarities.entrySet());
    
    Collections.sort(sortedEntries, 
                     Collections.reverseOrder(
                             new Comparator<Entry<DocData, Double>>() {

                                 @Override
                                 public int compare(Entry<DocData, Double> arg0, Entry<DocData, Double> arg1) {
        
                                   return arg0.getValue().compareTo(arg1.getValue());
                                 }
      
                             }));
    
    return sortedEntries;
  }

  /**
   * Takes a list of docs and returns the df stats for tokens
   * @param docs
   * @return
   */
  public Map<String, Integer> computeDF(List<DocData> docs) {
    
    Map<String, Integer> res = new HashMap<String, Integer>();
    
    for (DocData doc : docs) {
      for (Entry<String, Integer> tf : doc.getDocVector().entrySet()) {
        String token = tf.getKey();
        if (!res.containsKey(token)) {
          res.put(token, 0);
        }
        res.put(token, res.get(token) + 1);
      }
    }
    
    return res;
  }
  
  /**
   * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
   * Compute the MRR metric
   */
  @Override
  public void collectionProcessComplete(ProcessTrace arg0)
      throws ResourceProcessException, IOException {

    super.collectionProcessComplete(arg0);
    
    DecimalFormat df = new DecimalFormat();
    df.setMinimumFractionDigits(4);
    df.setMaximumFractionDigits(4);
    
    double rrScore = 0.0;
    
    Set<Integer> qidSet = queries.keySet();
    ArrayList<Integer> qids = new ArrayList<Integer>();
    for (int qid: qidSet) {
      qids.add(qid);
    }
    Collections.sort(qids);
    // List<Integer> queryList = qids.subList(0, 5); - this is to match TA output
    List<Integer> queryList = qids;
    
    for (Integer qid : queryList) {
      QueryData query = queries.get(qid);
      ArrayList<DocData> docs = query.getDocs();
      Map<String, Integer> queryTFVector = query.getQueryVector();
      
      List<Entry<DocData, Double>> ranked = rankDocs(queryTFVector, docs);
      
      int i = 0;
      for (Entry<DocData, Double> entry : ranked) {
        i++;
        DocData corpusDoc = entry.getKey();
        double docSim = entry.getValue();
        
        if (corpusDoc.getRel() == 1) {
          System.out.println(formatOutput(df, docSim, i, query.getQid(), corpusDoc.getRel(), corpusDoc.getDocText()));
          rrScore += 1.0 / i;
        }
      }
    }
    System.out.println("MRR="+df.format(rrScore / (double) queryList.size()));

  }
  
  public String formatOutput(DecimalFormat df, double cosine, int rank, int qid, int rel, String text) {
    String relResult = "";
    relResult += "cosine=" + df.format(cosine) + "\t";
    relResult += "rank=" + rank + "\t";
    relResult += "qid=" + qid + "\t";
    relResult += "rel=" + rel + "\t";
    relResult += text;
    
    return relResult;
  }

  /**
   * 
   * @return jaccard_similarity
   */
  private double computeJaccardSimilarity(Map<String, Integer> queryVector,
      Map<String, Integer> docVector) {
    
    Set<String> queryToks = new HashSet<String>();
    Set<String> docToks = new HashSet<String>();
    
    for (Entry<String, Integer> entry : queryVector.entrySet()) {
      queryToks.add(entry.getKey());
    }
    
    for (Entry<String, Integer> entry : docVector.entrySet()) {
      docToks.add(entry.getKey());
    }
    
    Set<String> intersection = new HashSet<String>(docToks);
    Set<String> union = new HashSet<String>(docToks);
    
    intersection.retainAll(queryToks);
    union.addAll(queryToks);
    
    return (double) intersection.size() / (double) union.size();
  }
  
  /**
   * 
   * @return cosine_similarity
   */
  private double computeCosineSimilarity(Map<String, Integer> queryVector,
      Map<String, Integer> docVector) {
    
    double dotProduct = 0.0;
    
    for (Map.Entry<String, Integer> tf : queryVector.entrySet()) {
      dotProduct += docVector.containsKey(tf.getKey()) ? tf.getValue() * docVector.get(tf.getKey()) : 0.0;
    }

    return dotProduct / vectorLength(queryVector) / vectorLength(docVector);
  }

  private double vectorLength(Map<String, Integer> vector) {
    double result = 0.0;
    for (Map.Entry<String, Integer> entry : vector.entrySet()) {
      result += Math.pow(entry.getValue(), 2);
    }
    
    return Math.sqrt(result);
  }

}
