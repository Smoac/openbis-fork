/*
 * Copyright 2007 ETH Zuerich, CISD
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
 * A roles that allows the termination of an operation.
 * 
 * @author Bernd Rinn
 */
public interface ITerminable
{

    /** Dummy implementation of an {@link ITerminable}. */
    public static final ITerminable DUMMY = new ITerminable()
        {
            public boolean terminate()
            {
                // We are a dummy.
                return false;
            }
        };

    /**
     * Terminates the {@link ITerminable}.
     * 
     * @return <code>true</code> if and only if the {@link ITerminable} has terminated successfully.
     */
    public boolean terminate();

}
