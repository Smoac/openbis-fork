package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;

/** Describes column's metadata and UI. */
interface ISampleColDefUI extends IColumnDefinition<Sample>
{
    int getWidth();

    boolean isHidden();
}