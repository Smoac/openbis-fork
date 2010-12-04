package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleImageHtmlRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleDateRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DssLinkTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GeneratedImageTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ImageTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Franz-Josef Elmer
 */
public class TypedTableGridColumnDefinition<T extends ISerializable> implements
        IColumnDefinition<TableModelRowWithObject<T>>
{
    protected TableModelColumnHeader header;

    private String title;

    private String downloadURL;

    private String sessionID;

    public TypedTableGridColumnDefinition(TableModelColumnHeader header, String title,
            String downloadURL, String sessionID)
    {
        this.header = header;
        this.title = title;
        this.downloadURL = downloadURL;
        this.sessionID = sessionID;
    }

    // GWT only
    @SuppressWarnings("unused")
    private TypedTableGridColumnDefinition()
    {
    }

    public String getHeader()
    {
        return title;
    }

    public String getIdentifier()
    {
        return header.getId();
    }

    public String tryToGetProperty(String key)
    {
        return null;
    }

    public String getValue(GridRowModel<TableModelRowWithObject<T>> rowModel)
    {
        Comparable<?> cell = tryGetComparableValue(rowModel);
        if (cell instanceof ImageTableCell)
        {
            ImageTableCell imageCell = (ImageTableCell) cell;
            int width = imageCell.getMaxThumbnailWidth();
            int height = imageCell.getMaxThumbnailHeight();
            String imagePath = imageCell.getPath();
            return SimpleImageHtmlRenderer.createEmbededDatastoreImageHtml(imagePath, width,
                    height, downloadURL, sessionID);
        }
        if (cell instanceof DateTableCell)
        {
            return SimpleDateRenderer.renderDate(((DateTableCell) cell).getDateTime());
        }
        if (cell instanceof GeneratedImageTableCell)
        {
            return ((GeneratedImageTableCell) cell).getHTMLString(downloadURL, sessionID);
        }
        if (cell instanceof DssLinkTableCell)
        {
            return ((DssLinkTableCell) cell).getHtmlString(sessionID);
        }
        return cell.toString();
    }

    public Comparable<?> tryGetComparableValue(GridRowModel<TableModelRowWithObject<T>> rowModel)
    {
        return rowModel.getOriginalObject().getValues().get(header.getIndex());
    }
}