package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;

/** Describes column's metadata and UI. */
public interface ISampleColDefUI extends IColumnDefinition<Sample>
{
    int getWidth();

    boolean isHidden();
}