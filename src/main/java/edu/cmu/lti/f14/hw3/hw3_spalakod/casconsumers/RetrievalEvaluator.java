package edu.cmu.lti.f14.hw3.hw3_spalakod.casconsumers;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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


public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;

	/** global tokens set **/
	public Set<String> tokens;
	
	/** for each query, store the query text vectors and so on for quick cosine sim computations **/
	public HashMap<Integer, QueryData> queryData;
		
	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();
		
		tokens = new HashSet<String>();
		
		queryData = new HashMap<Integer, QueryData>();
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

			// start populating the queydata objects
			if (!queryData.containsKey(doc.getQueryID())) {
			  queryData.put(doc.getQueryID(), new QueryData());
			}
			
			HashMap<String, Integer> docTFVector = new HashMap<String, Integer>();
			
			//Make sure that your previous annotators have populated this in CAS
			FSList fsTokenList = doc.getTokenList();
			ArrayList<Token>tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class);
			
			for (Token token : tokenList) {
			  docTFVector.put(token.getText(), token.getFrequency());
			}
			
			if (doc.getRelevanceValue() == 0) {
			  queryData.get(doc.getQueryID()).setIRRelDocVector(docTFVector);
			  queryData.get(doc.getQueryID()).setIrRelDocText(doc.getText());
			} else if (doc.getRelevanceValue() == 1) {
			  queryData.get(doc.getQueryID()).setRelDocVector(docTFVector);
			  queryData.get(doc.getQueryID()).setRelDocText(doc.getText());
			} else {
			  queryData.get(doc.getQueryID()).setQueryVector(docTFVector);
			}
		}

	}

	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);

		// TODO :: compute the cosine similarity measure
		ArrayList<Integer> qids = new ArrayList<Integer>();
		
		for (Integer i : queryData.keySet()) {
		  qids.add(i);
		}
		
		Collections.sort(qids);
		
		DecimalFormat df = new DecimalFormat();
		df.setMinimumFractionDigits(4);
		df.setMaximumFractionDigits(4);
		
		double rrScore = 0.0;
		
		for (Integer qid : qids) {
		  QueryData data = queryData.get(qid);
		  
		  Map<String, Integer> relDocVector = data.getRelDocVector();
		  Map<String, Integer> irRelDocVector = data.getIRRelDocVector();
		  Map<String, Integer> queryDocVector = data.getQueryVector();
		  
		  double relCosine = computeCosineSimilarity(queryDocVector, relDocVector);
		  double irRelCosine = computeCosineSimilarity(queryDocVector, irRelDocVector);
		  
		  int relRank = relCosine < irRelCosine ? 2 : 1;
		  int irRelRank = relRank == 1 ? 2 : 1;
		  
		  String relResult = formatOutput(df, relCosine, relRank, qid, 1, data.getRelDocText());
		  String irRelResult = formatOutput(df, irRelCosine, irRelRank, qid, 0, data.getIrRelDocText());
		  
		  System.out.println(relResult);
		  System.out.println(irRelResult);
		  
		  rrScore += (1.0 / relRank);
		}
		
		System.out.println("MRR=" + rrScore / qids.size());
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
	  
	  return result;
	}

}
