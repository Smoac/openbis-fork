package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Feature vector summary for the subgroup of well replicas together with detailed feature vectors
 * which were used to calculate the summary.
 * 
 * @author Tomasz Pylak
 */
public class MaterialReplicaSubgroupFeatureVector implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // DISPLAY NOTE: The header of each column should be:
    // <subgroupName> repl. <replicaSequenceNumber>
    // e.g. SIRNA XYZ repl. 2
    private List<MaterialSingleReplicaFeatureVector> singleReplicaValues;

    // e.g. average or median of all replica values in this supgroup
    // DISPLAY NOTE: this is e.g. the "median" column for each subgroup of 3 replicas for the same
    // SIRNA in the prototype
    private float[] aggregatedSummary;

    // DISPLAY NOTE: this decides about the header of the subgroup summary (aggregatedSummaryfield).
    // For now only "Median", but "Average" will be added in future.
    private ReplicaSummaryAggregationType summaryAggregationType;

    private String subgroupLabel;

    // GWT only
    @SuppressWarnings("unused")
    private MaterialReplicaSubgroupFeatureVector()
    {
    }

    public MaterialReplicaSubgroupFeatureVector(
            List<MaterialSingleReplicaFeatureVector> singleReplicaValues,
            float[] aggregatedSummary, ReplicaSummaryAggregationType summaryAggregationType,
            String subgroupLabel)
    {
        this.singleReplicaValues = singleReplicaValues;
        this.aggregatedSummary = aggregatedSummary;
        this.summaryAggregationType = summaryAggregationType;
        this.subgroupLabel = subgroupLabel;
    }

    public List<MaterialSingleReplicaFeatureVector> getSingleReplicaValues()
    {
        return singleReplicaValues;
    }

    public float[] getAggregatedSummary()
    {
        return aggregatedSummary;
    }

    public ReplicaSummaryAggregationType getSummaryAggregationType()
    {
        return summaryAggregationType;
    }

    public String getSubgroupLabel()
    {
        return subgroupLabel;
    }
}