package ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;

/**
 * Feature vectors (details and summaries) for one material.
 * 
 * @author Tomasz Pylak
 */
public class MaterialAllReplicasFeatureVectors implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // NOTE: has the same length as feature vectors in all summaries
    private List<CodeAndLabel> featureDescriptions;

    private MaterialFeatureVectorSummary generalSummary;

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