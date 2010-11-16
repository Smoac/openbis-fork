package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;

/**
 * Assigns colors to numeric ranges. Colors will be used to create a heat map.
 * 
 * @author Izabela Adamczyk
 */
public class NumberHeatmapRenderer implements IHeatmapRenderer<Float>
{

    private static String[] DEFAULT_COLORS =
        { "#67001F", "#B2182B", "#D6604D", "#F4A582", "#FDDBC7", "#F7F7F7", "#D1E5F0", "#92C5DE",
                "#4393C3", "#2166AC", "#053061" };

    private final float min;

    private float step;

    private final String[] colors;

    private final float max;

    public NumberHeatmapRenderer(float min, float max)
    {
        this(min, max, DEFAULT_COLORS);
    }

    public NumberHeatmapRenderer(float min, float max, String[] colors)
    {
        this.min = min;
        this.max = max;
        this.colors = colors;
        step = (max - min) / colors.length;
    }

    public Color getColor(Float value)
    {
        if (value > max || value < min)
        {
            throw new IllegalArgumentException();
        }
        if (value == max)
        {
            return new Color(colors[colors.length - 1]);
        }
        float range = value - min;
        float part = range / step;
        int colorNumber = (int) Math.floor(part);
        return new Color(colors[colorNumber]);
    }

    public List<HeatmapScaleElement> calculateScale()
    {
        ArrayList<HeatmapScaleElement> scale = new ArrayList<HeatmapScaleElement>();
        for (int i = 0; i < colors.length; i++)
        {
            String label = (min + step * (i + 1)) + "";
            scale.add(new HeatmapScaleElement(label, new Color(colors[i])));
        }
        return scale;
    }

    public String tryGetFirstLabel()
    {
        return min + "";
    }

}