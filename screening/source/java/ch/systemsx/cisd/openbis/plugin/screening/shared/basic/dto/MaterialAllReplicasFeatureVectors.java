package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Tomasz Pylak
 */
public class MaterialAllReplicasFeatureVectors implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // NOTE: has the same length as feature vectors in all summaries
    private List<CodeAndLabel> featureDescriptions;

    private MaterialFeatureVectorSummary generalSummary;

    // DISPLAY NOTE: All the columns should be sorted by <subgroupName> and inside each subgroup
    // they should be sorted by <replicaSequenceNumber>.
    // It can be assumed that the data in this DTO are provided in a way which ensure this order.
    // NOTE: Can be empty.
    private List<MaterialReplicaSubgroupFeatureVector> subgroups;

    // NOTE: Can be empty. Used for replicas which have no subgroups
    private List<MaterialSingleReplicaFeatureVector> replicas;

    // GWT only
    @SuppressWarnings("unused")
    private MaterialAllReplicasFeatureVectors()
    {
    }

    public MaterialAllReplicasFeatureVectors(List<CodeAndLabel> featureDescriptions,
            MaterialFeatureVectorSummary generalSummary,
            List<MaterialReplicaSubgroupFeatureVector> subgroups,
            List<MaterialSingleReplicaFeatureVector> replicas)
    {
        this.featureDescriptions = featureDescriptions;
        this.generalSummary = generalSummary;
        this.subgroups = subgroups;
        this.replicas = replicas;
    }

    public List<CodeAndLabel> getFeatureDescriptions()
    {
        return featureDescriptions;
    }

    public MaterialFeatureVectorSummary getGeneralSummary()
    {
        return generalSummary;
    }

    public List<MaterialReplicaSubgroupFeatureVector> getSubgroups()
    {
        return subgroups;
    }

    public List<MaterialSingleReplicaFeatureVector> getReplicas()
    {
        return replicas;
    }

}