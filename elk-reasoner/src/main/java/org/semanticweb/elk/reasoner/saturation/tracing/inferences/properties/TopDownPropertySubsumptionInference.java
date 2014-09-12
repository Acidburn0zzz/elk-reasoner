/**
 * 
 */
package org.semanticweb.elk.reasoner.saturation.tracing.inferences.properties;

import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedObjectProperty;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedPropertyChain;
import org.semanticweb.elk.reasoner.saturation.tracing.inferences.visitors.ObjectPropertyInferenceVisitor;

/**
 * Property hierarchy inference based on a told sub-property subsumption.
 * R <= S if H <= S and R is a told sub-property of H. This class stores H as the premise.
 * 
 * @author Pavel Klinov
 *
 * pavel.klinov@uni-ulm.de
 */
public class TopDownPropertySubsumptionInference extends SubPropertyChain<IndexedPropertyChain, IndexedObjectProperty>
		implements ObjectPropertyInference {

	private final IndexedObjectProperty premise_;
	
	public TopDownPropertySubsumptionInference(IndexedPropertyChain chain,
			IndexedObjectProperty sup, IndexedObjectProperty premise) {
		super(chain, sup);
		premise_ = premise;
	}

	public SubPropertyChain<IndexedObjectProperty, IndexedObjectProperty> getPremise() {
		return new SubPropertyChain<IndexedObjectProperty, IndexedObjectProperty>(premise_, getSuperPropertyChain());
	}
	
	@Override
	public <I, O> O acceptTraced(ObjectPropertyInferenceVisitor<I, O> visitor,
			I input) {
		return visitor.visit(this, input);
	}
	
	@Override
	public String toString() {
		return "Told sub-chain: " + getSubPropertyChain() + " => " + getSuperPropertyChain() + ", premise: " + premise_ + " => " + getSuperPropertyChain();
	}

}
