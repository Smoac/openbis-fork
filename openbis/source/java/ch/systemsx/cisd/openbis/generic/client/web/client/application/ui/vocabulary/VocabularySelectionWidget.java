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
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.shared.Vocabulary;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;

/**
 * A {@link ComboBox} extension for selecting a {@link Vocabulary}.
 * 
 * @author Christian Ribeaud
 */
public class VocabularySelectionWidget extends ComboBox<BaseModelData>
{
    public static final String NEW_VOCABULARY_CODE = "(New Vocabulary)";

    private static final String PREFIX = "vocabulary-select";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public VocabularySelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        setId(ID);
        setDisplayField(ModelDataPropertyNames.CODE);
        setEnabled(false);
        setEditable(false);
        setWidth(100);
        setFieldLabel(viewContext.getMessage(Dict.VOCABULARY));
        setStore(new ListStore<BaseModelData>());
    }

    /**
     * Returns the {@link Vocabulary} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final Vocabulary tryGetSelectedVocabulary()
    {
        return GWTUtils.tryGetSingleSelected(this);
    }

    private final void loadVocabularies()
    {
        DefaultResultSetConfig<String, Vocabulary> criteria =
                DefaultResultSetConfig.createFetchAll();
        viewContext.getService().listVocabularies(false, true, criteria,
                new ListVocabulariesCallback(viewContext));
    }

    /**
     * Refreshes the store with given list of {@link Vocabulary}.
     */
    protected void refreshStore(final List<Vocabulary> result)
    {
        final ListStore<BaseModelData> vocabularyStore = getStore();
        vocabularyStore.removeAll();
        vocabularyStore.add(convert(result));
        setEnabled(true);
        if (vocabularyStore.getCount() > 0)
        {
            setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, "vocabulary"));
        } else
        {
            setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, "vocabularies"));
        }
        applyEmptyText();
    }

    //
    // ComboBox
    //

    private static List<BaseModelData> convert(List<Vocabulary> list)
    {
        final List<BaseModelData> result = new ArrayList<BaseModelData>();
        for (final Vocabulary vocabulary : list)
        {
            result.add(createModel(vocabulary));
        }
        return result;
    }

    private static BaseModelData createModel(Vocabulary vocabulary)
    {
        return createModel(vocabulary, vocabulary.getCode());
    }

    protected final static BaseModelData createNewVocabularyVocabularyModel()
    {
        return createModel(null, NEW_VOCABULARY_CODE);
    }

    private static BaseModelData createModel(Object object, String code)
    {
        final BaseModelData model = new BaseModelData();
        model.set(ModelDataPropertyNames.CODE, code);
        model.set(ModelDataPropertyNames.OBJECT, object);
        return model;
    }

    @Override
    protected final void afterRender()
    {
        super.afterRender();
        loadVocabularies();
    }

    //
    // Helper classes
    //

    public final class ListVocabulariesCallback extends
            AbstractAsyncCallback<ResultSet<Vocabulary>>
    {
        ListVocabulariesCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void process(final ResultSet<Vocabulary> result)
        {
            refreshStore(result.getList());
        }

    }
}
