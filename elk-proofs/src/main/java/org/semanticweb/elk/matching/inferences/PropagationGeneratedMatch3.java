package org.semanticweb.elk.matching.inferences;

/*
 * #%L
 * ELK Proofs Package
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2016 Department of Computer Science, University of Oxford
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.semanticweb.elk.matching.conclusions.ConclusionMatchExpressionFactory;
import org.semanticweb.elk.matching.conclusions.PropagationMatch2;
import org.semanticweb.elk.matching.conclusions.SubPropertyChainMatch2;

public class PropagationGeneratedMatch3
		extends AbstractInferenceMatch<PropagationGeneratedMatch2> {

	PropagationGeneratedMatch3(PropagationGeneratedMatch2 parent,
			SubPropertyChainMatch2 thirdPremiseMatch) {
		super(parent);
		checkEquals(thirdPremiseMatch, getThirdPremiseMatch(DEBUG_FACTORY));
	}

	public SubPropertyChainMatch2 getThirdPremiseMatch(
			ConclusionMatchExpressionFactory factory) {
		return factory.getSubPropertyChainMatch2(
				getParent().getThirdPremiseMatch(factory),
				getParent().getParent().getSubDestinationMatch(), 0);
	}

	public PropagationMatch2 getConclusionMatch(
			ConclusionMatchExpressionFactory factory) {
		return factory.getPropagationMatch2(
				getParent().getParent().getConclusionMatch(factory),
				getParent().getExtendedDestinationMatch());
	}

	@Override
	public <O> O accept(InferenceMatch.Visitor<O> visitor) {
		return visitor.visit(this);
	}

	/**
	 * The visitor pattern for instances
	 * 
	 * @author Yevgeny Kazakov
	 *
	 * @param <O>
	 *            the type of the output
	 */
	public interface Visitor<O> {

		O visit(PropagationGeneratedMatch3 inferenceMatch3);

	}

	/**
	 * A factory for creating instances
	 * 
	 * @author Yevgeny Kazakov
	 *
	 */
	public interface Factory {

		PropagationGeneratedMatch3 getPropagationGeneratedMatch3(
				PropagationGeneratedMatch2 parent,
				SubPropertyChainMatch2 thirdPremiseMatch);

	}

}
