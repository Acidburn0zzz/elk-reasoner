/*
 * #%L
 * elk-reasoner
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 Oxford University Computing Laboratory
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

package org.semanticweb.elk.reasoner.taxonomy;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.owl.interfaces.ElkEntity;
import org.semanticweb.elk.owl.interfaces.ElkNamedIndividual;
import org.semanticweb.elk.owl.printers.OwlFunctionalStylePrinter;
import org.semanticweb.elk.reasoner.taxonomy.model.ComparatorKeyProvider;
import org.semanticweb.elk.reasoner.taxonomy.model.TaxonomyNode;
import org.semanticweb.elk.reasoner.taxonomy.model.TypeNode;
import org.semanticweb.elk.reasoner.taxonomy.model.UpdateableInstanceNode;
import org.semanticweb.elk.reasoner.taxonomy.model.UpdateableInstanceTaxonomy;
import org.semanticweb.elk.reasoner.taxonomy.model.UpdateableTaxonomy;
import org.semanticweb.elk.reasoner.taxonomy.model.UpdateableTaxonomyNode;
import org.semanticweb.elk.reasoner.taxonomy.model.UpdateableTypeNode;
import org.semanticweb.elk.util.collections.ArrayHashSet;
import org.semanticweb.elk.util.collections.Operations;
import org.semanticweb.elk.util.collections.Operations.FunctorEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class taxonomy that is suitable for concurrent processing. Taxonomy objects
 * are only constructed for consistent ontologies, and some consequences of this
 * are hardcoded here.
 * 
 * This class wraps an instance of {@link UpdateableTaxonomy} and lazily
 * generates wrappers for its nodes to store direct instances.
 * 
 * @author Yevgeny Kazakov
 * @author Frantisek Simancik
 * @author Markus Kroetzsch
 * @author Pavel Klinov
 * @author Peter Skocovsky
 */
