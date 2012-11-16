/*
 * #%L
 * elk-reasoner
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 Department of Computer Science, University of Oxford
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
package org.semanticweb.elk.reasoner.indexing.hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.semanticweb.elk.reasoner.indexing.IndexRuleChain;
import org.semanticweb.elk.reasoner.indexing.IndexRules;
import org.semanticweb.elk.reasoner.indexing.visitors.IndexedClassExpressionVisitor;
import org.semanticweb.elk.reasoner.indexing.visitors.IndexedObjectSomeValuesFromVisitor;
import org.semanticweb.elk.reasoner.saturation.SaturationState;
import org.semanticweb.elk.reasoner.saturation.conclusions.BackwardLink;
import org.semanticweb.elk.reasoner.saturation.conclusions.NegativeSuperClassExpression;
import org.semanticweb.elk.reasoner.saturation.conclusions.Propagation;
import org.semanticweb.elk.reasoner.saturation.context.Context;
import org.semanticweb.elk.reasoner.saturation.properties.SaturatedPropertyChain;
import org.semanticweb.elk.reasoner.saturation.rules.ContextRules;
import org.semanticweb.elk.reasoner.saturation.rules.RuleChain;
import org.semanticweb.elk.util.collections.ArrayHashSet;
import org.semanticweb.elk.util.collections.LazySetIntersection;
import org.semanticweb.elk.util.collections.chains.Chain;
import org.semanticweb.elk.util.collections.chains.Matcher;
import org.semanticweb.elk.util.collections.chains.ReferenceFactory;
import org.semanticweb.elk.util.collections.chains.SimpleTypeBasedMatcher;

/**
 * Represents all occurrences of an ElkObjectSomeValuesFrom in an ontology.
 * 
 * @author Frantisek Simancik
 * 
 */
public class IndexedObjectSomeValuesFrom extends IndexedClassExpression {

	// logger for this class
	// private static final Logger LOGGER_ =
	// Logger.getLogger(IndexedObjectSomeValuesFrom.class);

	protected final IndexedObjectProperty property;

	protected final IndexedClassExpression filler;

	IndexedObjectSomeValuesFrom(IndexedObjectProperty indexedObjectProperty,
			IndexedClassExpression filler) {
		this.property = indexedObjectProperty;
		this.filler = filler;
	}

	/**
	 * @return The indexed object property comprising this ObjectSomeValuesFrom.
	 */
	public IndexedObjectProperty getRelation() {
		return property;
	}

	/**
	 * @return The indexed class expression comprising this
	 *         ObjectSomeValuesFrom.
	 */
	public IndexedClassExpression getFiller() {
		return filler;
	}

	public <O> O accept(IndexedObjectSomeValuesFromVisitor<O> visitor) {
		return visitor.visit(this);
	}

	@Override
	public <O> O accept(IndexedClassExpressionVisitor<O> visitor) {
		return accept((IndexedObjectSomeValuesFromVisitor<O>) visitor);
	}

	@Override
	protected void updateOccurrenceNumbers(final IndexUpdater indexUpdater,
			final int increment, final int positiveIncrement,
			final int negativeIncrement) {

		if (negativeOccurrenceNo == 0 && negativeIncrement > 0) {
			// first negative occurrence of this expression
			// register the composition rule for the filler
			indexUpdater.add(filler, new ThisCompositionRule(this));
		}

		if (positiveOccurrenceNo == 0 && positiveIncrement > 0) {
			// first positive occurrence of this expression
			indexUpdater.add(filler, new PosExistentialRule(property));
		}

		positiveOccurrenceNo += positiveIncrement;
		negativeOccurrenceNo += negativeIncrement;

		if (negativeOccurrenceNo == 0 && negativeIncrement < 0) {
			// no negative occurrences of this expression left
			indexUpdater.remove(filler, new ThisCompositionRule(this));
		}

		if (positiveOccurrenceNo == 0 && positiveIncrement < 0) {
			// no positive occurrences of this expression left
			indexUpdater.remove(filler, new PosExistentialRule(property));
		}
	}

