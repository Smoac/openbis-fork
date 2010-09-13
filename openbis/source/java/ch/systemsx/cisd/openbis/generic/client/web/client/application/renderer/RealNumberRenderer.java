package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

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
    private static final String EXPONENT_FORMAT = "E000";

    private static final String ZEROS = "00000000000000000000";

    private static final int MAX_PRECISION = ZEROS.length();

    public static String render(String value,
            RealNumberFormatingParameters realNumberFormatingParameters)
    {
        if (realNumberFormatingParameters.isFormatingEnabled() == false || value.length() == 0)
        {
            return value;
        }
        int precision =
                Math.max(0, Math.min(MAX_PRECISION, realNumberFormatingParameters.getPrecision()));
        String format = "0." + ZEROS.substring(0, precision);
        boolean scientific = realNumberFormatingParameters.isScientific();
        if (scientific)
        {
            format += EXPONENT_FORMAT;
        }
        try
        {
            BigDecimal preciseValue = new BigDecimal(value);
            String formattedValue = new DecimalFormat(format).format(preciseValue);
            boolean valueIsZero = preciseValue.doubleValue() == 0;
            boolean formattedValueIsZero = new BigDecimal(formattedValue).doubleValue() == 0;
            if ((scientific == false) && (valueIsZero == false) && formattedValueIsZero)
            {
                formattedValue = new DecimalFormat(format + EXPONENT_FORMAT).format(preciseValue);
            }
            return MultilineHTML.wrapUpInDivWithTooltip(formattedValue, value);
        } catch (NumberFormatException ex)
        {
            return value;
        }
    }

    private final RealNumberFormatingParameters realNumberFormatingParameters;

    public RealNumberRenderer(RealNumberFormatingParameters realNumberFormatingParameters)
    {
        this.realNumberFormatingParameters = realNumberFormatingParameters;
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
        return render(value, realNumberFormatingParameters);
    }

}
