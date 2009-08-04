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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import java.util.List;

import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * {@link ComboBox} containing list of sample types loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleTypeSelectionWidget extends DropDownList<SampleTypeModel, SampleType>
{
    public static final String SUFFIX = "sample-type";

    private final IViewContext<?> viewContext;

    private final boolean onlyListable;

    private final boolean withAll;

    private final boolean withTypeInFile;

    public SampleTypeSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            final boolean onlyListable, final boolean withAll, final boolean withTypeInFile)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.SAMPLE_TYPE, ModelDataPropertyNames.CODE,
                "sample type", "sample types");
        this.viewContext = viewContext;
        this.onlyListable = onlyListable;
        this.withAll = withAll;
        this.withTypeInFile = withTypeInFile;
        setAutoSelectFirst(withAll);
    }

    public SampleTypeSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            final boolean onlyListable)
    {
        this(viewContext, idSuffix, onlyListable, false, false);
    }

    /**
     * Returns the {@link SampleType} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final SampleType tryGetSelectedSampleType()
    {
        return super.tryGetSelected();
    }

    @Override
    protected List<SampleTypeModel> convertItems(List<SampleType> result)
    {
        return SampleTypeModel.convert(result, onlyListable, withAll, withTypeInFile);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<SampleType>> callback)
    {
        viewContext.getCommonService().listSampleTypes(callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.SAMPLE_TYPE);
    }
}