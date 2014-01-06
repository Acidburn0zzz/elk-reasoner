package org.semanticweb.elk.reasoner.saturation.rules;

import java.io.Serializable;

import org.semanticweb.elk.reasoner.saturation.BasicSaturationStateWriter;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2012 Department of Computer Science, University of Oxford
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

/**
 * A rule that have a premise and can be applied to elements of a particular type modifiable by a
 * {@link BasicSaturationStateWriter}.
 * 
 * @author "Yevgeny Kazakov"
 * 
 * @param <E>
 *            the type of elements to which the rule can be applied
 */
public interface Rule<P, E> extends Serializable {

	/**
	 * Applying the rule to an element modifiable by
	 * {@link BasicSaturationStateWriter}
	 * 
	 * @param writer
	 *            a {@link BasicSaturationStateWriter} which could change the
	 *            element as a result of this rule's application
	 * @param element
	 *            the element to which the rule is applied
	 * @param premise
	 */
	public void apply(BasicSaturationStateWriter writer, P premise, E element);

	/**
	 * @return the name of this rule
	 */
	public String getName();

}
