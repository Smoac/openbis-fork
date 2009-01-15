package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;

/** Describes column's metadata and UI. */
public interface IColumnDefinitionUI<T> extends IColumnDefinition<T>
{
    int getWidth();

    boolean isHidden();
}