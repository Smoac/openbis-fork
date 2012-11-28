package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.IRealNumberRenderer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.model.MinMaxAndRange;

/**
 * Assigns colors to numeric ranges. Colors will be used to create a heat map.
 * 
 * @author Izabela Adamczyk
 */
class NumberHeatmapRenderer implements IHeatmapRenderer<Float>
{
    private final float step;

    private final List<Color> colors;

    private final float from;
    
    private final float until;

    private final IRealNumberRenderer realNumberRenderer;

    private final String scaleTop;

    private final String scaleBottom;

    public NumberHeatmapRenderer(MinMaxAndRange minMaxRange, IRealNumberRenderer realNumberRenderer)
    {
        this(minMaxRange, ColorConstants.LONG_GRADIENT_DEFAULT_COLORS, realNumberRenderer);
    }

    public NumberHeatmapRenderer(MinMaxAndRange minMaxRange, List<String> colors,
            IRealNumberRenderer realNumberRenderer)
    {
        from = minMaxRange.getRange().getFrom();
        until = minMaxRange.getRange().getUntil();
        this.colors = ColorConstants.asColors(colors);
        this.realNumberRenderer = realNumberRenderer;
        if (from == until)
        {
            this.step = 1.0f / this.colors.size();
        } else
        {
            this.step = (until - from) / this.colors.size();
        }
        if (step > 0)
        {
            scaleTop = round(Math.max(until, minMaxRange.getMax()));
            scaleBottom = round(Math.min(from, minMaxRange.getMin()));
        } else
        {
            scaleTop = round(Math.min(until, minMaxRange.getMin()));
            scaleBottom = round(Math.max(from, minMaxRange.getMax()));
        }
    }

    @Override
    public Color getColor(Float value)
    {
        if (value == null || Float.isNaN(value) || Float.isInfinite(value))
        {
            return ColorConstants.EMPTY_VALUE_COLOR;
        }
        float part = (until - value) / step;
        int colorNumber = Math.max(0, Math.min((int) Math.floor(part), colors.size() - 1));
        return colors.get(colorNumber);
    }

    @Override
    public List<HeatmapScaleElement> calculateScale()
    {
        List<HeatmapScaleElement> scale = new ArrayList<HeatmapScaleElement>();
        for (int i = 0; i < colors.size() - 1; i++)
        {
            String label = round(until - step * (i + 1));
            scale.add(new HeatmapScaleElement(label, colors.get(i)));
        }
        scale.add(new HeatmapScaleElement(scaleBottom, colors.get(colors.size() - 1)));
        return scale;
    }

    @Override
    public String tryGetFirstLabel()
    {
        return scaleTop;
    }

    private String round(float labelValue)
    {
        return realNumberRenderer
                .render(Math.abs(step) > 10 ? Math.round(labelValue) : labelValue);
    }
}