/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.columns;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.SampleRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;

/**
 * {@link ModelData} for {@link Sample}
 * 
 * @author Izabela Adamczyk
 * @author Tomasz Pylak
 */
public final class SampleModel extends BaseModelData
{
    private static final long serialVersionUID = 1L;

    public SampleModel(final Sample sample)
    {
        set(ModelDataPropertyNames.OBJECT, sample);

        List<ISampleColDefUI> columnsSchema = createColumnsSchema(sample);
        for (IColumnDefinition<Sample> column : columnsSchema)
        {
            String value = renderColumnValue(sample, column);
            set(column.getIdentifier(), value);
        }
    }

    private String renderColumnValue(final Sample sample, IColumnDefinition<Sample> column)
    {
        String value = column.getValue(sample);
        if (column instanceof CommonSampleColDef)
        {
            CommonSampleColDefKind columnKind = ((CommonSampleColDef) column).getColumnKind();
            if (columnKind == CommonSampleColDefKind.REGISTRATOR)
            {
                value = PersonRenderer.createPersonAnchor(sample.getRegistrator(), value);
            } else if (columnKind == CommonSampleColDefKind.CODE)
            {
                value = SampleRenderer.render(sample, value);
            }
        }
        return value;
    }

    private static List<ISampleColDefUI> createColumnsSchema(Sample sample)
    {
        List<ISampleColDefUI> list = createCommonColumnsSchema(null);
        List<ISampleColDefUI> parentColumns =
                createParentColumnsSchema(null, sample.getSampleType());
        list.addAll(parentColumns);
        for (SampleProperty prop : sample.getProperties())
        {
            SampleTypePropertyType etpt = prop.getEntityTypePropertyType();
            list.add(new PropertySampleColDef(etpt.getPropertyType(), etpt.isDisplayed()));
        }
        return list;
    }

    public static List<ISampleColDefUI> createPropertyColumnsSchema(
            List<SampleTypePropertyType> sampleTypePropertyTypes)
    {
        List<ISampleColDefUI> list = new ArrayList<ISampleColDefUI>();
        for (SampleTypePropertyType etpt : sampleTypePropertyTypes)
        {
            boolean isHidden = etpt.isDisplayed() == false;
            list.add(new PropertySampleColDef(etpt.getPropertyType(), isHidden));
        }
        return list;
    }

    // result is added to allColumns map
    public static List<ISampleColDefUI> createCommonColumnsSchema(IMessageProvider msgProviderOrNull)
    {
        List<ISampleColDefUI> list = new ArrayList<ISampleColDefUI>();
        for (CommonSampleColDefKind columnKind : CommonSampleColDefKind.values())
        {
            list.add(createColumn(columnKind, msgProviderOrNull));
        }
        return list;
    }

    private static CommonSampleColDef createColumn(CommonSampleColDefKind columnKind,
            IMessageProvider messageProviderOrNull)
    {
        String headerText = null;
        if (messageProviderOrNull != null)
        {
            headerText = messageProviderOrNull.getMessage(columnKind.getHeaderMsgKey());
        }
        return new CommonSampleColDef(columnKind, headerText);
    }

    public static List<ISampleColDefUI> createParentColumnsSchema(
            IMessageProvider msgProviderOrNull, SampleType sampleType)
    {
        List<ISampleColDefUI> list = new ArrayList<ISampleColDefUI>();
        for (int depth = 1; depth <= sampleType.getGeneratedFromHierarchyDepth(); depth++)
        {
            String headerText = getParentColumnHeader(msgProviderOrNull, "generated_from", depth);
            list.add(new ParentGeneratedFromSampleColDef(depth, headerText));
        }
        for (int depth = 1; depth <= sampleType.getPartOfHierarchyDepth(); depth++)
        {
            String headerText = getParentColumnHeader(msgProviderOrNull, "part_of", depth);
            list.add(new ParentContainerSampleColDef(depth, headerText));
        }
        return list;
    }

    private static String getParentColumnHeader(IMessageProvider msgProviderOrNull,
            String messageKey, int depth)
    {
        return msgProviderOrNull == null ? null : msgProviderOrNull.getMessage(messageKey, depth);
    }

    public final Sample getSample()
    {
        return (Sample) get(ModelDataPropertyNames.OBJECT);
    }

    public final static List<SampleModel> asSampleModels(final List<Sample> samples)
    {
        final List<SampleModel> sampleModels = new ArrayList<SampleModel>(samples.size());
        for (final Sample sample : samples)
        {
            sampleModels.add(new SampleModel(sample));
        }
        return sampleModels;
    }
}
