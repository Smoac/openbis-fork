package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.IRealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
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
        /** updates background color of the well with given coordinates */
        void updateWellStyle(int rowIx, int colIx, Color bakgroundColor);

        /** sets up an 'empty' tooltip ('Loading...') for the well with given coordinates */
        void addEmptyTooltip(int rowIx, int colIx);

        /** updates tooltip of the well with given coordinates */
        void updateTooltip(int rowIx, int colIx, String tooltipOrNull);

        /** schedules given action to be invoked as tooltip action for well with given coordinates */
        void scheduleUpdateTooltip(int rowIx, int colIx, IDelegatedAction refreshTooltipAction);

        /** updates legend of the heatmap to the new one */
        void updateLegend(Widget legend);
    }

    /** Generates tooltips for wells */
    static interface IWellTooltipGenerator
    {
        /**
         * Generates a short description of the well, which can be used as e.g. a tooltip.
         * 
         * @param distinguishedLabelOrNull if not null contains label of the feature which should be
         *            distinguished.
         */
        String generateTooltip(int rowIx, int colIx, String distinguishedLabelOrNull);
    }

    private static final String UNKNOWN_WELL_MSG = "No metadata available";

    // this renderer does not change, so we can cache it
    private final IHeatmapRenderer<WellData> cachedMetadataHeatmapRenderer;

    private final PlateLayouterModel model;

    private final HeatmapPresenter.IHeatmapViewManipulator viewManipulations;

    private final HeatmapPresenter.IWellTooltipGenerator tooltipGenerator;

    private final IRealNumberRenderer realNumberRenderer;

    private final ScreeningViewContext viewContext;

    // ---

    public HeatmapPresenter(ScreeningViewContext viewContext, PlateLayouterModel model,
            IRealNumberRenderer realNumberRenderer,
            HeatmapPresenter.IHeatmapViewManipulator viewManipulations)
    {
        this.viewContext = viewContext;
        this.model = model;
        this.viewManipulations = viewManipulations;
        this.realNumberRenderer = realNumberRenderer;
        this.tooltipGenerator = new WellTooltipGenerator(model, realNumberRenderer);
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
     * Changes the presented heatmap to the one which shows the feature with given name.
     */
    public void setFeatureValueMode(CodeAndLabel featureName)
    {
        if (model.isFeatureAvailable(featureName.getLabel()))
        {
            doChangeFeatureValueMode(featureName);
        } else
        {
            DatasetReference dataset = model.tryGetDatasetReference();
            if (dataset != null)
            {
                viewContext.getService().getFeatureVectorDataset(dataset, featureName,
                        createChangeHeatmapCallback(viewContext, featureName));
            } else
            {
                MessageBox.alert("Error", "No data set selected", null);
            }
        }
    }

    private void doChangeFeatureValueMode(CodeAndLabel featureName)
    {
        IHeatmapRenderer<WellData> renderer = createFeatureHeatmapRenderer(featureName.getLabel());
        refreshHeatmap(renderer, featureName.getLabel());
    }

    private AsyncCallback<FeatureVectorDataset> createChangeHeatmapCallback(
            final ScreeningViewContext context, final CodeAndLabel featureName)
    {
        return new AbstractAsyncCallback<FeatureVectorDataset>(context)
            {
                @Override
                protected void process(FeatureVectorDataset featureVector)
                {
                    model.updateFeatureVectorDataset(featureVector);

                    if (model.isFeatureAvailable(featureName.getLabel()))
                    {
                        doChangeFeatureValueMode(featureName);
                    } else
                    {
                        MessageBox.alert("Error",
                                "No values found for feature " + featureName.getLabel(), null);
                    }
                }
            };
    }

    private IHeatmapRenderer<WellData> createFeatureHeatmapRenderer(String featureLabel)
    {
        List<WellData> wellList = model.getWellList();
        if (model.isVocabularyFeature(featureLabel))
        {
            return createVocabularyFeatureHeatmapRenderer(wellList, featureLabel);
        } else
        {
            return createFloatFeatureHeatmapRenderer(wellList, featureLabel, realNumberRenderer);
        }
    }

    // here we are sure that all wells have a vocabulary feature with given featureLabel
    private static IHeatmapRenderer<WellData> createVocabularyFeatureHeatmapRenderer(
            List<WellData> wellList, final String featureLabel)
    {
        List<String> uniqueValues = extractUniqueVocabularyTerms(wellList, featureLabel);
        return new DelegatingStringHeatmapRenderer<WellData>(uniqueValues, null)
            {
                @Override
                protected String extractLabel(WellData well)
                {
                    return tryAsVocabularyFeature(well, featureLabel);
                }
            };
    }

    private static List<String> extractUniqueVocabularyTerms(List<WellData> wellList,
            String featureLabel)
    {
        Set<String> uniqueValues = new HashSet<String>();
        for (WellData well : wellList)
        {
            String term = tryAsVocabularyFeature(well, featureLabel);
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
    private void refreshHeatmap(IHeatmapRenderer<WellData> renderer, final String featureLabelOrNull)
    {
        WellData[][] wellMatrix = model.getWellMatrix();

        for (int rowIx = 0; rowIx < wellMatrix.length; rowIx++)
        {
            for (int colIx = 0; colIx < wellMatrix[rowIx].length; colIx++)
            {
                final WellData wellData = wellMatrix[rowIx][colIx];
                Color color = renderer.getColor(wellData);

                viewManipulations.updateWellStyle(rowIx, colIx, color);

                final int ri = rowIx;
                final int ci = colIx;
                final IDelegatedAction refreshTooltipAction = new IDelegatedAction()
                    {
                        @Override
                        public void execute()
                        {
                            viewContext.log("Update tooltip: "
                                    + wellData.getWellLocation().toString());
                            final String tooltip =
                                    tooltipGenerator.generateTooltip(ri, ci, featureLabelOrNull);
                            viewManipulations.updateTooltip(ri, ci, tooltip);
                        }
                    };

                if (wellData.isFullyLoaded())
                {
                    refreshTooltipAction.execute();
                } else
                {
                    viewManipulations.addEmptyTooltip(rowIx, colIx);
                    viewManipulations.scheduleUpdateTooltip(rowIx, colIx, new IDelegatedAction()
                        {
                            @Override
                            public void execute()
                            {
                                viewContext.log("Fetch Well Values: "
                                        + wellData.getWellLocation().toString());
                                fetchWellFeatures(wellData, refreshTooltipAction);
                            }
                        });
                }
            }
        }
        refreshLegend(renderer);
    }

    private void fetchWellFeatures(final WellData wellData,
            final IDelegatedAction refreshTooltipAction)
    {
        final DatasetReference datasetReference = model.tryGetDatasetReference();
        if (datasetReference != null)
        {
            final String datasetCode = datasetReference.getCode();
            final String datastoreCode = datasetReference.getDatastoreCode();
            final WellLocation location = wellData.getWellLocation();
            final AsyncCallback<FeatureVectorValues> callback =
                    new AbstractAsyncCallback<FeatureVectorValues>(viewContext)
                        {
                            @Override
                            protected void process(FeatureVectorValues resultOrNull)
                            {
                                viewContext.log("Update Well Feature Values: "
                                        + wellData.getWellLocation().toString());
                                if (resultOrNull != null)
                                {
                                    model.updateWellFeatureValues(resultOrNull);
                                } else
                                {
                                    model.resetFeatureValues(wellData);
                                }
                                refreshTooltipAction.execute();
                            }
                        };
            viewContext.getService().getWellFeatureVectorValues(datasetCode, datastoreCode,
                    location, callback);
        } else
        {
            wellData.setFullyLoaded(true);
            refreshTooltipAction.execute();
        }
    }

    private void refreshLegend(IHeatmapRenderer<WellData> renderer)
    {
        // creates the heatmap legend using current settings
        List<HeatmapScaleElement> scale = renderer.calculateScale();
        Widget legend = HeatmapScaleFactory.create(renderer.tryGetFirstLabel(), scale);
        viewManipulations.updateLegend(legend);
    }

    // here we are sure that all wells have a float feature with given featureLabel
    private static IHeatmapRenderer<WellData> createFloatFeatureHeatmapRenderer(
            List<WellData> wells, final String featureLabel, IRealNumberRenderer realNumberRenderer)
    {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (WellData well : wells)
        {
            Float value = tryAsFloatFeature(well, featureLabel);
            if (value != null && Float.isNaN(value) == false && Float.isInfinite(value) == false)
            {
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
        }
        return new DelegatingFloatHeatmapRenderer<WellData>(min, max, realNumberRenderer)
            {
                @Override
                protected Float convert(WellData well)
                {
                    return tryAsFloatFeature(well, featureLabel);
                }
            };
    }

    private static float tryAsFloatFeature(WellData well, final String featureLabel)
    {
        FeatureValue value = well.tryGetFeatureValue(featureLabel);
        return value != null ? value.asFloat() : null;
    }

    private static String tryAsVocabularyFeature(WellData well, final String featureLabel)
    {
        FeatureValue value = well.tryGetFeatureValue(featureLabel);
        return value != null ? value.tryAsVocabularyTerm() : null;
    }

    /** */
    private static class WellMetadataHeatmapRenderer extends
            DelegatingStringHeatmapRenderer<WellData>
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
            return label.toUpperCase().matches(ScreeningConstants.CONTROL_WELL_SAMPLE_TYPE_PATTERN);
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

}