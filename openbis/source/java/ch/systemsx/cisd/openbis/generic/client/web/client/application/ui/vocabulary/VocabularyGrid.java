/*
 * Copyright 2009 ETH Zuerich, CISD
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

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.shared.Vocabulary;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GridWithRPCProxy;

/**
 * {@link GridWithRPCProxy} displaying vocabularies.
 * 
 * @author Izabela Adamczyk
 */
public class VocabularyGrid extends GridWithRPCProxy<Vocabulary, VocabularyModel>
{
    private final CommonViewContext viewContext;

    public VocabularyGrid(final CommonViewContext viewContext, String idPrefix)
    {
        super(createColumnModel(viewContext), idPrefix);
        this.viewContext = viewContext;
    }

    private static ColumnModel createColumnModel(IViewContext<?> context)
    {
        final ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(ColumnConfigFactory.createCodeColumnConfig(context));
        return new ColumnModel(configs);
    }

    @Override
    protected void loadDataFromService(AsyncCallback<BaseListLoadResult<VocabularyModel>> callback)
    {
        viewContext.getService().listVocabularies(true,
                new ListVocabulariesCallback(viewContext, callback));
    }

    class ListVocabulariesCallback extends DelegatingAsyncCallback
    {
        public ListVocabulariesCallback(IViewContext<?> context,
                AsyncCallback<BaseListLoadResult<VocabularyModel>> callback)
        {
            super(context, callback);
        }

        @Override
        protected List<VocabularyModel> convert(List<Vocabulary> result)
        {
            return VocabularyModel.convert(result);
        }
    }
}