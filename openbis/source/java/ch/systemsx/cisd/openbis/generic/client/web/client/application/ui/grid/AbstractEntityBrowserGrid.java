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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.ShowDetailsLinkCellRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.SetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * The abstract superclass for grids, which have a defined criteria provider (e.g. tollbar with the
 * filter criteria).
 * <p>
 * Provides a mechanism of auto-refreshing the grid only after the criteria provider is refreshed.
 * It is useful when a grid displays entities with properties which can be assigned or unassigned
 * while the grid is shown.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public abstract class AbstractEntityBrowserGrid<T extends IEntityPropertiesHolder, M extends BaseEntityModel<T>, K extends DefaultResultSetConfig<String, T>>
        extends AbstractBrowserGrid<T, M>
{
    abstract protected IColumnDefinitionKind<T>[] getStaticColumnsDefinition();

    /**
     * @return Database modifications which affect the grid and not necessarily the criteria
     *         provider
     */
    abstract protected Set<DatabaseModificationKind> getGridRelevantModifications();

    /** @return true if column definitions should be refreshed */
    abstract protected boolean hasColumnsDefinitionChanged(K newCriteria);

    /** @return text which should be used as a grid header */
    abstract protected String createHeader();

    abstract protected EntityType tryToGetEntityType();

    abstract protected EntityKind getEntityKind();

    abstract protected ICriteriaProvider<K> getCriteriaProvider();

    // criteria used in the previous refresh operation or null if it has not occurred yet
    protected K criteria;

    public interface ICriteriaProvider<K>
    {
        /** @return criteria used as the main grid filter */
        K tryGetCriteria();

        /**
         * calls the refresh callback only after appropriate criteria are updated. But if there is
         * nothing to update in the criteria provider the callback is called immediately.
         */
        void update(Set<DatabaseModificationKind> observedModifications,
                final IDataRefreshCallback dataRefreshCallback);

        DatabaseModificationKind[] getRelevantModifications();
    }

    protected AbstractEntityBrowserGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String gridId, DisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, gridId, displayTypeIDGenerator);
    }

    protected AbstractEntityBrowserGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String gridId, boolean showHeader, boolean refreshAutomatically,
            IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, gridId, showHeader, refreshAutomatically, displayTypeIDGenerator);
        allowMultipleSelection();
    }

    @Override
    public String getGridDisplayTypeID()
    {
        String suffix = createDisplayIdSuffix(getEntityKind(), tryToGetEntityType());
        return createGridDisplayTypeID(suffix);
    }

    private static String createDisplayIdSuffix(EntityKind entityKindOrNull,
            EntityType entityTypeOrNull)
    {
        String suffix = "";
        if (entityKindOrNull != null)
        {
            suffix += "-" + entityKindOrNull.toString();
        }
        if (entityTypeOrNull != null)
        {
            suffix += "-" + entityTypeOrNull.getCode();
        }
        return suffix;
    }

    /**
     * Refreshes the browser grid up to given parameters.
     * <p>
     * Note that by doing so the result set in the cache on the server side will be removed.
     * </p>
     */
    @Override
    protected void refresh()
    {
        Boolean refreshColumnsNeeded = updateCriteria();
        if (refreshColumnsNeeded == null)
        {
            return;
        }
        String newHeader = createHeader();
        super.refresh(null, newHeader, refreshColumnsNeeded);
    }

    protected final void refreshColumnsSettingsIfNecessary()
    {
        Boolean refreshColumnsNeeded = updateCriteria();
        if (refreshColumnsNeeded != null && refreshColumnsNeeded.booleanValue())
        {
            super.recreateColumnModelAndRefreshColumnsWithFilters();
        }
    }

    /**
     * Updates criteria, returns null if bew criteria are not yet known, otherwise a boolean which
     * tells if columns definition changed.
     */
    private Boolean updateCriteria()
    {
        K newCriteria = getCriteriaProvider().tryGetCriteria();
        if (newCriteria == null)
        {
            return null;
        }
        boolean refreshColumnsDefinition = hasColumnsDefinitionChanged(newCriteria);
        this.criteria = newCriteria;
        return refreshColumnsDefinition;
    }

    @Override
    protected boolean isRefreshEnabled()
    {
        return getCriteriaProvider().tryGetCriteria() != null;
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        List<DatabaseModificationKind> relevantModifications =
                new ArrayList<DatabaseModificationKind>();
        SetUtils.addAll(relevantModifications, getCriteriaProvider().getRelevantModifications());
        relevantModifications.addAll(getGridRelevantModifications());
        return relevantModifications.toArray(DatabaseModificationKind.EMPTY_ARRAY);
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        final boolean shouldRefreshGridAfterwards =
                SetUtils.containsAny(observedModifications, getGridRelevantModifications());
        // we refresh the whole grid after the entity types are refreshed. In this way we are able
        // to take into account new property types which constitute new columns.
        getCriteriaProvider().update(observedModifications,
                createRefreshGridCallback(shouldRefreshGridAfterwards));
    }

    /**
     * Initializes criteria and refreshes the grid when criteria are fetched. <br>
     * Note that in this way we do not refresh the grid automatically, but we wait until all the
     * property types will be fetched from the server (criteria provider will be updated), to set
     * the available grid columns.
     */
    protected void updateCriteriaProviderAndRefresh()
    {
        boolean shouldRefreshGridAfterwards = true;
        HashSet<DatabaseModificationKind> observedModifications =
                new HashSet<DatabaseModificationKind>();
        getCriteriaProvider().update(observedModifications,
                createRefreshGridCallback(shouldRefreshGridAfterwards));
    }

    private final IDataRefreshCallback createRefreshGridCallback(final boolean shouldRefreshGrid)
    {
        IDataRefreshCallback entityTypeRefreshCallback = new IDataRefreshCallback()
            {
                public void postRefresh(boolean wasSuccessful)
                {
                    if (shouldRefreshGrid)
                    {
                        refreshGridSilently();
                    }
                }
            };
        return entityTypeRefreshCallback;
    }

    protected final GridCellRenderer<BaseEntityModel<?>> createShowDetailsLinkCellRenderer()
    {
        return new ShowDetailsLinkCellRenderer(viewContext
                .getMessage(Dict.SHOW_DETAILS_LINK_TEXT_VALUE));
    }

    // ------ static helpers

    protected final static Set<DatabaseModificationKind> getGridRelevantModifications(
            ObjectKind entity)
    {
        Set<DatabaseModificationKind> result = new HashSet<DatabaseModificationKind>();
        result.add(createOrDelete(entity));
        result.add(edit(entity));
        result.add(createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT));
        result.add(edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT));
        result.add(edit(ObjectKind.VOCABULARY_TERM));
        return result;
    }

    protected static <K> ICriteriaProvider<K> createUnrefreshableCriteriaProvider(final K criteria)
    {
        return new ICriteriaProvider<K>()
            {
                public K tryGetCriteria()
                {
                    return criteria;
                }

                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return new DatabaseModificationKind[0];
                }

                public void update(Set<DatabaseModificationKind> observedModifications,
                        IDataRefreshCallback postRefreshCallback)
                {
                    postRefreshCallback.postRefresh(true);
                }
            };
    }

    protected static boolean hasColumnsDefinitionChanged(EntityType newEntityType,
            EntityType prevEntityType)
    {
        if (newEntityType == null)
        {
            return false; // nothing chosen
        }
        if (prevEntityType == null)
        {
            return true; // first selection
        }
        return newEntityType.equals(prevEntityType) == false
                || propertiesEqual(newEntityType, prevEntityType) == false;
    }

    private static boolean propertiesEqual(EntityType entityType1, EntityType entityType2)
    {
        return entityType1.getAssignedPropertyTypes()
                .equals(entityType2.getAssignedPropertyTypes());
    }

}
