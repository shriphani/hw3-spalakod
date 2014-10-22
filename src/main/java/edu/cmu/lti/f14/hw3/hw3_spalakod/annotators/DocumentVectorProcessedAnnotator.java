package edu.cmu.lti.f14.hw3.hw3_spalakod.annotators;

import java.util.*;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f14.hw3.hw3_spalakod.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_spalakod.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_spalakod.utils.StanfordLemmatizer;
import edu.cmu.lti.f14.hw3.hw3_spalakod.utils.Utils;
import edu.stanford.nlp.ling.tokensregex.PhraseTable.TokenList;


public class DocumentVectorProcessedAnnotator extends JCasAnnotator_ImplBase {
  
  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {

    FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
    if (iter.isValid()) {
      iter.moveToNext();
      Document doc = (Document) iter.get();
      createTermFreqVector(jcas, doc);
    }

  }

  /**
   * better tokenizer that retrieves tokens, kicks out punctuation and lowercases stuff
   *
   * @param doc input text
   * @return    a list of tokens.
   */

  List<String> tokenize(String doc) {
    
    List<String> res = new ArrayList<String>();
    
    for (String t : doc.split("\\s+|\\d+|\\p{Punct}")) {
      res.add(t.toLowerCase());
    }
    
    return res;
  }

  /**
   * better tokenizer that retrieves tokens, kicks out punctuation and lowercases stuff
   *
   * @param doc input text
   * @return    a list of tokens.
   */
List<String> tokenize1(String doc) {
    
    List<String> tokenized = tokenize(doc);
    List<String> res = new ArrayList<String>();
    
    for (String t : tokenized) {
      res.add(StanfordLemmatizer.stemWord(t));
    }
    
    return res;
  }
  
  
  /**
   * 
   * @param jcas
   * @param doc
   */

  private void createTermFreqVector(JCas jcas, Document doc) {

    String docText = doc.getText();
    
    
    
    Map<String, Integer> tokenFreqMap = new HashMap<String, Integer>();
    
    for (String s : tokenize1(docText)) {
      if (tokenFreqMap.containsKey(s)) {
        tokenFreqMap.put(s, tokenFreqMap.get(s) + 1);
      }
      else {
        tokenFreqMap.put(s, 1);
      }
    }
    
    ArrayList<Token> ts = new ArrayList<Token>();
    
    Iterator it = tokenFreqMap.entrySet().iterator();
    
    while (it.hasNext()) {
      Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>)it.next();
      Token t = new Token(jcas);
      t.setText(pairs.getKey());
      t.setFrequency(pairs.getValue());
      
      ts.add(t);
    }
    FSList tokenList = Utils.fromCollectionToFSList(jcas, ts);
    doc.setTokenList(tokenList);
  }

}
