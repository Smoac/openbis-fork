package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.IRealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Manages heatmap presentation, controls view and model.
 * 
 * @author Tomasz Pylak
 */
class HeatmapPresenter
{
    /** Triggers changes in the heatmap view */
    static interface IHeatmapViewManipulator
    {
        /** updates background color and tooltip of the well with given coordinates */
        void refreshWellStyle(int rowIx, int colIx, Color bakgroundColor, String tooltipOrNull);

        /** updates legend of the heatmap to the new one */
        void updateLegend(Widget legend);
    }

    private static final String UNKNOWN_WELL_MSG = "No metadata available";

    // this renderer does not change, so we can cache it
    private final IHeatmapRenderer<WellData> cachedMetadataHeatmapRenderer;

    private final PlateLayouterModel model;

    private final HeatmapPresenter.IHeatmapViewManipulator viewManipulations;

    private final IRealNumberRenderer realNumberRenderer;

    // ---

    public HeatmapPresenter(PlateLayouterModel model, IRealNumberRenderer realNumberRenderer,
            HeatmapPresenter.IHeatmapViewManipulator viewManipulations)
    {
        this.model = model;
        this.viewManipulations = viewManipulations;
        this.realNumberRenderer = realNumberRenderer;
        this.cachedMetadataHeatmapRenderer =
                WellMetadataHeatmapRenderer.create(model.getWellList());
        setWellMetadataMode();
    }

    /** Changes the presented heatmap to the one which shows well types. */
    public void setWellMetadataMode()
    {
        IHeatmapRenderer<WellData> renderer = cachedMetadataHeatmapRenderer;
        refreshHeatmap(renderer, null);
    }

    /**
     * Changes the presented heatmap to the one which shows the feature which appears in all
     * {@link WellData} on a specified index.
     */
    public void setFeatureValueMode(int featureIndex)
    {
        IHeatmapRenderer<WellData> renderer =
                createFeatureHeatmapRenderer(model.getWellList(), featureIndex, realNumberRenderer);
        refreshHeatmap(renderer, featureIndex);
    }

    // updates color of all well components
    private void refreshHeatmap(IHeatmapRenderer<WellData> renderer, Integer featureIndexOrNull)
    {
        WellData[][] wellMatrix = model.getWellMatrix();
        for (int row = 0; row < wellMatrix.length; row++)
        {
            for (int col = 0; col < wellMatrix[row].length; col++)
            {
                WellData wellData = wellMatrix[row][col];
                Color color = renderer.getColor(wellData);
                String tooltipOrNull = tryGenerateTooltip(wellData, featureIndexOrNull);
                viewManipulations.refreshWellStyle(row, col, color, tooltipOrNull);
            }
        }
        refreshLegend(renderer);
    }

    private void refreshLegend(IHeatmapRenderer<WellData> renderer)
    {
        // creates the heatmap legend using current settings
        List<HeatmapScaleElement> scale = renderer.calculateScale();
        Widget legend = HeatmapScaleFactory.create(renderer.tryGetFirstLabel(), scale);
        viewManipulations.updateLegend(legend);
    }

