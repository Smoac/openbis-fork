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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.sql;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lemnik.eodsql.QueryTool;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class SampleAuthorizationSqlValidator implements ISampleAuthorizationSqlValidator
{

    @Override
    public Set<Long> validate(PersonPE person, Collection<Long> sampleIds)
    {
        SampleQuery query = QueryTool.getManagedQuery(SampleQuery.class);
        List<SampleAuthorizationRecord> records = query.getAuthorizations(new LongOpenHashSet(sampleIds));
        SampleByIdentiferValidator validator = new SampleByIdentiferValidator();
        Set<Long> result = new HashSet<Long>();

        for (SampleAuthorizationRecord record : records)
        {
            final SampleAuthorizationRecord theRecord = record;

            if (validator.doValidation(person, new IIdentifierHolder()
                {
                    @Override
                    public String getIdentifier()
                    {
                        return new SampleIdentifier(theRecord.spaceCode, theRecord.containedCode, theRecord.code).getIdentifier();
                    }
                }))
            {
                result.add(record.id);
            }
        }

        return result;
    }

}
