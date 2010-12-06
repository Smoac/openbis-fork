/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.util.List;

import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.utilities.UnicodeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.parser.BisTabFileLoader;
import ch.systemsx.cisd.openbis.generic.shared.parser.NamedInputStream;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser.UpdatedBasicExperimentParserObjectFactory;

/**
 * Loads experiments from the files.
 * 
 * @author Izabela Adamczyk
 */
public class UpdatedExperimentLoader
{
    private List<BatchRegistrationResult> results;

    private List<UpdatedBasicExperiment> newExperiments;

    private BisTabFileLoader<UpdatedBasicExperiment> tabFileLoader;

    public void load(Collection<NamedInputStream> files)
    {
        tabFileLoader =
                new BisTabFileLoader<UpdatedBasicExperiment>(
                        new IParserObjectFactoryFactory<UpdatedBasicExperiment>()
                            {
                                public final IParserObjectFactory<UpdatedBasicExperiment> createFactory(
                                        final IPropertyMapper propertyMapper)
                                        throws ParserException
                                {
                                    return new UpdatedBasicExperimentParserObjectFactory(
                                            propertyMapper);
                                }
                            }, false);
        newExperiments = new ArrayList<UpdatedBasicExperiment>();
        results = new ArrayList<BatchRegistrationResult>(files.size());
        for (final NamedInputStream file : files)
        {
            final Reader reader = UnicodeUtils.createReader(file.getInputStream());
            final List<UpdatedBasicExperiment> loadedExperiments =
                    tabFileLoader.load(new DelegatedReader(reader, file.getOriginalFilename()));
            newExperiments.addAll(loadedExperiments);
            results.add(new BatchRegistrationResult(file.getOriginalFilename(), String.format(
                    "Update of %d experiment(s) is complete.", loadedExperiments.size())));
        }
    }

    public List<BatchRegistrationResult> getResults()
    {
        return new ArrayList<BatchRegistrationResult>(results);
    }

    public List<UpdatedBasicExperiment> getNewBasicExperiments()
    {
        return new ArrayList<UpdatedBasicExperiment>(newExperiments);
    }

}