    private static IHeatmapRenderer<WellData> createFeatureHeatmapRenderer(List<WellData> wells,
            final int featureIndex, IRealNumberRenderer realNumberRenderer)
    {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (WellData well : wells)
        {
            Float value = well.tryGetFeatureValue(featureIndex);
            if (value != null && Float.isNaN(value) == false && Float.isInfinite(value) == false)
            {
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
        }
        return new DetegatingFloatHeatmapRenderer<WellData>(min, max, realNumberRenderer)
            {
                @Override
                protected Float convert(WellData well)
                {
                    return well.tryGetFeatureValue(featureIndex);
                }
            };
    }

    private String tryGenerateTooltip(WellData wellData, Integer featureIndexOrNull)
    {
        if (featureIndexOrNull != null)
        {
            return tryGenerateFeatureTooltip(wellData, featureIndexOrNull);
        } else
        {
            return tryGenerateMetadataTooltip(wellData);
        }
    }

    private String tryGenerateFeatureTooltip(WellData wellData, int featureIndex)
    {
        Float value = wellData.tryGetFeatureValue(featureIndex);
        List<String> featureLabels = model.tryGetFeatureLabels();
        assert featureLabels != null : "feature labels not set";
        String tooltip = featureLabels.get(featureIndex) + ": <b>" + value + "</b>";
        String metadataTooltip = tryGenerateMetadataTooltip(wellData);
        if (metadataTooltip != null)
        {
            tooltip += "\n" + metadataTooltip;
        }
        return tooltip;
    }

    private static String tryGenerateMetadataTooltip(WellData wellData)
    {
        WellMetadata metadata = wellData.tryGetMetadata();
        if (metadata == null)
        {
            return null;
        }
        String tooltip = getWellDescription(metadata);

        List<IEntityProperty> properties = metadata.getWellSample().getProperties();
        Collections.sort(properties);
        for (IEntityProperty property : properties)
        {
            PropertyType propertyType = property.getPropertyType();
            tooltip += "\n" + propertyType.getLabel() + ": " + getPropertyDisplayText(property);
            Material material = property.getMaterial();
            if (material != null
                    && material.getMaterialType().getCode()
                            .equalsIgnoreCase(ScreeningConstants.GENE_PLUGIN_TYPE_CODE))
            {
                List<IEntityProperty> geneProperties = material.getProperties();
                for (IEntityProperty geneProperty : geneProperties)
                {
                    if (geneProperty.getPropertyType().getCode()
                            .equalsIgnoreCase(ScreeningConstants.GENE_SYMBOLS))
                    {
                        tooltip += " [" + geneProperty.tryGetAsString() + "]";
                    }
                }
            }
        }
        return tooltip;
    }

    private static String getPropertyDisplayText(IEntityProperty property)
    {
        Material material = property.getMaterial();
        if (material != null)
        {
            return material.getCode();
        } else
        {
            return property.tryGetAsString();
        }
    }

    private static String getWellDescription(WellMetadata metadata)
    {
        return "Well: " + metadata.getWellSample().getSubCode();
    }

    /** */
    private static class WellMetadataHeatmapRenderer extends
            DetegatingStringHeatmapRenderer<WellData>
    {
        public static IHeatmapRenderer<WellData> create(List<WellData> wells)
        {
            List<String> uniqueValues = extractUniqueSortedValues(wells);
            return new WellMetadataHeatmapRenderer(uniqueValues);
        }

        private WellMetadataHeatmapRenderer(List<String> uniqueValues)
        {
            super(uniqueValues);
        }

        private static List<String> extractUniqueSortedValues(List<WellData> wells)
        {
            Set<String> uniqueValues = new HashSet<String>();
            boolean emptyWellsEncountered = false;
            for (WellData well : wells)
            {
                String label = tryExtractLabel(well);
                if (label != null)
                {
                    uniqueValues.add(label);
                } else
                {
                    emptyWellsEncountered = true;
                }
            }
            ArrayList<String> result = new ArrayList<String>(uniqueValues);
            Collections.sort(result);
            if (emptyWellsEncountered)
            {
                result.add(UNKNOWN_WELL_MSG);
            }
            return result;
        }

        @Override
        protected final String extractLabel(WellData well)
        {
            String label = tryExtractLabel(well);
            if (label == null)
            {
                return UNKNOWN_WELL_MSG;
            } else
            {
                return label;
            }
        }

        private static final String tryExtractLabel(WellData well)
        {
            WellMetadata metadata = well.tryGetMetadata();
            if (metadata == null)
            {
                return null;
            } else
            {
                return metadata.getWellSample().getEntityType().getCode();
            }
        }
    }

}