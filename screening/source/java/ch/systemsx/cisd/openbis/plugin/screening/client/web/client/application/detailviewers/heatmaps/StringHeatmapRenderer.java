package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;

/**
 * Assigns colors to string labels. Colors will be used to create a heatmap.
 * 
 * @author Tomasz Pylak
 */
public class StringHeatmapRenderer implements IHeatmapRenderer<String>
{
    private static final Color CATEGORY_OTHERS_COLOR = new Color("#00FF00");

    private static final List<String> LONG_DEFAULT_COLORS = Arrays.asList("#67001F", "#B2182B",
            "#D6604D", "#F4A582", "#FDDBC7", "#F7F7F7", "#D1E5F0", "#92C5DE", "#4393C3", "#2166AC",
            "#053061");

    private static final List<String> SHORT_DEFAULT_COLORS = Arrays.asList("#5E3C99", "$B2ABD2",
            "#F7F7F7", "#FDB863", "#E66101");

    private static final List<String> DOUBLE_DEFAULT_COLORS = Arrays.asList("#5E3C99", "#F7F7F7");

    private static final List<String> SINGLE_DEFAULT_COLORS = Arrays.asList("#F7F7F7");

    private static final String CATEGORY_OTHERS_LABEL = "Others";

    private final List<HeatmapScaleElement> scale;

    private final Map<String, Color> colorsMap;

    private final boolean moreLabelsThanColors;

    /**
     * Assigns colors to string labels from a fixed default set of colors. If there are more values
     * than colors, the "overhead values" are marked as belonging to one "Others" group.
     */
    public StringHeatmapRenderer(List<String> values)
    {
        this(values, asColors(getDefaultColors(values.size())));
    }

    private static List<String> getDefaultColors(int size)
    {
        if (size == 1)
        {
            return SINGLE_DEFAULT_COLORS;
        } else if (size == 2)
        {
            return DOUBLE_DEFAULT_COLORS;
        } else if (size <= SHORT_DEFAULT_COLORS.size())
        {
            return SHORT_DEFAULT_COLORS;
        } else
        {
            return LONG_DEFAULT_COLORS;
        }
    }

    /**
     * Assigns specified colors to string labels using colors in the specified order. If there are
     * more values than colors, the "overhead values" are marked as belonging to one "Others" group.
     */
    public StringHeatmapRenderer(List<String> uniqueValues, List<Color> scaleColors)
    {
        this.scale = calculateScale(uniqueValues, scaleColors);
        this.colorsMap = calculateColorMap(scale);
        scale.add(new HeatmapScaleElement(CATEGORY_OTHERS_LABEL, CATEGORY_OTHERS_COLOR));

        this.moreLabelsThanColors = (uniqueValues.size() > scaleColors.size());
    }

    private static List<Color> asColors(List<String> defaultColors)
    {
        List<Color> colors = new ArrayList<Color>();
        for (String color : LONG_DEFAULT_COLORS)
        {
            colors.add(new Color(color));
        }
        return colors;
    }

    private static Map<String, Color> calculateColorMap(List<HeatmapScaleElement> scale)
    {
        Map<String, Color> colorsMap = new HashMap<String, Color>();
        for (HeatmapScaleElement range : scale)
        {
            colorsMap.put(range.getLabel(), range.getColor());
        }
        return colorsMap;
    }

    private static List<HeatmapScaleElement> calculateScale(List<String> uniqueValues,
            List<Color> scaleColors)
    {
        List<HeatmapScaleElement> scale = new ArrayList<HeatmapScaleElement>();
        Iterator<Color> colorsIter = scaleColors.iterator();
        Iterator<String> valuesIter = uniqueValues.iterator();
        while (colorsIter.hasNext() && valuesIter.hasNext())
        {
            scale.add(new HeatmapScaleElement(valuesIter.next(), colorsIter.next()));
        }
        return scale;
    }

    public Color getColor(String value)
    {
        Color color = colorsMap.get(value);
        assert color != null || moreLabelsThanColors : "Unexpected value " + value;

        if (color == null)
        {
            return CATEGORY_OTHERS_COLOR;
        } else
        {
            return color;
        }
    }

    public List<HeatmapScaleElement> calculateScale()
    {
        return scale;
    }

    public String tryGetFirstLabel()
    {
        return null;
    }
}