/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityPropertyUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Abstract super class of {@link TypedTableGrid}-based entity browsers.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractEntityGrid<E extends IEntityInformationHolderWithProperties> extends
        TypedTableGrid<E>
{
    public AbstractEntityGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, boolean refreshAutomatically,
            IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, browserId, refreshAutomatically, displayTypeIDGenerator);
    }

    public AbstractEntityGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, browserId, displayTypeIDGenerator);
    }

    protected abstract EntityKind getEntityKind();

    @Override
    protected boolean isEditable(BaseEntityModel<TableModelRowWithObject<E>> model, String columnID)
    {
        String propertyName = columnID.substring(SampleGridColumnIDs.PROPERTIES_PREFIX.length());
        E sample = model.getBaseObject().getObjectOrNull();
        BasicEntityType entityType = sample.getEntityType();
        IEntityProperty propertyOrNull = tryGetProperty(sample, propertyName);
        if (propertyOrNull != null && propertyOrNull.isScriptable())
        {
            return false;
        }
        List<IColumnDefinition<TableModelRowWithObject<E>>> columnDefinitions =
                getColumnDefinitions(Arrays.asList(columnID));
        IColumnDefinition<TableModelRowWithObject<E>> columnDefinition = columnDefinitions.get(0);
        return columnDefinition.tryToGetProperty(entityType.getCode()) != null;
    }

    @Override
    protected void applyModifications(BaseEntityModel<TableModelRowWithObject<E>> model,
            List<IModification> modifications)
    {
        final EntityKind entityKind = getEntityKind();
        final TechId entityId = new TechId(model.getBaseObject().getObjectOrNull());
        final EntityPropertyUpdates updates = new EntityPropertyUpdates(entityKind, entityId);
        for (IModification modification : modifications)
        {
            String propertyCode =
                    modification.getColumnID().substring(
                            SampleGridColumnIDs.PROPERTIES_PREFIX.length());
            updates.addModifiedProperty(propertyCode, modification.tryGetNewValue());
        }
        viewContext.getService().updateProperties(updates,
                createApplyModificationsCallback(model, modifications));
    }

    @Override
    protected void showEntityViewer(TableModelRowWithObject<E> row, boolean editMode,
            boolean inBackground)
    {
        showEntityInformationHolderViewer(row.getObjectOrNull(), editMode, inBackground);
    }

}
