package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.i18n.client.NumberFormat;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.MultilineHTML;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RealNumberFormatingParameters;

/**
 * Renderer of {@link Double} value.
 * 
 * @author Izabela Adamczyk
 */
public final class RealNumberRenderer implements GridCellRenderer<BaseEntityModel<?>>
{
    private static final String ZEROS = "00000000000000000000";
    private static final int MAX_PRECISION = ZEROS.length();

    private static final int MAX_DIGITAL_FORMAT_LENGTH = 10;

    private static final double MIN_DIGITAL_FORMAT_VALUE = 0.00005;

    private static final String SCIENTIFIC_FORMAT = "0.0000E0000";

    private static final String DIGITAL_FORMAT = "0.0000";

    private static String render(String value)
    {
        double doubleValue = Double.parseDouble(value);
        String formattedValue = NumberFormat.getFormat(DIGITAL_FORMAT).format(doubleValue);
        if (formattedValue.length() > MAX_DIGITAL_FORMAT_LENGTH)
        {
            formattedValue = NumberFormat.getFormat(SCIENTIFIC_FORMAT).format(doubleValue);
        }
        if (Math.abs(doubleValue) < MIN_DIGITAL_FORMAT_VALUE)
        {
            formattedValue += "..."; // show 0.0000...
        } else
        {
            formattedValue += "&nbsp;&nbsp;&nbsp;"; // add ' ' to always have a correct alignment
        }
        return MultilineHTML.wrapUpInDivWithTooltip(formattedValue, Double.toString(doubleValue));
    }

    public Object render(BaseEntityModel<?> model, String property, ColumnData config,
            int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
            Grid<BaseEntityModel<?>> grid)
    {
        String value = String.valueOf(model.get(property));
        if (value == null)
        {
            return "";
        }
        return render(value);
    }

    public static String render(String value,
            RealNumberFormatingParameters realNumberFormatingParameters)
    {
        if (realNumberFormatingParameters.isFormatingEnabled() == false)
        {
            return value;
        }
        int precision = Math.max(0, Math.min(MAX_PRECISION, realNumberFormatingParameters.getPrecision()));
        String format = "0." + ZEROS.substring(0, precision);
        if (realNumberFormatingParameters.isScientific())
        {
            format += "E000";
        }
        double doubleValue = Double.parseDouble(value);
        String formattedValue = NumberFormat.getFormat(format).format(doubleValue);
        return MultilineHTML.wrapUpInDivWithTooltip(formattedValue, value);
    }

}