public class ConcurrentInstanceTaxonomy
		extends AbstractInstanceTaxonomy<ElkClass, ElkNamedIndividual>
		implements UpdateableInstanceTaxonomy<ElkClass, ElkNamedIndividual> {

	// logger for events
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(ConcurrentInstanceTaxonomy.class);

	/** provides keys that are used for hashing instead of the elkIndividuals */
	private final ComparatorKeyProvider<ElkEntity> individualKeyProvider_;
	/** thread safe map from class IRIs to individual nodes */
	private final ConcurrentMap<Object, IndividualNode> individualNodeLookup_;
	/** thread safe set of all individual nodes */
	private final Set<UpdateableInstanceNode<ElkClass, ElkNamedIndividual>> allIndividualNodes_;

	private final UpdateableTaxonomy<ElkClass> classTaxonomy_;

	private final ConcurrentMap<TaxonomyNode<ElkClass>, UpdateableTypeNodeWrapper> wrapperMap_;

	public ConcurrentInstanceTaxonomy(final ComparatorKeyProvider<ElkEntity> classKeyProvider,
			final ComparatorKeyProvider<ElkEntity> individualKeyProvider) {
		this(new ConcurrentClassTaxonomy(classKeyProvider), individualKeyProvider);
	}

	public ConcurrentInstanceTaxonomy(UpdateableTaxonomy<ElkClass> classTaxonomy,
			final ComparatorKeyProvider<ElkEntity> individualKeyProvider) {
		this.classTaxonomy_ = classTaxonomy;
		this.individualKeyProvider_ = individualKeyProvider;
		this.individualNodeLookup_ = new ConcurrentHashMap<Object, IndividualNode>();
		this.allIndividualNodes_ = Collections
				.newSetFromMap(new ConcurrentHashMap<UpdateableInstanceNode<ElkClass, ElkNamedIndividual>, Boolean>());
		this.wrapperMap_ = new ConcurrentHashMap<TaxonomyNode<ElkClass>, UpdateableTypeNodeWrapper>();
	}

	@Override
	public ComparatorKeyProvider<ElkEntity> getKeyProvider() {
		return classTaxonomy_.getKeyProvider();
	}
	
	@Override
	public ComparatorKeyProvider<ElkEntity> getInstanceKeyProvider() {
		return individualKeyProvider_;
	}
	
	@Override
	public UpdateableTypeNode<ElkClass, ElkNamedIndividual>
			getNode(ElkClass elkClass) {
		UpdateableTaxonomyNode<ElkClass> node = classTaxonomy_.getNode(elkClass);

		return getCreateUpdateableTypeNode(node);
	}

	/**
	 * Obtain a {@link TypeNode} object for a given {@link ElkClass}, or
	 * {@code null} if none assigned.
	 * 
	 * @param individual
	 * @return instance node object for elkClass, possibly still incomplete
	 */
	@Override
	public UpdateableInstanceNode<ElkClass, ElkNamedIndividual> getInstanceNode(
			ElkNamedIndividual individual) {
		return individualNodeLookup_.get(individualKeyProvider_.getKey(individual));
	}

	@Override
	public Set<? extends UpdateableTypeNode<ElkClass, ElkNamedIndividual>>
			getNodes() {

		return Operations.map(classTaxonomy_.getNodes(), functor_);
	}

	@Override
	public Set<? extends UpdateableInstanceNode<ElkClass, ElkNamedIndividual>>
			getInstanceNodes() {
		return Collections.unmodifiableSet(allIndividualNodes_);
	}

	@Override
	public IndividualNode getCreateInstanceNode(
			Collection<ElkNamedIndividual> members) {
		// search if some node is already assigned to some member, and if so
		// use this node and update its members if necessary
		IndividualNode previous = null;

		for (ElkNamedIndividual member : members) {
			previous = individualNodeLookup_.get(individualKeyProvider_.getKey(member));
			if (previous == null)
				continue;
			synchronized (previous) {
				if (previous.size() < members.size())
					previous.setMembers(members);
				else
					return previous;
			}
			// updating the index
			for (ElkNamedIndividual newMember : members) {
				individualNodeLookup_.put(individualKeyProvider_.getKey(newMember), previous);
			}

			return previous;
		}

		// TODO: avoid code duplication, the same technique is used for creating
		// non-bottom class nodes!

		IndividualNode node = new IndividualNode(members, individualKeyProvider_);
		// we first assign the node to the canonical member to avoid
		// concurrency problems
		ElkNamedIndividual canonical = node.getCanonicalMember();

		previous = individualNodeLookup_.putIfAbsent(individualKeyProvider_.getKey(canonical), node);

		if (previous != null) {
			return previous;
		}

		allIndividualNodes_.add(node);

		if (LOGGER_.isTraceEnabled()) {
			LOGGER_.trace(OwlFunctionalStylePrinter.toString(canonical)
					+ ": node created");
		}

		for (ElkNamedIndividual member : members) {
			if (member != canonical)
				individualNodeLookup_.put(individualKeyProvider_.getKey(member), node);
		}

		return node;
	}

	@Override
	public boolean removeInstanceNode(ElkNamedIndividual instance) {
		IndividualNode node = individualNodeLookup_.get(individualKeyProvider_.getKey(instance));

		if (node != null) {
			LOGGER_.trace("Removing the instance node {}", node);

			List<UpdateableTypeNode<ElkClass, ElkNamedIndividual>> directTypes = new LinkedList<UpdateableTypeNode<ElkClass, ElkNamedIndividual>>();

			synchronized (node) {
				for (ElkNamedIndividual individual : node) {
					individualNodeLookup_.remove(individualKeyProvider_.getKey(individual));
				}

				allIndividualNodes_.remove(node);
				directTypes.addAll(node.getDirectTypeNodes());
			}
			// detaching the removed instance node from all its direct types
			for (UpdateableTypeNode<ElkClass, ElkNamedIndividual> typeNode : directTypes) {
				synchronized (typeNode) {
					typeNode.removeDirectInstanceNode(node);
				}
			}

			return true;
		}
		// else
		return false;
	}

	@Override
	public UpdateableTypeNode<ElkClass, ElkNamedIndividual> getCreateNode(
			Collection<ElkClass> members) {
		UpdateableTaxonomyNode<ElkClass> taxNode = classTaxonomy_
				.getCreateNode(members);

		return getCreateUpdateableTypeNode(taxNode);
	}

	@Override
	public UpdateableTypeNode<ElkClass, ElkNamedIndividual> getTopNode() {
		return getCreateUpdateableTypeNode(classTaxonomy_.getTopNode());
	}

	@Override
	public UpdateableTypeNode<ElkClass, ElkNamedIndividual> getBottomNode() {
		return getCreateUpdateableTypeNode(classTaxonomy_.getBottomNode());
	}

	@Override
	public boolean addToBottomNode(ElkClass member) {
		return classTaxonomy_.addToBottomNode(member);
	}

	@Override
	public boolean removeFromBottomNode(ElkClass member) {
		return classTaxonomy_.removeFromBottomNode(member);
	}

	@Override
	public boolean removeNode(final ElkClass member) {
		final UpdateableTaxonomyNode<ElkClass> node =
				classTaxonomy_.getNode(member);
		if (node == null) {
			return false;
		}
		UpdateableTypeNodeWrapper wrapper = wrapperMap_.get(node);

		if (wrapper != null && wrapperMap_.remove(node, wrapper)) {

			for (UpdateableInstanceNode<ElkClass, ElkNamedIndividual> instanceNode : wrapper
					.getDirectInstanceNodes()) {
				synchronized (instanceNode) {
					instanceNode.removeDirectTypeNode(wrapper);
				}
			}
		}

		return classTaxonomy_.removeNode(member);
	}

	private UpdateableTypeNodeWrapper getCreateUpdateableTypeNode(
			UpdateableTaxonomyNode<ElkClass> taxNode) {
		if (taxNode == null) {
			return null;
		}

		synchronized (taxNode) {
			UpdateableTypeNodeWrapper wrapper = wrapperMap_.get(taxNode);

			if (wrapper == null) {
				wrapper = new UpdateableTypeNodeWrapper(taxNode);
				wrapperMap_.put(taxNode, wrapper);
			}

			return wrapper;
		}
	}

	/**
	 * Transforms updateable taxonomy nodes into updateable type nodes
	 */
	private final FunctorEx<UpdateableTaxonomyNode<ElkClass>, UpdateableTypeNodeWrapper> functor_ = new FunctorEx<UpdateableTaxonomyNode<ElkClass>, UpdateableTypeNodeWrapper>() {

		@Override
		public UpdateableTypeNodeWrapper apply(
				UpdateableTaxonomyNode<ElkClass> node) {
			return getCreateUpdateableTypeNode(node);
		}

		@Override
		public UpdateableTaxonomyNode<ElkClass> deapply(Object element) {
			if (element instanceof UpdateableTypeNodeWrapper) {
				return ((UpdateableTypeNodeWrapper) element).getNode();
			}
			// else
			return null;
		}

	};

	/**
	 * 
	 * @author Pavel Klinov
	 * 
	 *         pavel.klinov@uni-ulm.de
	 * @author Peter Skocovsky
	 */
	private class UpdateableTypeNodeWrapper implements
			UpdateableTypeNode<ElkClass, ElkNamedIndividual> {

		protected final UpdateableTaxonomyNode<ElkClass> classNode_;

		/**
		 * ElkNamedIndividual nodes whose members are instances of the members
		 * of this node.
		 */
		private final Set<UpdateableInstanceNode<ElkClass, ElkNamedIndividual>> directInstanceNodes_;

		UpdateableTypeNodeWrapper(UpdateableTaxonomyNode<ElkClass> node) {
			this.classNode_ = node;
			this.directInstanceNodes_ = Collections.newSetFromMap(
					new ConcurrentHashMap<UpdateableInstanceNode<ElkClass, ElkNamedIndividual>, Boolean>());
		}

		public UpdateableTaxonomyNode<ElkClass> getNode() {
			return classNode_;
		}

		@Override
		public ComparatorKeyProvider<ElkEntity> getKeyProvider() {
			return classNode_.getKeyProvider();
		}
		
		@Override
		public Iterator<ElkClass> iterator() {
			return classNode_.iterator();
		}
		
		@Override
		public boolean contains(ElkClass member) {
			return classNode_.contains(member);
		}
		
		@Override
		public int size() {
			return classNode_.size();
		}
		
		@Override
		public ElkClass getCanonicalMember() {
			return classNode_.getCanonicalMember();
		}

		@Override
		public Set<? extends UpdateableInstanceNode<ElkClass, ElkNamedIndividual>>
				getDirectInstanceNodes() {
			return Collections.unmodifiableSet(directInstanceNodes_);
		}

		@Override
		public Set<? extends UpdateableInstanceNode<ElkClass, ElkNamedIndividual>>
				getAllInstanceNodes() {
			Set<UpdateableInstanceNode<ElkClass, ElkNamedIndividual>> result;

			if (!classNode_.getDirectSubNodes().isEmpty()) {
				result = new ArrayHashSet<UpdateableInstanceNode<ElkClass, ElkNamedIndividual>>();
				Queue<UpdateableTypeNode<ElkClass, ElkNamedIndividual>> todo = new LinkedList<UpdateableTypeNode<ElkClass, ElkNamedIndividual>>();

				todo.add(this);

				while (!todo.isEmpty()) {
					UpdateableTypeNode<ElkClass, ElkNamedIndividual> next = todo.poll();
					result.addAll(next.getDirectInstanceNodes());

					for (UpdateableTypeNode<ElkClass, ElkNamedIndividual> nextSubNode : next
							.getDirectSubNodes()) {
						todo.add(nextSubNode);
					}
				}

				return Collections.unmodifiableSet(result);

			}
			// else
			return Collections.unmodifiableSet(getDirectInstanceNodes());
		}

		@Override
		public void addDirectSuperNode(
				UpdateableTypeNode<ElkClass, ElkNamedIndividual> superNode) {
			// FIXME: This is a dirty trick, that shouldn't be necessary with generic implementation
			getNode().addDirectSuperNode(
					((UpdateableTypeNodeWrapper) superNode).getNode());
		}

		@Override
		public void addDirectSubNode(
				UpdateableTypeNode<ElkClass, ElkNamedIndividual> subNode) {
			// FIXME: This is a dirty trick, that shouldn't be necessary with generic implementation
			getNode().addDirectSubNode(
					((UpdateableTypeNodeWrapper) subNode).getNode());
		}

		@Override
		public boolean removeDirectSubNode(
				UpdateableTypeNode<ElkClass, ElkNamedIndividual> subNode) {
			// FIXME: This is a dirty trick, that shouldn't be necessary with generic implementation
			return getNode().removeDirectSubNode(
					((UpdateableTypeNodeWrapper) subNode).getNode());
		}

		@Override
		public boolean removeDirectSuperNode(
				UpdateableTypeNode<ElkClass, ElkNamedIndividual> superNode) {
			// FIXME: This is a dirty trick, that shouldn't be necessary with generic implementation
			return getNode().removeDirectSuperNode(
					((UpdateableTypeNodeWrapper) superNode).getNode());
		}

		@Override
		public boolean trySetModified(boolean modified) {
			return getNode().trySetModified(modified);
		}

		@Override
		public boolean isModified() {
			return getNode().isModified();
		}

		@Override
		public Set<UpdateableTypeNodeWrapper> getDirectSuperNodes() {
			return Operations.map(getNode().getDirectSuperNodes(), functor_);
		}

		@Override
		public Set<UpdateableTypeNodeWrapper> getAllSuperNodes() {
			return Operations.map(getNode().getAllSuperNodes(), functor_);
		}

		@Override
		public Set<UpdateableTypeNodeWrapper> getDirectSubNodes() {
			return Operations.map(getNode().getDirectSubNodes(), functor_);
		}

		@Override
		public Set<UpdateableTypeNodeWrapper> getAllSubNodes() {
			return Operations.map(getNode().getAllSubNodes(), functor_);
		}

		@Override
		public void addDirectInstanceNode(
				UpdateableInstanceNode<ElkClass, ElkNamedIndividual> instanceNode) {
			LOGGER_.trace("{}: new direct instance-node {}", classNode_,
					instanceNode);

			directInstanceNodes_.add(instanceNode);
		}

		/*
		 * This method is not thread safe
		 */
		@Override
		public void removeDirectInstanceNode(
				UpdateableInstanceNode<ElkClass, ElkNamedIndividual> instanceNode) {
			LOGGER_.trace("{}: direct instance node removed {}", classNode_,
					instanceNode);

			directInstanceNodes_.remove(instanceNode);
		}

		@Override
		public String toString() {
			return classNode_.toString();
		}

		@Override
		public int hashCode() {
			return classNode_.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof UpdateableTypeNodeWrapper) {
				return classNode_ == ((UpdateableTypeNodeWrapper) obj).classNode_;
			}

			return false;
		}

	}

}
