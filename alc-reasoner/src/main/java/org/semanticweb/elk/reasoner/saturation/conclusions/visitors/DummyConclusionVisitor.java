package org.semanticweb.elk.reasoner.saturation.conclusions.visitors;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2014 Department of Computer Science, University of Oxford
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

import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.BackwardLink;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.Clash;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.ComposedSubsumer;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.Conclusion;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.ContextInitialization;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.DecomposedSubsumer;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.Disjunction;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.ForwardLink;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.NegatedSubsumer;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.NegativePropagation;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.PossibleComposedSubsumer;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.PossibleDecomposedSubsumer;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.PossiblePropagatedExistential;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.PropagatedClash;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.Propagation;

/**
 * A {@link ConclusionVisitor} that does nothing for the visited
 * {@link Conclusion}. Used as a skeleton to construct visitors so not to forget
 * to use new types of {@link Conclusion}s.
 * 
 * @author "Yevgeny Kazakov"
 * 
 * @param <I>
 *            the type of input parameter with which this visitor works
 * @param <O>
 *            the type of output parameter with which this visitor works
 */
class DummyConclusionVisitor<I, O> implements ConclusionVisitor<I, O> {

	/**
	 * Dummy default visiting method
	 * 
	 * @param conclusion
	 * @param input
	 * @return
	 */
	protected O defaultVisit(Conclusion conclusion, I input) {
		// does nothing
		return null;
	}

	@Override
	public O visit(ComposedSubsumer conclusion, I input) {
		return defaultVisit(conclusion, input);
	}

	@Override
	public O visit(DecomposedSubsumer conclusion, I input) {
		return defaultVisit(conclusion, input);
	}

	@Override
	public O visit(PossibleComposedSubsumer conclusion, I input) {
		return defaultVisit(conclusion, input);
	}

	@Override
	public O visit(PossibleDecomposedSubsumer conclusion, I input) {		
		return defaultVisit(conclusion, input);
	}

	@Override
	public O visit(BackwardLink conclusion, I input) {
		return defaultVisit(conclusion, input);
	}

	@Override
	public O visit(ContextInitialization conclusion, I input) {
		return defaultVisit(conclusion, input);
	}

	@Override
	public O visit(ForwardLink conclusion, I input) {
		return defaultVisit(conclusion, input);
	}

	@Override
	public O visit(NegatedSubsumer conclusion, I input) {
		return defaultVisit(conclusion, input);
	}

	@Override
	public O visit(Disjunction conclusion, I input) {
		return defaultVisit(conclusion, input);
	}

	@Override
	public O visit(Propagation conclusion, I input) {
		return defaultVisit(conclusion, input);
	}

	@Override
	public O visit(NegativePropagation conclusion, I input) {
		return defaultVisit(conclusion, input);
	}

	@Override
	public O visit(Clash conclusion, I input) {
		return defaultVisit(conclusion, input);
	}

	@Override
	public O visit(PropagatedClash conclusion, I input) {
		return defaultVisit(conclusion, input);
	}

	@Override
	public O visit(PossiblePropagatedExistential conclusion, I input) {
		return defaultVisit(conclusion, input);
	}

}
