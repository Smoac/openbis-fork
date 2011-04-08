package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.List;

/**
 * @author Tomasz Pylak
 */
public class MaterialReplicaSubgroupFeatureVector
{
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