/**
 * 
 */
package org.semanticweb.elk.proofs.inferences.properties;

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

import java.util.Collection;

import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyAxiom;
import org.semanticweb.elk.proofs.expressions.Expression;
import org.semanticweb.elk.proofs.expressions.SingleAxiomExpression;
import org.semanticweb.elk.proofs.sideconditions.SideCondition;

/**
 * The base class for class inferences whose conclusions are always object
 * property axioms.
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 */
abstract class AbstractPropertyInference implements PropertyInference {

	final Expression<ElkObjectPropertyAxiom> conclusion;

	AbstractPropertyInference(ElkObjectPropertyAxiom c) {
		conclusion = new SingleAxiomExpression<ElkObjectPropertyAxiom>(c);
	}

	@Override
	public Expression<ElkObjectPropertyAxiom> getConclusion() {
		return conclusion;
	}

	@Override
	public SideCondition getSideCondition() {
		return null;
	}

	@Override
	public Collection<? extends Expression<? extends ElkObjectPropertyAxiom>> getPremises() {
		// TODO Auto-generated method stub
		return null;
	}

	
}