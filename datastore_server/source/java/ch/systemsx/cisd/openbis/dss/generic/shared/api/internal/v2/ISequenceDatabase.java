/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SequenceSearchResult;

/**
 * Interface for a database for nucleotid or protein sequences.
 * Implenting classes should have a public constructor with two arguments: First is an instance of
 * {@link Properties} and second is an instance of {@link File} which points to the root of the data set store.
 *
 * @author Franz-Josef Elmer
 */
public interface ISequenceDatabase
{
    public String getName();
    
    public boolean isAvailable();
    
    public List<SequenceSearchResult> search(String sequenceSnippet, Map<String, String> optionalParameters);
}
