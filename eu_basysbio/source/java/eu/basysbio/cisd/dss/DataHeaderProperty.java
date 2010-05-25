package eu.basysbio.cisd.dss;

/**
 * Properties of data header.
 * 
 * @author Izabela Adamczyk
 */
public enum DataHeaderProperty
{

    ExperimentCode
    {
        @Override
        String getValue(DataColumnHeader header)
        {
            return header.getExperimentCode();
        }
    },
    CultivationMethodExperimentCode
    {
        @Override
        String getValue(DataColumnHeader header)
        {
            return header.getCultivationMethod();
        }
    },
    BiologicalReplicatateCode
    {
        @Override
        String getValue(DataColumnHeader header)
        {
            return header.getBiologicalReplicateCode();
        }
    },
    TimePoint
    {
        /**
         * String value of integer.
         */
        @Override
        String getValue(DataColumnHeader header)
        {
            return Integer.toString(header.getTimePoint());
        }
    },
    TimePointType
    {
        @Override
        String getValue(DataColumnHeader header)
        {
            return header.getTimePointType();
        }
    },
    TechnicalReplicateCode
    {
        @Override
        String getValue(DataColumnHeader header)
        {
            return header.getTechnicalReplicateCode();
        }
    },
    CelLoc
    {
        @Override
        String getValue(DataColumnHeader header)
        {
            return header.getCelLoc();
        }
    },
    DataSetType
    {
        @Override
        String getValue(DataColumnHeader header)
        {
            return header.getTimeSeriesDataSetType();
        }
    },
    ValueType
    {
        @Override
        String getValue(DataColumnHeader header)
        {
            return header.getValueType();
        }
    },
    Scale
    {
        @Override
        String getValue(DataColumnHeader header)
        {
            return header.getScale();
        }
    },
    BiID
    {
        @Override
        String getValue(DataColumnHeader header)
        {
            return header.getBiID();
        }
    },
    CG
    {
        @Override
        String getValue(DataColumnHeader header)
        {
            return header.getControlledGene();
        }
    },
    GROWTH_PHASE
    {
        @Override
        String getValue(DataColumnHeader header)
        {
            return header.getGrowthPhase();
        }
    },
    GENOTYPE
    {
        @Override
        String getValue(DataColumnHeader header)
        {
            return header.getGenotype();
        }
    };

    abstract String getValue(DataColumnHeader header);
}