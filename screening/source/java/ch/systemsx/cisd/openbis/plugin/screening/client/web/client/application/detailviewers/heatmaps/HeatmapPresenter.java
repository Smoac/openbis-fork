package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.IRealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
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
        IHeatmapRenderer<WellData> renderer = createFeatureHeatmapRenderer(featureIndex);
        refreshHeatmap(renderer, featureIndex);
    }

    private IHeatmapRenderer<WellData> createFeatureHeatmapRenderer(int featureIndex)
    {
        List<WellData> wellList = model.getWellList();
        if (model.isVocabularyFeature(featureIndex))
        {
            return createVocabularyFeatureHeatmapRenderer(wellList, featureIndex);
        } else
        {
            return createFloatFeatureHeatmapRenderer(wellList, featureIndex, realNumberRenderer);
        }
    }

    // here we are sure that all wells have a vocabulary feature at featureIndex index
    private static IHeatmapRenderer<WellData> createVocabularyFeatureHeatmapRenderer(
            List<WellData> wellList, final int featureIndex)
    {
        List<String> uniqueValues = extractUniqueVocabularyTerms(wellList, featureIndex);
        return new DetegatingStringHeatmapRenderer<WellData>(uniqueValues, null)
            {
                @Override
                protected String extractLabel(WellData well)
                {
                    return tryAsVocabularyFeature(well, featureIndex);
                }
            };
    }

    private static List<String> extractUniqueVocabularyTerms(List<WellData> wellList,
            int featureIndex)
    {
        Set<String> uniqueValues = new HashSet<String>();
        for (WellData well : wellList)
        {
            String term = tryAsVocabularyFeature(well, featureIndex);
            if (term != null)
            {
                uniqueValues.add(term);
            }
        }
        List<String> result = new ArrayList<String>(uniqueValues);
        Collections.sort(result);
        return result;
    }

    // updates color of all well components
    private void refreshHeatmap(IHeatmapRenderer<WellData> renderer, Integer featureIndexOrNull)
    {
        WellData[][] wellMatrix = model.getWellMatrix();
        for (int rowIx = 0; rowIx < wellMatrix.length; rowIx++)
        {
            for (int colIx = 0; colIx < wellMatrix[rowIx].length; colIx++)
            {
                WellData wellData = wellMatrix[rowIx][colIx];
                Color color = renderer.getColor(wellData);
                String tooltipOrNull =
                        WellTooltipGenerator.tryGenerateTooltip(model, rowIx, colIx,
                                featureIndexOrNull, realNumberRenderer);
                viewManipulations.refreshWellStyle(rowIx, colIx, color, tooltipOrNull);
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

    // here we are sure that all wells have a float feature at featureIndex index
    private static IHeatmapRenderer<WellData> createFloatFeatureHeatmapRenderer(
            List<WellData> wells, final int featureIndex, IRealNumberRenderer realNumberRenderer)
    {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (WellData well : wells)
        {
            Float value = tryAsFloatFeature(well, featureIndex);
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
                    return tryAsFloatFeature(well, featureIndex);
                }
            };
    }

    private static float tryAsFloatFeature(WellData well, final int featureIndex)
    {
        FeatureValue value = well.tryGetFeatureValue(featureIndex);
        return value != null ? value.asFloat() : null;
    }

    private static String tryAsVocabularyFeature(WellData well, final int featureIndex)
    {
        FeatureValue value = well.tryGetFeatureValue(featureIndex);
        return value != null ? value.tryAsVocabularyTerm() : null;
    }

    /** */
    private static class WellMetadataHeatmapRenderer extends
            DetegatingStringHeatmapRenderer<WellData>
    {
        private static final int MAX_SPECIFIC_COLORS = 3;

        private static final Color EMPTY_WELL_COLOR = new Color("#F2F0F7");

        private static final List<Color> CONTROL_COLORS = Arrays.asList(new Color("#DAFFB3"), // green
                new Color("#9CFFB3"), // dark green
                new Color("#FFFFB3") // yellow
                );

        private static final List<Color> NON_CONTROL_COLORS = Arrays.asList(new Color("#D3CCFF"), // violet
                new Color("#BDCCFF"), // blue
                new Color("#E9CCFF") // pink
                );

        public static IHeatmapRenderer<WellData> create(List<WellData> wells)
        {
            Set<String> labels = new HashSet<String>();
            boolean emptyWellsEncountered = false;
            for (WellData well : wells)
            {
                String label = tryExtractLabel(well);
                if (label != null)
                {
                    labels.add(label);
                } else
                {
                    emptyWellsEncountered = true;
                }
            }
            ArrayList<String> uniqueValues = new ArrayList<String>(labels);
            Collections.sort(uniqueValues);
            if (emptyWellsEncountered)
            {
                uniqueValues.add(UNKNOWN_WELL_MSG);
            }
            List<Color> colorsOrNull =
                    tryGetMetadataLegendColors(uniqueValues, emptyWellsEncountered);
            return new WellMetadataHeatmapRenderer(uniqueValues, colorsOrNull);
        }

        private static List<Color> tryGetMetadataLegendColors(List<String> uniqueValues,
                boolean emptyWellsEncountered)
        {
            int colorsNumber = uniqueValues.size();
            int nonEmptyColors = colorsNumber - (emptyWellsEncountered ? 1 : 0);
            if (nonEmptyColors > MAX_SPECIFIC_COLORS)
            {
                return null;
            }

            assert CONTROL_COLORS.size() >= MAX_SPECIFIC_COLORS : "not enough control colors";
            assert NON_CONTROL_COLORS.size() >= MAX_SPECIFIC_COLORS : "not enough non-control colors";

            Iterator<Color> controlColorsIter = CONTROL_COLORS.iterator();
            Iterator<Color> nonControlColorsIter = NON_CONTROL_COLORS.iterator();

            List<Color> colors = new ArrayList<Color>();
            for (int i = 0; i < nonEmptyColors; i++)
            {
                if (isControl(uniqueValues.get(i)))
                {
                    colors.add(controlColorsIter.next());
                } else
                {
                    colors.add(nonControlColorsIter.next());
                }
            }
            if (emptyWellsEncountered)
            {
                colors.add(EMPTY_WELL_COLOR);
            }
            return colors;
        }

        private static boolean isControl(String label)
        {
            return label.toUpperCase().indexOf(ScreeningConstants.CONTROL_WELL_TYPE_CODE_MARKER) != -1;
        }

        private WellMetadataHeatmapRenderer(List<String> uniqueValues, List<Color> colorsOrNull)
        {
            super(uniqueValues, colorsOrNull);
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

    static class WellTooltipGenerator
    {
        private static final String NEWLINE = "\n";

        private static final int MAX_DESCRIBED_FEATURES = 30;

        /**
         * Generates a short description of the well, which can be used as e.g. a tooltip
         * 
         * @param featureIndexOrNull if not null contains an index of the feature which should be
         *            distinguished.
         */
        public static String tryGenerateTooltip(PlateLayouterModel model, int rowIx, int colIx,
                Integer featureIndexOrNull, IRealNumberRenderer realNumberRenderer)
        {
            return new WellTooltipGenerator(model, realNumberRenderer).tryGenerateShortDescription(
                    rowIx, colIx, featureIndexOrNull);
        }

        private final PlateLayouterModel model;

        private final IRealNumberRenderer realNumberRenderer;

        private WellTooltipGenerator(PlateLayouterModel model,
                IRealNumberRenderer realNumberRenderer)
        {
            this.model = model;
            this.realNumberRenderer = realNumberRenderer;
        }

        private String tryGenerateShortDescription(int rowIx, int colIx, Integer featureIndexOrNull)
        {
            WellData wellData = model.getWellMatrix()[rowIx][colIx];
            String tooltip = "";
            if (featureIndexOrNull != null)
            {
                tooltip += generateOneFeatureDescription(wellData, featureIndexOrNull, true);
            }

            tooltip += generateMetadataDescription(wellData);

            int featuresNum = getNumberOfFeatures();
            if (featuresNum - (featureIndexOrNull != null ? 1 : 0) > 0)
            {
                if (tooltip.length() == 0)
                {
                    tooltip += getWellCodeDescription(wellData);
                } else
                {
                    tooltip += NEWLINE; // separate metadata from the text below
                }
                int describedFeaturesNum = Math.min(MAX_DESCRIBED_FEATURES, featuresNum);
                for (int ix = 0; ix < describedFeaturesNum; ix++)
                {
                    if (featureIndexOrNull == null || ix != featureIndexOrNull.intValue())
                    {
                        tooltip += generateOneFeatureDescription(wellData, ix, false);
                    }
                }
                if (featuresNum > describedFeaturesNum)
                {
                    tooltip += "...";
                }
            }
            return tooltip;

        }

        private int getNumberOfFeatures()
        {
            List<String> featureLabels = model.tryGetFeatureLabels();
            return featureLabels == null ? 0 : featureLabels.size();
        }

        private String generateOneFeatureDescription(WellData wellData, int featureIndex,
                boolean distinguished)
        {
            List<String> featureLabels = model.tryGetFeatureLabels();
            assert featureLabels != null : "feature labels not set";

            FeatureValue value = wellData.tryGetFeatureValue(featureIndex);
            // if the value should be distinguished we show it even if it's null
            if (value == null && distinguished == false)
            {
                return "";
            }
            String textValue = (value == null ? "" : "" + renderValue(value));
            if (distinguished)
            {
                textValue = "<b>" + textValue + "</b>";
            }
            return featureLabels.get(featureIndex) + ": " + textValue + NEWLINE;
        }

        private String renderValue(FeatureValue value)
        {
            if (value.isFloat())
            {
                return renderFloat(value.asFloat());
            } else
            {
                return value.toString();
            }
        }

        private String renderFloat(float value)
        {
            return realNumberRenderer.render(value);
        }

        private static String generateMetadataDescription(WellData wellData)
        {
            WellMetadata metadata = wellData.tryGetMetadata();
            if (metadata == null)
            {
                return "";
            }
            String tooltip = getWellCodeDescription(metadata);

            List<IEntityProperty> properties = metadata.getWellSample().getProperties();
            Collections.sort(properties);
            for (IEntityProperty property : properties)
            {
                PropertyType propertyType = property.getPropertyType();
                tooltip +=
                        NEWLINE + propertyType.getLabel() + ": " + getPropertyDisplayText(property);
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
            return tooltip + NEWLINE;
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

        private static String getWellCodeDescription(WellData wellData)
        {
            WellMetadata metadata = wellData.tryGetMetadata();
            return metadata == null ? "" : getWellCodeDescription(metadata) + NEWLINE;
        }

        private static String getWellCodeDescription(WellMetadata metadata)
        {
            Sample wellSample = metadata.getWellSample();
            String sampleTypeCode = wellSample.getSampleType().getCode();
            return printFriendlyCode(sampleTypeCode) + ": " + wellSample.getSubCode();
        }

        // private
        static String printFriendlyCode(String code)
        {
            String[] tokens = code.split("_|-");
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < tokens.length; i++)
            {
                if (sb.length() > 0)
                {
                    sb.append(" ");
                }
                sb.append(capitalizeFirst(tokens[i]));
            }
            return sb.toString();
        }

        private static String capitalizeFirst(String value)
        {
            if (value == null || value.length() == 0)
            {
                return value;
            }
            return ("" + value.charAt(0)).toUpperCase() + value.substring(1).toLowerCase();
        }
    }

}