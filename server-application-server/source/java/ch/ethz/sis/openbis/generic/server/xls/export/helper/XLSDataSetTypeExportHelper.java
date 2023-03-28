/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.CODE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.DESCRIPTION;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.VALIDATION_SCRIPT;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.VERSION;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.VersionUtils;

public class XLSDataSetTypeExportHelper extends AbstractXLSEntityTypeExportHelper<DataSetType>
{

    public XLSDataSetTypeExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public DataSetType getEntityType(final IApplicationServerApi api, final String sessionToken,
            final String permId)
    {
        final DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType().withVocabulary();
        propertyAssignmentFetchOptions.withPropertyType().withSampleType();
        propertyAssignmentFetchOptions.withPropertyType().withMaterialType();
        propertyAssignmentFetchOptions.withPlugin().withScript();
        final Map<IEntityTypeId, DataSetType> dataSetTypes = api.getDataSetTypes(sessionToken,
                Collections.singletonList(new EntityTypePermId(permId, EntityKind.DATA_SET)), fetchOptions);

        assert dataSetTypes.size() <= 1;

        final Iterator<DataSetType> iterator = dataSetTypes.values().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    protected Attribute[] getAttributes(final DataSetType entityType)
    {
        return new Attribute[] { VERSION, CODE, DESCRIPTION, VALIDATION_SCRIPT };
    }

    @Override
    protected String getAttributeValue(final DataSetType dataSetType, final Attribute attribute)
    {
        switch (attribute)
        {
            case CODE:
            {
                return dataSetType.getCode();
            }
            case DESCRIPTION:
            {
                return dataSetType.getDescription();
            }
            case VALIDATION_SCRIPT:
            {
                final Plugin validationPlugin = dataSetType.getValidationPlugin();
                return validationPlugin != null ? (validationPlugin.getName() != null ? validationPlugin.getName() + ".py" : "") : "";

            }
            case VERSION:
            {
                return String.valueOf(VersionUtils.getStoredVersion(allVersions, ImportTypes.DATASET_TYPE, null, dataSetType.getCode()));
            }
            default:
            {
                return null;
            }
        }
    }

    @Override
    protected ExportableKind getExportableKind()
    {
        return ExportableKind.DATASET_TYPE;
    }

}
