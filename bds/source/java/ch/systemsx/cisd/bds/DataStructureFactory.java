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

package ch.systemsx.cisd.bds;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IStorage;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Factory of data structures. Currently only structures compatible with Version 1.0 can be created.
 * 
 * @author Franz-Josef Elmer
 */
public final class DataStructureFactory
{
    private static final Factory<IDataStructure> factory = new Factory<IDataStructure>();

    static
    {
        factory.register(new Version(1, 0), DataStructureV1_0.class);
        factory.register(new Version(1, 1), DataStructureV1_1.class);
    }

    /**
     * Returns the class of the object returned after invoking
     * {@link #createDataStructure(IStorage, Version)}.
     * 
     * @param version Version of the data structure.
     * @throws DataStructureException if no data structure can be created for the specified version.
     */
    public static Class<? extends IDataStructure> getDataStructureClassFor(final Version version)
    {
        return factory.getClassFor(version);
    }

    /**
     * Creates a data structure for the specified version.
     * 
     * @param storage Storage behind the data structure.
     * @param version Version of the data structure to be created.
     * @throws EnvironmentFailureException found data structure class has not an appropriated
     *             constructor.
     * @throws DataStructureException if no data structure can be created for the specified version.
     */
    public static IDataStructure createDataStructure(final IStorage storage, final Version version)
    {
        return factory.create(IStorage.class, storage, version);
    }

    private DataStructureFactory()
    {
        // Can not be instantiated
    }
}