	@Override
	public String toString() {
		return "ObjectSomeValuesFrom(" + this.property + ' ' + this.filler
				+ ')';
	}

	@Override
	public void applyDecompositionRule(SaturationState.Writer writer,
			Context context) {
		/*
		 * RuleStatistics stats = ruleEngine.getRulesTimer();
		 * 
		 * stats.timeObjectSomeValuesFromDecompositionRule -=
		 * CachedTimeThread.currentTimeMillis;
		 * stats.countObjectSomeValuesFromDecompositionRule++;
		 */

		try {
			writer.produce(writer.getCreateContext(filler), new BackwardLink(
					context, property));
		} finally {
			// stats.timeObjectSomeValuesFromDecompositionRule +=
			// CachedTimeThread.currentTimeMillis;
		}
	}

	/**
	 * 
	 */
	private static class ThisCompositionRule extends ContextRules {

		private final Collection<IndexedObjectSomeValuesFrom> negExistentials_;

		private ThisCompositionRule(RuleChain<Context> tail) {
			super(tail);
			this.negExistentials_ = new ArrayList<IndexedObjectSomeValuesFrom>(
					1);
		}

		ThisCompositionRule(IndexedObjectSomeValuesFrom negExistential) {
			super(null);
			this.negExistentials_ = new ArrayList<IndexedObjectSomeValuesFrom>(
					1);
			this.negExistentials_.add(negExistential);
		}

		private boolean addNegExistential(
				IndexedObjectSomeValuesFrom existential) {
			return negExistentials_.add(existential);
		}

		private boolean removeNegExistential(
				IndexedObjectSomeValuesFrom existential) {
			return negExistentials_.remove(existential);
		}

		/**
		 * @return {@code true} if this rule never does anything
		 */
		private boolean isEmpty() {
			return negExistentials_.isEmpty();
		}

		@Override
		public void apply(SaturationState.Writer writer, Context context) {

			/*
			 * RuleStatistics stats = ruleEngine.getRulesTimer();
			 * 
			 * stats.timeObjectSomeValuesFromCompositionRule -=
			 * CachedTimeThread.currentTimeMillis;
			 * stats.countObjectSomeValuesFromCompositionRule++;
			 */

			try {

				final Set<IndexedPropertyChain> candidatePropagationProperties = context
						.getRoot().getPosPropertiesInExistentials();

				if (candidatePropagationProperties == null) {
					return;
				}

				for (IndexedObjectSomeValuesFrom e : negExistentials_) {
					IndexedPropertyChain relation = e.getRelation();
					/*
					 * creating propagations for relevant sub-properties of the
					 * relation
					 */
					/*
					 * for (IndexedPropertyChain property :
					 * relation.getSaturated().getSubProperties()) {
					 * state.produce(context, new Propagation(property, e)); }
					 */
					for (IndexedPropertyChain property : new LazySetIntersection<IndexedPropertyChain>(
							candidatePropagationProperties, relation
									.getSaturated().getSubProperties())) {
						writer.produce(context, new Propagation(property, e));
					}

					/*
					 * creating propagations for relevant sub-compositions of
					 * the relation
					 */
					for (IndexedPropertyChain property : relation
							.getSaturated().getSubCompositions()) {
						SaturatedPropertyChain propertySaturation = property
								.getSaturated();

						// if
						// (!propertySaturation.getRightSubProperties().isEmpty())
						// {
						if (!new LazySetIntersection<IndexedPropertyChain>(
								candidatePropagationProperties,
								propertySaturation.getRightSubProperties())
								.isEmpty()) {
							/*
							 * create propagations for told super-properties of
							 * the chain instead of the chain itself if the
							 * optimization is on. otherwise a composed backward
							 * link will be created for a super-property while
							 * the propagation for the chain, so we can lose the
							 * entailment.
							 */
							if (SaturatedPropertyChain.REPLACE_CHAINS_BY_TOLD_SUPER_PROPERTIES
									&& property.getRightChains() == null) {
								for (IndexedPropertyChain superChain : property
										.getToldSuperProperties()) {
									writer.produce(context, new Propagation(
											superChain, e));
								}
							} else {
								writer.produce(context, new Propagation(
										property, e));
							}
						}
					}

					// propagating to the this context if relation is reflexive
					if (relation.getSaturated().isReflexive())
						writer.produce(context,
								new NegativeSuperClassExpression(e));
				}
			} finally {
				// stats.timeObjectSomeValuesFromCompositionRule +=
				// CachedTimeThread.currentTimeMillis;
			}
		}

