package org.semanticweb.elk.matching.inferences;

import org.semanticweb.elk.matching.ElkMatchException;

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
import org.semanticweb.elk.matching.conclusions.IndexedContextRootMatch;
import org.semanticweb.elk.matching.conclusions.SubClassInclusionComposedMatch1;
import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.owl.interfaces.ElkClassExpression;
import org.semanticweb.elk.owl.predefined.PredefinedElkIris;
import org.semanticweb.elk.reasoner.saturation.inferences.SubClassInclusionOwlThing;

public class SubClassInclusionOwlThingMatch1
		extends AbstractInferenceMatch<SubClassInclusionOwlThing> {

	private final IndexedContextRootMatch originMatch_;

	private final ElkClass owlThingMatch_;

	SubClassInclusionOwlThingMatch1(SubClassInclusionOwlThing parent,
			SubClassInclusionComposedMatch1 conclusionMatch) {
		super(parent);
		originMatch_ = conclusionMatch.getDestinationMatch();
		ElkClassExpression subsumerMatch = conclusionMatch
				.getSubsumerGeneralMatch();
		if (subsumerMatch instanceof ElkClass) {
			owlThingMatch_ = (ElkClass) subsumerMatch;
			if (owlThingMatch_.getIri().equals(PredefinedElkIris.OWL_THING)) {
				return;
			}
		}
		// else
		throw new ElkMatchException(parent.getConclusionSubsumer(),
				subsumerMatch);
	}

	public IndexedContextRootMatch getOriginMatch() {
		return originMatch_;
	}

	public ElkClass getSubsumerMatch() {
		return owlThingMatch_;
	}

	public SubClassInclusionComposedMatch1 getConclusionMatch(
			ConclusionMatchExpressionFactory factory) {
		return factory.getSubClassInclusionComposedMatch1(
				getParent().getConclusion(factory), originMatch_,
				owlThingMatch_);
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

		O visit(SubClassInclusionOwlThingMatch1 inferenceMatch1);

	}

	/**
	 * A factory for creating instances
	 * 
	 * @author Yevgeny Kazakov
	 *
	 */
	public interface Factory {

		SubClassInclusionOwlThingMatch1 getSubClassInclusionOwlThingMatch1(
				SubClassInclusionOwlThing parent,
				SubClassInclusionComposedMatch1 conclusionMatch);

	}

}
