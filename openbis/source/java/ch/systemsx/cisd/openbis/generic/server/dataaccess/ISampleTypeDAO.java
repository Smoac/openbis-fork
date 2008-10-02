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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * An interface that contains all data access operations on {@link SampleTypePE}s.
 * 
 * @author Christian Ribeaud
 */
public interface ISampleTypeDAO
{
    /**
     * @return The list of {@link SampleTypePE}s registered in the database.
     * @param onlyListable if true, then only types appropriate to be listed are returned.
     */
    public List<SampleTypePE> listSampleTypes(boolean onlyListable) throws DataAccessException;

    /**
     * For given <var>code</var> returns corresponding <code>SampleType</code>.
     * 
     * @return <code>null</code> if no type found.
     */
    public SampleTypePE tryFindSampleTypeByCode(String code) throws DataAccessException;

    public SampleTypePE tryFindByExample(SampleTypePE sampleType);
}