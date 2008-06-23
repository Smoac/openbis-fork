/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

/**
 * A <code>ITriggerable</code> that chains a sequence of one or more {@link ITriggerable}s.
 * 
 * @author Christian Ribeaud
 */
public final class CompoundTriggerable implements ITriggerable
{
    private final ITriggerable[] triggerables;

    public CompoundTriggerable(final ITriggerable... triggerables)
    {
        assert triggerables != null : "Unspecified ITriggerable implementations.";
        this.triggerables = triggerables;
    }

    //
    // ITriggerable
    //

    public final void trigger()
    {
        for (final ITriggerable triggerable : triggerables)
        {
            triggerable.trigger();
        }
    }

}
