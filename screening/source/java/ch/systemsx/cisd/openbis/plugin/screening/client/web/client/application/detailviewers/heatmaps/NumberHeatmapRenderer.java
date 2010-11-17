package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.IRealNumberRenderer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;

/**
 * Assigns colors to numeric ranges. Colors will be used to create a heat map.
 * 
 * @author Izabela Adamczyk
 */
class NumberHeatmapRenderer implements IHeatmapRenderer<Float>
{
    private final float min;

    private final float step;

    private final List<Color> colors;

    private final float max;

    private final IRealNumberRenderer realNumberRenderer;

    public NumberHeatmapRenderer(float min, float max, IRealNumberRenderer realNumberRenderer)
    {
        this(min, max, ColorConstants.LONG_GRADIENT_DEFAULT_COLORS, realNumberRenderer);
    }

    public NumberHeatmapRenderer(float min, float max, List<String> colors,
            IRealNumberRenderer realNumberRenderer)
    {
        this.min = min;
        this.max = max;
        this.colors = ColorConstants.asColors(colors);
        this.step = (max - min) / this.colors.size();
        this.realNumberRenderer = realNumberRenderer;
    }

    public Color getColor(Float value)
    {
        if (value == null || Float.isNaN(value) || Float.isInfinite(value))
        {
            return ColorConstants.EMPTY_VALUE_COLOR;
        }
        if (value > max || value < min)
        {
            throw new IllegalArgumentException("value from the wrong range " + value);
        }
        float range = max - value;
        float part = range / step;
        int colorNumber = Math.min((int) Math.floor(part), colors.size() - 1);
        return colors.get(colorNumber);
    }

    public List<HeatmapScaleElement> calculateScale()
    {
        ArrayList<HeatmapScaleElement> scale = new ArrayList<HeatmapScaleElement>();
        for (int i = 0; i < colors.size(); i++)
        {
            String label = round((max - step * (i + 1)));
            scale.add(new HeatmapScaleElement(label, colors.get(i)));
        }
        return scale;
    }

    public String tryGetFirstLabel()
    {
        return round(max);
    }

    private String round(float labelValue)
    {
        if (Math.abs(step) > 10)
        {
            return "" + Math.round(labelValue);
        } else
        {
            return realNumberRenderer.render(labelValue);
        }
    }
}