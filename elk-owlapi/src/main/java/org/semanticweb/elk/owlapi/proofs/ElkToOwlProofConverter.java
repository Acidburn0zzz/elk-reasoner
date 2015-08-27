/**
 * 
 */
package org.semanticweb.elk.owlapi.proofs;
/*
 * #%L
 * ELK OWL API Binding
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

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.elk.owl.AbstractElkAxiomVisitor;
import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owl.interfaces.ElkClassAssertionAxiom;
import org.semanticweb.elk.owl.interfaces.ElkClassExpression;
import org.semanticweb.elk.owl.interfaces.ElkDisjointClassesAxiom;
import org.semanticweb.elk.owl.interfaces.ElkEquivalentClassesAxiom;
import org.semanticweb.elk.owl.interfaces.ElkIndividual;
import org.semanticweb.elk.owl.interfaces.ElkObjectInverseOf;
import org.semanticweb.elk.owl.interfaces.ElkObjectProperty;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyChain;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyDomainAxiom;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyExpression;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyRangeAxiom;
import org.semanticweb.elk.owl.interfaces.ElkReflexiveObjectPropertyAxiom;
import org.semanticweb.elk.owl.interfaces.ElkSubClassOfAxiom;
import org.semanticweb.elk.owl.interfaces.ElkSubObjectPropertyOfAxiom;
import org.semanticweb.elk.owl.interfaces.ElkTransitiveObjectPropertyAxiom;
import org.semanticweb.elk.owl.visitors.ElkSubObjectPropertyExpressionVisitor;
import org.semanticweb.elk.owlapi.wrapper.ElkAxiomWrap;
import org.semanticweb.elk.proofs.expressions.AxiomExpression;
import org.semanticweb.elk.proofs.expressions.Expression;
import org.semanticweb.elk.proofs.expressions.ExpressionVisitor;
import org.semanticweb.elk.proofs.expressions.LemmaExpression;
import org.semanticweb.elk.proofs.inferences.Inference;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapitools.proofs.OWLInference;
import org.semanticweb.owlapitools.proofs.expressions.OWLAxiomExpression;
import org.semanticweb.owlapitools.proofs.expressions.OWLExpression;

/**
 * Converts ELK axioms to the OWL API representation.
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 */
public class ElkToOwlProofConverter {

	public static OWLExpression convert(Expression expression) {
		return expression.accept(new ExpressionVisitor<Void, OWLExpression>() {

			@Override
			public OWLExpression visit(	AxiomExpression<?> expr, Void input) {
				return convert(expr);
			}

			@Override
			public OWLExpression visit(LemmaExpression<?> expr, Void input) {
				return new LemmaExpressionWrap(expr);
			}
		}, null);
	}
	
	public static OWLAxiomExpression convert(AxiomExpression<?> expression) {
		return new AxiomExpressionWrap(expression);
	}

	public static OWLInference convert(Inference inference) {
		return new InferenceWrap(inference);
	}

