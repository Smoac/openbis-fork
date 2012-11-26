package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;

/**
 * Allows to convert any object into a String and present the String on the heatmap using
 * {@link StringHeatmapRenderer}.
 * 
 * @author Tomasz Pylak
 */
abstract class DelegatingStringHeatmapRenderer<T> implements IHeatmapRenderer<T>
{
    /** Convert T into a String which will be represented on the heatmap. */
    protected abstract String extractLabel(T value);

    private final IHeatmapRenderer<String> delegator;

    public DelegatingStringHeatmapRenderer(List<String> uniqueValues, List<Color> colorsOrNull)
    {
        this.delegator = new StringHeatmapRenderer(uniqueValues, colorsOrNull);
    }

    @Override
    public Color getColor(T value)
    {
        return delegator.getColor(extractLabel(value));
    }

    @Override
    public List<HeatmapScaleElement> calculateScale()
    {
        return delegator.calculateScale();
    }

    @Override
    public String tryGetFirstLabel()
    {
        return null;
    }
}