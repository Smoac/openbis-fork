/*
 * Copyright 2011 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.plugin.proteomics.server.business;

import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.common.eodsql.MockDataSet;
import ch.systemsx.cisd.openbis.generic.server.TestJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatabaseInstancePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.EntityTypePropertyTypePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SamplePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SampleTypePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SpacePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinRelatedSample;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.SampleAbundance;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.SamplePeptideModification;

/**
 * @author Franz-Josef Elmer
 */
public class ProteinRelatedSampleTableTest extends AbstractBOTestCase
{
    private ProteinRelatedSampleTable table;

    private SamplePE sample1;

    private SamplePE sample2;

    private SamplePE sample3;

    @Override
    @BeforeMethod
    public void setUp()
    {
        super.setUp();
        table =
                new ProteinRelatedSampleTable(daoFactory, specificDAOFactory,
                        new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()));

        SpacePE space =
                new SpacePEBuilder()
                        .code("s")
                        .databaseInstance(
                                new DatabaseInstancePEBuilder().code("my-db").getDatabaseInstance())
                        .getSpace();
        SampleTypePEBuilder sampleTypeBuilder = new SampleTypePEBuilder().code("my-type");
        PropertyTypePE propertyType =
                CommonTestUtils.createPropertyType("A", DataTypeCode.VARCHAR, null, null);
        EntityTypePropertyTypePEBuilder e = sampleTypeBuilder.assign(propertyType);
        EntityTypePropertyTypePE etpt = e.getEntityTypePropertyType();
        SampleTypePE sampleType = sampleTypeBuilder.getSampleType();
        sample1 =
                new SamplePEBuilder(1).space(space).code("S1").permID("s-1").type(sampleType)
                        .property(etpt, "hello").getSample();
        sample2 =
                new SamplePEBuilder(2).space(space).code("S2").permID("s-2").type(sampleType)
                        .getSample();
        sample3 =
                new SamplePEBuilder(3).space(space).code("S3").permID("s-3").type(sampleType)
                        .getSample();
    }

    @Test
    public void test()
    {
        final TechId experimentID = new TechId(42);
        final TechId proteinReferenceID = new TechId(43);
        context.checking(new Expectations()
            {
                {
                    one(specificDAOFactory).getProteinQueryDAO(experimentID);
                    will(returnValue(proteinDAO));

                    one(experimentDAO).getByTechId(experimentID);
                    ExperimentPE experiment = new ExperimentPE();
                    experiment.setPermId("exp-1");
                    will(returnValue(experiment));

                    one(proteinDAO).listSampleAbundanceByProtein(experiment.getPermId(),
                            proteinReferenceID.getId());
                    MockDataSet<SampleAbundance> sampleAbundances =
                            new MockDataSet<SampleAbundance>();
                    sampleAbundances.add(sampleAbundance("s-1", 0.25));
                    sampleAbundances.add(sampleAbundance("s-2", 0.75));
                    will(returnValue(sampleAbundances));

                    one(proteinDAO).listSamplePeptideModificatioByProtein(experiment.getPermId(),
                            proteinReferenceID.getId());
                    MockDataSet<SamplePeptideModification> modifications =
                            new MockDataSet<SamplePeptideModification>();
                    modifications.add(modification("s-2", "ab", 1, 21.5, 0.25));
                    modifications.add(modification("s-3", "def", 1, 12.375, 0.5));
                    modifications.add(modification("s-3", "def", 3, -1.5, 0.75));
                    will(returnValue(modifications));

                    one(sampleDAO).tryToFindByPermID("s-1");
                    will(returnValue(sample1));

                    one(sampleDAO).tryToFindByPermID("s-2");
                    will(returnValue(sample2));

                    one(sampleDAO).tryToFindByPermID("s-3");
                    will(returnValue(sample3));
                }
            });

        table.load(experimentID, proteinReferenceID, "abcdefabcab");

        List<ProteinRelatedSample> samples = table.getSamples();
        assertEquals("1:s-1:S1:MY-DB:/s/S1:SAMPLE:my-type:[A: hello]:0.25::null:null:null",
                render(samples.get(0)));
        assertEquals("2:s-2:S2:MY-DB:/s/S2:SAMPLE:my-type:[]:0.75:a:1:21.5:0.25",
                render(samples.get(1)));
        assertEquals("2:s-2:S2:MY-DB:/s/S2:SAMPLE:my-type:[]:0.75:a:7:21.5:0.25",
                render(samples.get(2)));
        assertEquals("2:s-2:S2:MY-DB:/s/S2:SAMPLE:my-type:[]:0.75:a:10:21.5:0.25",
                render(samples.get(3)));
        assertEquals("3:s-3:S3:MY-DB:/s/S3:SAMPLE:my-type:[]:null:d:4:12.375:0.5",
                render(samples.get(4)));
        assertEquals("3:s-3:S3:MY-DB:/s/S3:SAMPLE:my-type:[]:null:f:6:-1.5:0.75",
                render(samples.get(5)));
        assertEquals(6, samples.size());
        context.assertIsSatisfied();
    }

    private String render(ProteinRelatedSample sample)
    {
        char modifiedAminoAcid = sample.getModifiedAminoAcid();
        return sample.getId() + ":" + sample.getPermId() + ":" + sample.getCode() + ":"
                + sample.getIdentifier() + ":" + sample.getEntityKind() + ":"
                + sample.getEntityType() + ":" + sample.getProperties() + ":"
                + sample.getAbundance() + ":"
                + (modifiedAminoAcid == 0 ? "" : Character.toString(modifiedAminoAcid)) + ":"
                + sample.getModificationPosition() + ":" + sample.getModificationMass() + ":"
                + sample.getModificationFraction();
    }

    private SampleAbundance sampleAbundance(String permID, double abundance)
    {
        SampleAbundance sampleAbundance = new SampleAbundance();
        sampleAbundance.setSamplePermID(permID);
        sampleAbundance.setAbundance(abundance);
        return sampleAbundance;
    }

    private SamplePeptideModification modification(String permID, String sequence, int position,
            double mass, double fraction)
    {
        SamplePeptideModification modification = new SamplePeptideModification();
        modification.setSamplePermID(permID);
        modification.setSequence(sequence);
        modification.setPosition(position);
        modification.setMass(mass);
        modification.setFraction(fraction);
        return modification;
    }
}
