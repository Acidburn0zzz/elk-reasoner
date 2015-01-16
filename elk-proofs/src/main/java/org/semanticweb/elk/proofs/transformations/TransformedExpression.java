/**
 * 
 */
package org.semanticweb.elk.proofs.transformations;
/*
 * #%L
 * OWL API Proofs Model
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

import org.semanticweb.elk.owl.exceptions.ElkException;
import org.semanticweb.elk.proofs.expressions.ExpressionVisitor;
import org.semanticweb.elk.proofs.expressions.derived.DerivedExpression;
import org.semanticweb.elk.proofs.inferences.Inference;
import org.semanticweb.elk.util.collections.Operations;

/**
 * Generic base class for {@link DerivedExpression}s which transform their
 * {@link Inference}s before returning them from the method
 * {@link #getInferences()}. The transformation is done using the provided
 * instance of {@link Operations.Transformation}. A special case of
 * transformation is filtering, i.e., eliminating some inferences from the
 * output.
 * 
 * @author Pavel Klinov
 *
 *         pavel.klinov@uni-ulm.de
 */
abstract class TransformedExpression<D extends DerivedExpression, T extends Operations.Transformation<Inference, Iterable<Inference>>> implements DerivedExpression {

	protected final D expression;
	
	protected final T transformation; 
	
	protected final Operations.Transformation<Inference, TransformedInference<T>> propagation = new Operations.Transformation<Inference, TransformedInference<T>>() {

		@Override
		public TransformedInference<T> transform(Inference inf) {
			return propagateTransformation(inf);
		}}; 
	
	protected TransformedExpression(D expr, T f) {
		expression = expr;
		transformation = f;
	}
	
	@Override
	public <I, O> O accept(ExpressionVisitor<I, O> visitor, I input) {
		return expression.accept(visitor, input);
	}


	@Override
	public Iterable<TransformedInference<T>> getInferences() throws ElkException {
		return Operations.mapConcat(expression.getInferences(), new Operations.Transformation<Inference, Iterable<TransformedInference<T>>>() {

			@Override
			public Iterable<TransformedInference<T>> transform(Inference inf) {
				Iterable<Inference> transformed = transformation.transform(inf); 
				
				return Operations.map(transformed, propagation);
			}
			
		});
	}

	protected TransformedInference<T> propagateTransformation(Inference inf) {
		return new TransformedInference<T>(inf, transformation);
	}
	
	public T getFilterCondition() {
		return transformation;
	}

	@Override
	public String toString() {
		return expression.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return expression.equals(obj);
	}

	@Override
	public int hashCode() {
		return expression.hashCode();
	}
	
}