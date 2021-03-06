/*-
 * #%L
 * ELK Reasoner Core
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
package org.semanticweb.elk.reasoner;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomReasonerInterrupter extends ReasonerInterrupter {

	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(ReasonerInterrupter.class);

	private final Random random_;
	private double chance_;

	public RandomReasonerInterrupter(final Random random, final double chance) {
		if (chance < 0 || chance > 1) {
			throw new IllegalArgumentException(
					"chance must be between 0 and 1 inclusive!");
		}
		this.random_ = random;
		this.chance_ = chance;
	}

	@Override
	public boolean isInterrupted() {
		if (random_.nextDouble() < chance_) {
			LOGGER_.debug("========== INTERRUPT ==========");
			interrupt();
		}
		return super.isInterrupted();
	}

	public double getChance() {
		return chance_;
	}

	public void setChance(final double chance) {
		if (chance < 0 || chance > 1) {
			throw new IllegalArgumentException(
					"chance must be between 0 and 1 inclusive!");
		}
		this.chance_ = chance;
	}

	public Random getRandom() {
		return random_;
	}

}
