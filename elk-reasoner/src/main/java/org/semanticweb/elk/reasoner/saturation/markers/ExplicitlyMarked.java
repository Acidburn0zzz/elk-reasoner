/*
 * #%L
 * ELK Reasoner
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
package org.semanticweb.elk.reasoner.saturation.markers;



/**
 * Represents a non-definite element marked by at least one marker.
 * 
 * @author Frantisek Simancik
 * 
 */
public class ExplicitlyMarked<T> implements Marked<T> {
	
	protected final Markers markers;
	protected final T key;

	public ExplicitlyMarked(T key, Markers markers) {
		this.markers = markers;
		this.key = key;
	}
	
	public T getKey() {
		return key;
	}

	public Markers getMarkers() {
		return markers;
	}
}