		private static Matcher<RuleChain<Context>, ThisCompositionRule> MATCHER_ = new SimpleTypeBasedMatcher<RuleChain<Context>, ThisCompositionRule>(
				ThisCompositionRule.class);

		private static ReferenceFactory<RuleChain<Context>, ThisCompositionRule> FACTORY_ = new ReferenceFactory<RuleChain<Context>, ThisCompositionRule>() {
			@Override
			public ThisCompositionRule create(RuleChain<Context> tail) {
				return new ThisCompositionRule(tail);
			}
		};

		@Override
		public boolean addTo(Chain<RuleChain<Context>> ruleChain) {
			ThisCompositionRule rule = ruleChain.getCreate(MATCHER_, FACTORY_);
			boolean changed = false;

			for (IndexedObjectSomeValuesFrom negExistential : negExistentials_) {
				changed |= rule.addNegExistential(negExistential);
			}

			return changed;

		}

		@Override
		public boolean removeFrom(Chain<RuleChain<Context>> ruleChain) {
			boolean changed = false;
			ThisCompositionRule rule = ruleChain.find(MATCHER_);

			if (rule != null) {
				for (IndexedObjectSomeValuesFrom negExistential : negExistentials_) {
					changed |= rule.removeNegExistential(negExistential);
				}

				if (rule.isEmpty()) {
					ruleChain.remove(MATCHER_);
					changed = true;
				}
			}

			return changed;

		}

	}

	/**
	 * 
	 */
	private static class PosExistentialRule extends
			IndexRules<IndexedClassExpression> {

		private final Set<IndexedObjectProperty> properties_ = new ArrayHashSet<IndexedObjectProperty>(
				16);

		private PosExistentialRule(IndexRuleChain<IndexedClassExpression> tail) {
			super(tail);
		}

		PosExistentialRule(IndexedObjectProperty property) {
			super(null);
			properties_.add(property);
		}

		private static Matcher<IndexRuleChain<IndexedClassExpression>, PosExistentialRule> MATCHER_ = new SimpleTypeBasedMatcher<IndexRuleChain<IndexedClassExpression>, PosExistentialRule>(
				PosExistentialRule.class);

		private static ReferenceFactory<IndexRuleChain<IndexedClassExpression>, PosExistentialRule> FACTORY_ = new ReferenceFactory<IndexRuleChain<IndexedClassExpression>, PosExistentialRule>() {
			@Override
			public PosExistentialRule create(
					IndexRuleChain<IndexedClassExpression> tail) {
				return new PosExistentialRule(tail);
			}
		};

		@Override
		public boolean addTo(
				Chain<IndexRuleChain<IndexedClassExpression>> ruleChain) {
			PosExistentialRule rule = ruleChain.getCreate(MATCHER_, FACTORY_);

			return rule.properties_.addAll(properties_);
		}

		@Override
		public boolean removeFrom(
				Chain<IndexRuleChain<IndexedClassExpression>> ruleChain) {
			PosExistentialRule rule = ruleChain.find(MATCHER_);
			boolean changed = false;

			if (rule != null) {
				changed = rule.properties_.removeAll(properties_);

				if (rule.properties_.isEmpty()) {
					ruleChain.remove(MATCHER_);
				}
			}

			return changed;
		}

		@Override
		public boolean apply(IndexedClassExpression filler) {
			boolean changed = false;

			for (IndexedObjectProperty property : properties_) {
				changed |= filler.addPosPropertyInExistential(property);
			}

			return changed;
		}

		@Override
		public boolean deapply(IndexedClassExpression filler) {
			boolean changed = false;

			for (IndexedObjectProperty property : properties_) {
				changed |= filler.removePosPropertyInExistential(property);
			}

			return changed;
		}

	}
}