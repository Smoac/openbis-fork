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
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFICATION_DATE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFIER;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATION_DATE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATOR;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.SAMPLE;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSDataSetExportHelper extends AbstractXLSEntityExportHelper<DataSet, DataSetType>
{

    public XLSDataSetExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final List<String> permIds, final int rowNumber, final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap,
            final XLSExport.TextFormatting textFormatting, final boolean compatibleWithImport)
    {
        return compatibleWithImport ? new AdditionResult(0, List.of())
                : super.add(api, sessionToken, wb, permIds, rowNumber, entityTypeExportFieldsMap, textFormatting, false);
    }

    @Override
    protected Collection<DataSet> getEntities(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<DataSetPermId> dataSetPermIds = permIds.stream().map(DataSetPermId::new)
                .collect(Collectors.toList());
        final DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withSample();
        fetchOptions.withExperiment();
        fetchOptions.withType().withPropertyAssignments().withPropertyType();
        fetchOptions.withProperties();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();
        return api.getDataSets(sessionToken, dataSetPermIds, fetchOptions).values();
    }

    @Override
    protected ExportableKind getExportableKind()
    {
        return ExportableKind.DATASET;
    }

    @Override
    protected ExportableKind getTypeExportableKind()
    {
        return ExportableKind.DATASET_TYPE;
    }

    @Override
    protected String getEntityTypeName()
    {
        return "Dataset type";
    }

    @Override
    protected String getIdentifier(final DataSet entity)
    {
        return entity.getPermId().getPermId();
    }

    @Override
    protected Function<DataSet, DataSetType> getTypeFunction()
    {
        return DataSet::getType;
    }

    @Override
    protected Attribute[] getAttributes(final DataSet dataSet)
    {
        return new Attribute[] { CODE, dataSet.getSample() != null ? SAMPLE : EXPERIMENT, REGISTRATOR, REGISTRATION_DATE,
                MODIFIER, MODIFICATION_DATE };
    }

    @Override
    protected String getAttributeValue(final DataSet dataSet, final Attribute attribute)
    {
        switch (attribute)
        {
            case CODE:
            {
                return dataSet.getCode();
            }
            case SAMPLE:
            {
                return dataSet.getSample().getIdentifier().getIdentifier();
            }
            case EXPERIMENT:
            {
                return dataSet.getExperiment().getIdentifier().getIdentifier();
            }
            case REGISTRATOR:
            {
                return dataSet.getRegistrator().getUserId();
            }
            case REGISTRATION_DATE:
            {
                return DATE_FORMAT.format(dataSet.getRegistrationDate());
            }
            case MODIFIER:
            {
                return dataSet.getModifier().getUserId();
            }
            case MODIFICATION_DATE:
            {
                return DATE_FORMAT.format(dataSet.getModificationDate());
            }
            default:
            {
                return null;
            }
        }
    }

    @Override
    protected Stream<String> getAttributeValuesStream(final DataSet dataSet)
    {
        return Stream.of(dataSet.getCode(), dataSet.getSample() != null
                ? dataSet.getSample().getIdentifier().getIdentifier()
                : dataSet.getExperiment().getIdentifier().getIdentifier(),
                dataSet.getRegistrator().getUserId(), DATE_FORMAT.format(dataSet.getRegistrationDate()),
                dataSet.getModifier().getUserId(), DATE_FORMAT.format(dataSet.getModificationDate()));
    }

    @Override
    protected String typePermIdToString(final DataSetType dataSetType)
    {
        return dataSetType.getPermId().getPermId();
    }

}
