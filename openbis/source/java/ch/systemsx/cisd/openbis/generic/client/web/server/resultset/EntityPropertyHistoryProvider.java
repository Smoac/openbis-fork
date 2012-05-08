/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityPropertyHistoryGridColumnIDs.AUTHOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityPropertyHistoryGridColumnIDs.MATERIAL;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityPropertyHistoryGridColumnIDs.PROPERTY_TYPE_CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityPropertyHistoryGridColumnIDs.PROPERTY_TYPE_LABEL;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityPropertyHistoryGridColumnIDs.VALID_FROM_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityPropertyHistoryGridColumnIDs.VALID_UNTIL_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityPropertyHistoryGridColumnIDs.VALUE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityPropertyHistoryGridColumnIDs.VOCABULARY_TERM;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListEntityPropertyHistoryCriteria;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityPropertyHistory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class EntityPropertyHistoryProvider extends
        AbstractCommonTableModelProvider<EntityPropertyHistory>
{
    private final ListEntityPropertyHistoryCriteria criteria;

    public EntityPropertyHistoryProvider(ICommonServer commonServer, String sessionToken,
            ListEntityPropertyHistoryCriteria criteria)
    {
        super(commonServer, sessionToken);
        this.criteria = criteria;
    }

    @Override
    protected TypedTableModel<EntityPropertyHistory> createTableModel()
    {
        List<EntityPropertyHistory> history =
                commonServer.listEntityPropertyHistory(sessionToken, criteria.getEntityKind(),
                        criteria.getEntityID());
        TypedTableModelBuilder<EntityPropertyHistory> builder =
                new TypedTableModelBuilder<EntityPropertyHistory>();
        builder.addColumn(PROPERTY_TYPE_CODE).hideByDefault();
        builder.addColumn(PROPERTY_TYPE_LABEL);
        builder.addColumn(VALUE);
        builder.addColumn(VOCABULARY_TERM);
        builder.addColumn(MATERIAL);
        builder.addColumn(AUTHOR);
        builder.addColumn(VALID_FROM_DATE);
        builder.addColumn(VALID_UNTIL_DATE);
        for (EntityPropertyHistory entry : history)
        {
            builder.addRow(entry);
            builder.column(PROPERTY_TYPE_CODE).addString(entry.getPropertyType().getCode());
            builder.column(PROPERTY_TYPE_LABEL).addString(entry.getPropertyType().getLabel());
            builder.column(VALUE).addString(entry.getValue());
            builder.column(VOCABULARY_TERM).addString(entry.getVocabularyTerm());
            builder.column(MATERIAL).addString(entry.getMaterial());
            builder.column(AUTHOR).addPerson(entry.getAuthor());
            builder.column(VALID_FROM_DATE).addDate(entry.getValidFromDate());
            builder.column(VALID_UNTIL_DATE).addDate(entry.getValidUntilDate());
        }
        return builder.getModel();
    }

}
