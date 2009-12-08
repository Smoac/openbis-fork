/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.AminoAcidMass;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.AnnotatedProtein;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.DataSet;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Database;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Experiment;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Parameter;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Peptide;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.PeptideModification;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProgramDetails;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Protein;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinAnnotation;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinGroup;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinProphetDetails;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinReference;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummary;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummaryDataFilter;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummaryHeader;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sample;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sequence;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class ResultDataSetUploaderTest extends AssertJUnit
{
    private static final long MOD_PEPTIDE_ID = 101L;

    private static final long PEPTIDE_ID = 99L;

    private static final long CELL_LYSATE_ID1 = 88L;

    private static final String CELL_LYSATE1 = "cell_lysate1";

    private static final String CELL_LYSATE_PERM_ID1 = "c1";

    private static final long SEQUENCE_ID = 77L;

    private static final long PROTEIN_REFERENCE_ID = 66L;

    private static final String UNIPROT_ID1 = "unipr1";

    private static final String PROTEIN_NAME1 = "my protein";

    private static final String SEQUENCE1 = "seq";

    private static final String UNIPROT_ID2 = "unipr2";

    private static final String PROTEIN_NAME2 = "my 2. protein";

    private static final String SEQUENCE2 = "seqe";

    private static final long PROTEIN1_ID = 55L;

    private static final long DATA_SET_ID = 42L;

    private static final String DATA_SET_CODE = "ds1";

    private static final long DATABASE_ID = 33l;

    private static final String NAME_AND_VERSION = "uniprot.HUMAN.v123.fasta";

    private static final String REFERENCE_DATABASE = "/here/and/there/" + NAME_AND_VERSION;

    private static final long EXPERIMENT_ID = 11l;

    private static final String EXPERIMENT_PERM_ID = "e1234";

    private static final long SAMPLE_ID = 22l;

    private static final String SAMPLE_PERM_ID = "s1234";

    private static final String DB_INSTANCE = "DB";

    private static final String GROUP_CODE = "G1";

    private Mockery context;

    private Connection connection;

    private IEncapsulatedOpenBISService service;

    private IProtDAO dao;

    private ResultDataSetUploader uploader;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        connection = context.mock(Connection.class);
        dao = context.mock(IProtDAO.class);
        service = context.mock(IEncapsulatedOpenBISService.class);

        uploader = new ResultDataSetUploader(dao, connection, service);
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testNoProteinsGetExperimentSampleDatabaseAndDataSet()
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).tryToGetExperimentByPermID(EXPERIMENT_PERM_ID);
                    Experiment experiment = new Experiment();
                    experiment.setPermID(EXPERIMENT_PERM_ID);
                    experiment.setId(EXPERIMENT_ID);
                    will(returnValue(experiment));

                    one(dao).tryToGetSampleByPermID(SAMPLE_PERM_ID);
                    Sample sample = new Sample();
                    sample.setPermID(SAMPLE_PERM_ID);
                    sample.setId(SAMPLE_ID);
                    will(returnValue(sample));

                    one(dao).tryToGetDatabaseByName(NAME_AND_VERSION);
                    Database database = new Database();
                    database.setNameAndVersion(NAME_AND_VERSION);
                    database.setId(DATABASE_ID);
                    will(returnValue(database));

                    one(dao).tryToGetDataSetByPermID(DATA_SET_CODE);
                    DataSet dataSet = new DataSet();
                    dataSet.setId(DATA_SET_ID);
                    dataSet.setDatabaseID(DATABASE_ID);
                    will(returnValue(dataSet));
                }
            });

        uploader.upload(createDataSetInfo(), createProteinSummary());

        context.assertIsSatisfied();
    }

    @Test
    public void testNoProteinsCreateExperimentSampleDatabaseAndDataSet()
    {
        prepareForCreatingExperimentSampleDatabaseAndDataSet();

        uploader.upload(createDataSetInfo(), createProteinSummary());

        context.assertIsSatisfied();
    }

    @Test
    public void testEmptyProteinGroup()
    {
        prepareForCreatingExperimentSampleDatabaseAndDataSet();

        ProteinSummary summary = createProteinSummary();
        summary.getProteinGroups().add(createProteinGroup());
        uploader.upload(createDataSetInfo(), summary);

        context.assertIsSatisfied();
    }

    @Test
    public void testProteinGroupWithTwoProteins()
    {
        prepareForCreatingExperimentSampleDatabaseAndDataSet();
        double probability = 1.0;
        prepareForCreatingProtein(probability);
        ProteinAnnotation a1 = createAnnotation(UNIPROT_ID1, PROTEIN_NAME1, SEQUENCE1);
        prepareForCreatingIdentifiedProtein(a1, false);
        ProteinAnnotation a2 = createAnnotation(UNIPROT_ID2, PROTEIN_NAME2, SEQUENCE2);
        prepareForCreatingIdentifiedProtein(a2, true);

        ProteinSummary summary = createProteinSummary();
        Protein p1 = createProtein(probability, a1, a2);
        p1.setPeptides(Collections.<Peptide> emptyList());
        summary.getProteinGroups().add(createProteinGroup(p1, new Protein()));

        uploader.upload(createDataSetInfo(), summary);
        context.assertIsSatisfied();
    }

    @Test
    public void testAbundancesForSameSampleInTwoDifferentProteins()
    {
        prepareForCreatingExperimentSampleDatabaseAndDataSet();
        double probability = 0.75;
        ProteinSummary summary = createProteinSummary();
        prepareForCreatingProtein(probability);
        ProteinAnnotation a1 = createAnnotation(UNIPROT_ID1, PROTEIN_NAME1, SEQUENCE1);
        prepareForCreatingIdentifiedProtein(a1, false);
        Protein p1 = createProtein(probability, a1);
        p1.setName(PROTEIN_NAME1);
        p1.getParameters().add(createAbundance(CELL_LYSATE1, 2.5));
        p1.getParameters().add(new Parameter());
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new GroupIdentifier(DB_INSTANCE, GROUP_CODE), CELL_LYSATE1);
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetSampleWithExperiment(sampleIdentifier);
                    ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample =
                            new ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample();
                    sample.setPermId(CELL_LYSATE_PERM_ID1);
                    will(returnValue(sample));

                    one(dao).tryToGetSampleByPermID(CELL_LYSATE_PERM_ID1);
                    will(returnValue(null));
                    one(dao).createSample(EXPERIMENT_ID, CELL_LYSATE_PERM_ID1);
                    will(returnValue(CELL_LYSATE_ID1));

                    one(dao).createAbundance(PROTEIN1_ID, CELL_LYSATE_ID1, 2.5);
                }
            });
        p1.setPeptides(Collections.<Peptide> emptyList());

        prepareForCreatingProtein(probability);
        summary.getProteinGroups().add(createProteinGroup(p1));
        ProteinAnnotation a2 = createAnnotation(UNIPROT_ID2, PROTEIN_NAME2, SEQUENCE2);
        prepareForCreatingIdentifiedProtein(a2, false);
        Protein p2 = createProtein(probability, a2);
        p2.setName(PROTEIN_NAME1);
        p2.getParameters().add(createAbundance(CELL_LYSATE1, 42.5));
        context.checking(new Expectations()
            {
                {
                    one(dao).createAbundance(PROTEIN1_ID, CELL_LYSATE_ID1, 42.5);
                }
            });
        p2.setPeptides(Collections.<Peptide> emptyList());
        summary.getProteinGroups().add(createProteinGroup(p2));
        prepareForCommit();

        uploader.upload(createDataSetInfo(), summary);
        uploader.commit();
        context.assertIsSatisfied();
    }

    @Test
    public void testAbundancesForNonExistingSample()
    {
        prepareForCreatingExperimentSampleDatabaseAndDataSet();
        double probability = 0.75;
        prepareForCreatingProtein(probability);
        ProteinSummary summary = createProteinSummary();
        Protein p1 = createProtein(probability);
        p1.setName(PROTEIN_NAME1);
        p1.getParameters().add(createAbundance(CELL_LYSATE1, 2.5));
        p1.getParameters().add(new Parameter());
        final GroupIdentifier groupIdentifier = new GroupIdentifier(DB_INSTANCE, GROUP_CODE);
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(groupIdentifier, CELL_LYSATE1);
        final ListSamplesByPropertyCriteria criteria =
                new ListSamplesByPropertyCriteria(AbundanceHandler.MZXML_FILENAME, CELL_LYSATE1,
                        GROUP_CODE, null);
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(null));

                    one(service).listSamplesByCriteria(
                            with(new BaseMatcher<ListSamplesByPropertyCriteria>()
                                {
                                    public boolean matches(Object item)
                                    {
                                        return criteria.toString().equals(item.toString());
                                    }

                                    public void describeTo(Description description)
                                    {
                                        description.appendValue(criteria);
                                    }
                                }));
                }
            });
        p1.setPeptides(Collections.<Peptide> emptyList());
        summary.getProteinGroups().add(createProteinGroup(p1));
        prepareForRollback();

        try
        {
            uploader.upload(createDataSetInfo(), summary);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            AssertionUtil.assertContains("Protein '" + PROTEIN_NAME1
                    + "' has an abundance value for an unidentified sample: " + CELL_LYSATE1, ex
                    .getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testProteinWithUnmodifiedPeptide()
    {
        prepareForCreatingExperimentSampleDatabaseAndDataSet();
        double probability = 1.0;
        prepareForCreatingProtein(probability);
        ProteinAnnotation a1 = createAnnotation(UNIPROT_ID1, PROTEIN_NAME1, SEQUENCE1);
        prepareForCreatingIdentifiedProtein(a1, false);

        ProteinSummary summary = createProteinSummary();
        Protein p1 = createProtein(probability, a1);
        final Peptide peptide = new Peptide();
        peptide.setSequence("abcd");
        peptide.setCharge(3);
        p1.setPeptides(Arrays.asList(peptide));
        summary.getProteinGroups().add(createProteinGroup(p1));
        context.checking(new Expectations()
            {
                {
                    one(dao).createPeptide(PROTEIN1_ID, peptide.getSequence(), peptide.getCharge());
                    will(returnValue(PEPTIDE_ID));
                }
            });

        uploader.upload(createDataSetInfo(), summary);

        context.assertIsSatisfied();
    }

    @Test
    public void testProteinWithModifiedPeptide()
    {
        prepareForCreatingExperimentSampleDatabaseAndDataSet();
        double probability = 1.0;
        prepareForCreatingProtein(probability);
        ProteinAnnotation a1 = createAnnotation(UNIPROT_ID1, PROTEIN_NAME1, SEQUENCE1);
        prepareForCreatingIdentifiedProtein(a1, false);

        ProteinSummary summary = createProteinSummary();
        Protein p1 = createProtein(probability, a1);
        final Peptide peptide = new Peptide();
        peptide.setSequence("abcd");
        peptide.setCharge(3);
        final PeptideModification modification = new PeptideModification();
        modification.setNTermMass(42);
        modification.setCTermMass(4711);
        final AminoAcidMass mass = new AminoAcidMass();
        mass.setMass(123);
        mass.setPosition(1);
        modification.setAminoAcidMasses(Arrays.asList(mass));
        peptide.getModifications().add(modification);
        p1.setPeptides(Arrays.asList(peptide));
        summary.getProteinGroups().add(createProteinGroup(p1));
        context.checking(new Expectations()
            {
                {
                    one(dao).createPeptide(PROTEIN1_ID, peptide.getSequence(), peptide.getCharge());
                    will(returnValue(PEPTIDE_ID));

                    one(dao).createModifiedPeptide(PEPTIDE_ID, modification.getNTermMass(),
                            modification.getCTermMass());
                    will(returnValue(MOD_PEPTIDE_ID));

                    one(dao).createModification(MOD_PEPTIDE_ID, mass.getPosition(), mass.getMass());
                }
            });

        uploader.upload(createDataSetInfo(), summary);

        context.assertIsSatisfied();
    }

    private Parameter createAbundance(String sampleCode, double value)
    {
        Parameter parameter = new Parameter();
        parameter.setName(sampleCode);
        parameter.setValue(Double.toString(value));
        parameter.setType(ResultDataSetUploader.PARAMETER_TYPE_ABUNDANCE);
        return parameter;
    }

    private Protein createProtein(double probability, ProteinAnnotation... annotations)
    {
        Protein protein = new Protein();
        protein.setProbability(probability);
        if (annotations.length > 0)
        {
            protein.setAnnotation(annotations[0]);
        }
        List<AnnotatedProtein> indistinguishableProteins = new ArrayList<AnnotatedProtein>();
        for (int i = 1; i < annotations.length; i++)
        {
            AnnotatedProtein annotatedProtein = new AnnotatedProtein();
            annotatedProtein.setAnnotation(annotations[i]);
            indistinguishableProteins.add(annotatedProtein);
        }
        protein.setIndistinguishableProteins(indistinguishableProteins);
        return protein;
    }

    private ProteinAnnotation createAnnotation(String uniprotID, String description, String sequence)
    {
        ProteinAnnotation proteinAnnotation = new ProteinAnnotation();
        proteinAnnotation.setDescription(uniprotID
                + " "
                + ProteinDescription.createKeyValuePair(ProteinDescription.DESCRIPTION_KEY,
                        description) + " "
                + ProteinDescription.createKeyValuePair(ProteinDescription.SEQUENCE_KEY, sequence));
        return proteinAnnotation;
    }

    private ProteinGroup createProteinGroup(Protein... proteins)
    {
        ProteinGroup proteinGroup = new ProteinGroup();
        proteinGroup.setProteins(Arrays.asList(proteins));
        return proteinGroup;
    }

    private void prepareForCreatingProtein(final double probability)
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).createProtein(DATA_SET_ID, probability);
                    will(returnValue(PROTEIN1_ID));
                }
            });
    }

    private void prepareForCreatingIdentifiedProtein(ProteinAnnotation annotation,
            final boolean referenceExist)
    {
        ProteinDescription proteinDescription = new ProteinDescription(annotation.getDescription());
        final String uniprotID = proteinDescription.getAccessionNumber();
        final String description = proteinDescription.getDescription();
        final String sequence = proteinDescription.getSequence();
        context.checking(new Expectations()
            {
                {
                    one(dao).tryToGetProteinReference(uniprotID);
                    if (referenceExist == false)
                    {
                        will(returnValue(null));

                        one(dao).createProteinReference(uniprotID, description);
                        will(returnValue(PROTEIN_REFERENCE_ID));
                    } else
                    {
                        ProteinReference proteinReference = new ProteinReference();
                        proteinReference.setId(PROTEIN_REFERENCE_ID);
                        will(returnValue(proteinReference));

                        one(dao).updateProteinReferenceDescription(PROTEIN_REFERENCE_ID,
                                description);
                    }

                    one(dao).tryToGetSequencesByReferenceAndDatabase(PROTEIN_REFERENCE_ID,
                            DATABASE_ID);
                    Sequence seq = new Sequence(sequence);
                    seq.setId(SEQUENCE_ID);
                    if (referenceExist == false)
                    {
                        will(returnValue(null));

                        seq.setDatabaseID(DATABASE_ID);
                        seq.setProteinReferenceID(PROTEIN_REFERENCE_ID);
                        one(dao).createSequence(seq);
                        will(returnValue(SEQUENCE_ID));
                    } else
                    {
                        will(returnValue(Arrays.asList(seq)));
                    }

                    one(dao).createIdentifiedProtein(PROTEIN1_ID, SEQUENCE_ID);
                }
            });
    }

    private void prepareForCreatingExperimentSampleDatabaseAndDataSet()
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).tryToGetExperimentByPermID(EXPERIMENT_PERM_ID);
                    will(returnValue(null));
                    one(dao).createExperiment(EXPERIMENT_PERM_ID);
                    will(returnValue(EXPERIMENT_ID));

                    one(dao).tryToGetSampleByPermID(SAMPLE_PERM_ID);
                    will(returnValue(null));
                    one(dao).createSample(EXPERIMENT_ID, SAMPLE_PERM_ID);
                    will(returnValue(SAMPLE_ID));

                    one(dao).tryToGetDatabaseByName(NAME_AND_VERSION);
                    will(returnValue(null));
                    one(dao).createDatabase(NAME_AND_VERSION);
                    will(returnValue(DATABASE_ID));

                    one(dao).tryToGetDataSetByPermID(DATA_SET_CODE);
                    will(returnValue(null));
                    one(dao).createDataSet(EXPERIMENT_ID, SAMPLE_ID, DATA_SET_CODE, DATABASE_ID);
                    will(returnValue(DATA_SET_ID));
                }
            });
    }

    private void prepareForCommit()
    {
        context.checking(new Expectations()
            {
                {
                    try
                    {
                        one(connection).commit();
                    } catch (SQLException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }
            });
    }

    private void prepareForRollback()
    {
        context.checking(new Expectations()
            {
                {
                    try
                    {
                        one(connection).rollback();
                    } catch (SQLException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }
            });
    }

    private DataSetInformation createDataSetInfo()
    {
        DataSetInformation info = new DataSetInformation();
        info.setDataSetCode(DATA_SET_CODE);
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample();
        sample.setPermId(SAMPLE_PERM_ID);
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment();
        experiment.setPermId(EXPERIMENT_PERM_ID);
        sample.setExperiment(experiment);
        Group group = new Group();
        DatabaseInstance databaseInstance = new DatabaseInstance();
        databaseInstance.setCode(DB_INSTANCE);
        group.setInstance(databaseInstance);
        group.setCode(GROUP_CODE);
        sample.setGroup(group);
        info.setSample(sample);
        return info;
    }

    private ProteinSummary createProteinSummary()
    {
        ProteinSummary proteinSummary = new ProteinSummary();
        ProteinSummaryHeader proteinSummaryHeader = new ProteinSummaryHeader();
        proteinSummaryHeader.setReferenceDatabase(REFERENCE_DATABASE);
        ProgramDetails programDetails = new ProgramDetails();
        ProteinProphetDetails proteinProphetDetails = new ProteinProphetDetails();
        ProteinSummaryDataFilter m1 = createFilter(0.5, 0.125);
        ProteinSummaryDataFilter m2 = createFilter(1.0, 0.0);
        proteinProphetDetails.setDataFilters(Arrays.asList(m1, m2));
        programDetails.setSummary(new Object[]
            { proteinProphetDetails });
        proteinSummaryHeader.setProgramDetails(programDetails);
        proteinSummary.setSummaryHeader(proteinSummaryHeader);
        proteinSummary.setProteinGroups(new ArrayList<ProteinGroup>());
        return proteinSummary;
    }

    private ProteinSummaryDataFilter createFilter(final double probability, final double fdr)
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).createProbabilityToFDRMapping(DATA_SET_ID, probability, fdr);
                }
            });
        ProteinSummaryDataFilter mapping = new ProteinSummaryDataFilter();
        mapping.setMinProbability(probability);
        mapping.setFalsePositiveErrorRate(fdr);
        return mapping;
    }

}