	public static OWLAxiom convert(ElkAxiom axiom) {
		final OWLDataFactory factory = OWLManager.getOWLDataFactory();

		return axiom.accept(new AbstractElkAxiomVisitor<OWLAxiom>() {

			@Override
			protected OWLAxiom defaultLogicalVisit(ElkAxiom axiom) {
				// shouldn't get here
				throw new IllegalStateException("Conversion of " + axiom.getClass().getSimpleName() + " not implemented");
			}

			@Override
			protected OWLAxiom defaultNonLogicalVisit(ElkAxiom axiom) {
				throw new IllegalStateException("Conversion of " + axiom.getClass().getSimpleName() + " not implemented");
			}

			@SuppressWarnings("unchecked")
			private OWLAxiom asOWLAxiom(ElkAxiom axiom) {
				// check if the ELK axiom is a wrapper around the original OWL API axiom which comes from the source ontology
				return (axiom instanceof ElkAxiomWrap<?>) ? ((ElkAxiomWrap<OWLAxiom>) axiom).getOWLAxiom() : null;
			}

			@Override
			public OWLAxiom visit(ElkDisjointClassesAxiom axiom) {
				OWLAxiom ax = asOWLAxiom(axiom);
				
				if (ax != null) {
					return ax;
				}

				return factory.getOWLDisjointClassesAxiom(ClassExpressionConverter.convertClasses(axiom.getClassExpressions(), new ClassExpressionConverter(factory)));
			}

			@Override
			public OWLAxiom visit(
					ElkEquivalentClassesAxiom axiom) {
				OWLAxiom ax = asOWLAxiom(axiom);
				
				if (ax != null) {
					return ax;
				}
				
				return factory.getOWLEquivalentClassesAxiom(ClassExpressionConverter.convertClasses(axiom.getClassExpressions(), new ClassExpressionConverter(factory)));
			}

			@Override
			public OWLAxiom visit(ElkSubClassOfAxiom axiom) {
				OWLAxiom ax = asOWLAxiom(axiom);
				
				if (ax != null) {
					return ax;
				}

				return factory.getOWLSubClassOfAxiom(convert(axiom.getSubClassExpression(), factory), convert(axiom.getSuperClassExpression(), factory));
			}
			
			

			@Override
			public OWLAxiom visit(ElkObjectPropertyRangeAxiom axiom) {
				OWLAxiom ax = asOWLAxiom(axiom);
				
				if (ax != null) {
					return ax;
				}

				return factory.getOWLObjectPropertyDomainAxiom(convert(axiom.getProperty(), factory), convert(axiom.getRange(), factory));
			}

			@Override
			public OWLAxiom visit(ElkClassAssertionAxiom axiom) {
				OWLAxiom ax = asOWLAxiom(axiom);
				
				if (ax != null) {
					return ax;
				}

				return factory.getOWLClassAssertionAxiom(convert(axiom.getClassExpression(), factory), convert(axiom.getIndividual(), factory));
			}

			@Override
			public OWLAxiom visit(ElkObjectPropertyDomainAxiom axiom) {
				OWLAxiom ax = asOWLAxiom(axiom);
				
				if (ax != null) {
					return ax;
				}
				
				return factory.getOWLObjectPropertyDomainAxiom(convert(axiom.getProperty(), factory),  convert(axiom.getDomain(), factory));
			}

			@Override
			public OWLAxiom visit(ElkReflexiveObjectPropertyAxiom axiom) {
				OWLAxiom ax = asOWLAxiom(axiom);
				
				if (ax != null) {
					return ax;
				}
				
				return factory.getOWLReflexiveObjectPropertyAxiom(convert(axiom.getProperty(), factory));
			}
			
			@Override
			public OWLAxiom visit(ElkTransitiveObjectPropertyAxiom axiom) {
				OWLAxiom ax = asOWLAxiom(axiom);
				
				if (ax != null) {
					return ax;
				}
				
				return factory.getOWLTransitiveObjectPropertyAxiom(convert(axiom.getProperty(), factory));
			}

			@Override
			public OWLAxiom visit(ElkSubObjectPropertyOfAxiom axiom) {
				OWLAxiom ax = asOWLAxiom(axiom);
				
				if (ax != null) {
					return ax;
				}
				
				final OWLObjectPropertyExpression sup = convert(axiom.getSuperObjectPropertyExpression(), factory);
				
				return axiom.getSubObjectPropertyExpression().accept(new ElkSubObjectPropertyExpressionVisitor<OWLAxiom>() {

					@Override
					public OWLAxiom visit(ElkObjectInverseOf p) {
						return factory.getOWLSubObjectPropertyOfAxiom(factory.getOWLObjectInverseOf(convert(p, factory)), sup);
					}

					@Override
					public OWLAxiom visit(ElkObjectProperty p) {
						return factory.getOWLSubObjectPropertyOfAxiom(convert(p, factory), sup);
					}

					@Override
					public OWLAxiom visit(ElkObjectPropertyChain chain) {
						List<OWLObjectPropertyExpression> converted = new ArrayList<OWLObjectPropertyExpression>(chain.getObjectPropertyExpressions().size());
						ObjectPropertyExpressionConverter converter = new ObjectPropertyExpressionConverter(factory);
						
						for (ElkObjectPropertyExpression pe : chain.getObjectPropertyExpressions()) {
							converted.add(pe.accept(converter));
						}
						
						return factory.getOWLSubPropertyChainOfAxiom(converted, sup);
					}
					
				});
				
			}

		});
	}

	static OWLClassExpression convert(final ElkClassExpression ce, final OWLDataFactory factory) {
		return ce.accept(new ClassExpressionConverter(factory));
	}
	
	static OWLIndividual convert(final ElkIndividual ind, final OWLDataFactory factory) {
		return ind.accept(new IndividualConverter(factory));
	}
	
	static OWLObjectPropertyExpression convert(final ElkObjectPropertyExpression ce, final OWLDataFactory factory) {
		return ce.accept(new ObjectPropertyExpressionConverter(factory));
	}
	
}