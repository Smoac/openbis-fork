/*
 * Copyright 2009 ETH Zuerich, CISD
 *
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
 */

package ch.ethz.bsse.cisd.yeastlab.condition;

import ch.ethz.bsse.cisd.yeastlab.model.ParentCandidate;

/**
 * Interface encapsulating a single function (command) checking whether a {@link ParentCandidate}
 * satisfies certain condition.
 * 
 * @author Piotr Buczek
 */
public interface IBetterCandidateCondition
{
    /**
     * @returns <tt>true</tt> - if <var>candidate</var> satisfies the condition,<br>
     *          <tt>false</tt> - otherwise
     */
    boolean isBetter(ParentCandidate candidate);
}
