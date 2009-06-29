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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * A {@link ComboBox} extension for selecting a {@link Vocabulary}.
 * 
 * @author Christian Ribeaud
 */
public class VocabularySelectionWidget extends DropDownList<BaseModelData, Vocabulary>
{

    private static final String PREFIX = "vocabulary-select";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public VocabularySelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, PREFIX, Dict.VOCABULARY, ModelDataPropertyNames.CODE, viewContext
                .getMessage(Dict.VOCABULARY), viewContext.getMessage(Dict.VOCABULARY));
        this.viewContext = viewContext;
        setWidth(100);
    }

    //
    // Model creation
    //

    private static BaseModelData createModel(Vocabulary vocabulary)
    {
        return createModel(vocabulary, vocabulary.getCode());
    }

    private static BaseModelData createModel(Object object, String code)
    {
        final BaseModelData model = new BaseModelData();
        model.set(ModelDataPropertyNames.CODE, code);
        model.set(ModelDataPropertyNames.OBJECT, object);
        return model;
    }

    //
    // Helper classes
    //

    @Override
    protected List<BaseModelData> convertItems(List<Vocabulary> list)
    {
        final List<BaseModelData> result = new ArrayList<BaseModelData>();
        for (final Vocabulary vocabulary : list)
        {
            result.add(createModel(vocabulary));
        }
        return result;
    }

    @Override
    protected void loadData(final AbstractAsyncCallback<List<Vocabulary>> callback)
    {
        DefaultResultSetConfig<String, Vocabulary> criteria =
                DefaultResultSetConfig.createFetchAll();
        // TODO 2009-03-31, Tomasz Pylak: clean the server-side cache at some point
        viewContext.getService().listVocabularies(false, true, criteria,
                new AsyncCallback<ResultSet<Vocabulary>>()
                    {
                        public void onFailure(Throwable caught)
                        {
                            callback.onFailure(caught);
                        }

                        public void onSuccess(ResultSet<Vocabulary> result)
                        {
                            callback.onSuccess(result.getList());
                        }
                    });
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY) };
    }
}
