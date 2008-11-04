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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SearchableEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;

/**
 * A {@link SimpleComboBox} extension for searching entities.
 * 
 * @author Christian Ribeaud
 */
final class EntityChooser extends ComboBox<SearchableEntityModel>
{
    private final IViewContext<IGenericClientServiceAsync> genericContext;

    EntityChooser(final IViewContext<IGenericClientServiceAsync> genericContext)
    {
        this.genericContext = genericContext;
        setEditable(false);
        setDisplayField(ModelDataPropertyNames.DESCRIPTION);
        setStore(new ListStore<SearchableEntityModel>());
    }

    public final SearchableEntity tryGetSelectedSearchableEntity()
    {
        final List<SearchableEntityModel> selection = getSelection();
        if (selection.size() > 0)
        {
            return selection.get(0).get(ModelDataPropertyNames.OBJECT);
        } else
        {
            return null;
        }

    }

    //
    // ComboBox
    //

    @Override
    protected final void onRender(final Element parent, final int index)
    {
        super.onRender(parent, index);
        genericContext.getService().listSearchableEntities(
                new AbstractAsyncCallback<List<SearchableEntity>>(genericContext)
                    {

                        //
                        // AbstractAsyncCallback
                        //

                        @Override
                        protected void process(final List<SearchableEntity> result)
                        {
                            final ListStore<SearchableEntityModel> searchableEntityStore =
                                    getStore();
                            searchableEntityStore.removeAll();
                            searchableEntityStore
                                    .add(SearchableEntityModel.NULL_SEARCHABLE_ENTITY_MODEL);
                            searchableEntityStore.add(SearchableEntityModel.convert(result));
                            setValue(searchableEntityStore.getAt(0));
                        }
                    });
    }
}