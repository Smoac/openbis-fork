/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.parser.BisTabFileLoader;
import ch.systemsx.cisd.openbis.generic.shared.parser.NamedInputStream;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser.UpdatedDataSetParserObjectFactory;

/**
 * Loads data sets from the files.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetLoader
{

    private List<BatchRegistrationResult> results;

    private List<NewDataSet> newObjects;

    private BisTabFileLoader<NewDataSet> tabFileLoader;

    public void load(Collection<NamedInputStream> files)
    {
        tabFileLoader =
                new BisTabFileLoader<NewDataSet>(new IParserObjectFactoryFactory<NewDataSet>()
                    {
                        @Override
                        public final IParserObjectFactory<NewDataSet> createFactory(
                                final IPropertyMapper propertyMapper) throws ParserException
                        {
                            return new UpdatedDataSetParserObjectFactory(propertyMapper);
                        }
                    }, false);
        newObjects = new ArrayList<NewDataSet>();
        results = new ArrayList<BatchRegistrationResult>(files.size());
        for (final NamedInputStream file : files)
        {
            final Reader reader = file.getUnicodeReader();
            final Map<String, String> defaults = Collections.emptyMap();
            final List<NewDataSet> loadedMaterials =
                    tabFileLoader.load(new DelegatedReader(reader, file.getOriginalFilename()),
                            defaults);
            newObjects.addAll(loadedMaterials);
            results.add(new BatchRegistrationResult(file.getOriginalFilename(), String.format(
                    "%d data set(s) found and registered.", loadedMaterials.size())));
        }
    }

    public List<BatchRegistrationResult> getResults()
    {
        return new ArrayList<BatchRegistrationResult>(results);
    }

    public List<NewDataSet> getNewDataSets()
    {
        return new ArrayList<NewDataSet>(newObjects);
    }

}