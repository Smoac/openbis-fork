/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.AbstractDataSetByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.AbstractValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
@Component
public class DataSetAuthorizationValidator implements IDataSetAuthorizationValidator
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    public Set<Long> validate(PersonPE person, Collection<Long> dataSetIds)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);
        List<DataSetAuthorizationRecord> records = query.getAuthorizations(new LongOpenHashSet(dataSetIds));
        AbstractValidator<DataSetAuthorizationRecord> validator =
                new AbstractDataSetByExperimentOrSampleIdentifierValidator<DataSetAuthorizationRecord>()
                    {

                        @Override
                        protected boolean isStorageConfirmed(DataSetAuthorizationRecord record)
                        {
                            if (record.isStorageConfirmed != null)
                            {
                                return record.isStorageConfirmed;
                            } else
                            {
                                return true;
                            }
                        }

                        @Override
                        protected String getExperimentIdentifier(DataSetAuthorizationRecord record)
                        {
                            if (record.experimentCode != null)
                            {
                                return new ExperimentIdentifier(record.experimentSpaceCode, record.experimentProjectCode, record.experimentCode)
                                        .getIdentifier();
                            } else
                            {
                                return null;
                            }
                        }

                        @Override
                        protected String getSampleIdentifier(DataSetAuthorizationRecord record)
                        {
                            if (record.sampleCode != null)
                            {
                                return new SampleIdentifier(record.sampleSpaceCode, record.sampleContainerCode, record.sampleCode).getIdentifier();
                            } else
                            {
                                return null;
                            }
                        }

                    };

        validator.init(new AuthorizationDataProvider(daoFactory));

        Set<Long> result = new HashSet<Long>();

        for (DataSetAuthorizationRecord record : records)
        {
            if (validator.doValidation(person, record))
            {
                result.add(record.id);
            }
        }

        return result;
    }

}
