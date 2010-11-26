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

package ch.systemsx.cisd.openbis.generic.server.business.bo.dynamic_property;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.Session;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.AbstractBOTest;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityPropertiesConverter.IHibernateSessionProvider;
import ch.systemsx.cisd.openbis.generic.server.business.bo.dynamic_property.calculator.AbstractEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.business.bo.dynamic_property.calculator.BasicPropertyAdaptor;
import ch.systemsx.cisd.openbis.generic.server.business.bo.dynamic_property.calculator.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.business.bo.dynamic_property.calculator.IEntityPropertyAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author Piotr Buczek
 */
public class DynamicPropertyEvaluatorTest extends AbstractBOTest
{

    private DynamicPropertyEvaluator evaluator;

    private IHibernateSessionProvider sessionProvider;

    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
        final Session session = new Mockery().mock(Session.class);
        sessionProvider = new IHibernateSessionProvider()
            {
                public Session getSession()
                {
                    return session;
                }
            };
        evaluator = new DynamicPropertyEvaluator(daoFactory, sessionProvider);
    }

    @Test
    public void testEvaluateProperty()
    {
        // test evaluation of a single dynamic property

        IEntityPropertyAdaptor p1 = createProperty("p1", "v1");
        IEntityPropertyAdaptor p2 = createProperty("p2", "v2");

        final IEntityAdaptor entityAdaptor =
                createEntity("e", Arrays.asList(new IEntityPropertyAdaptor[]
                    { p1, p2 }));
        final ScriptPE script =
                createScript("s1", "entity.propertyValue('p1') + ' ' + entity.propertyValue('p2')");

        final PropertyTypePE propertyType = createPropertyType("dynamic");
        final EntityTypePropertyTypePE etpt =
                createDynamicSamplePropertyAssignment(propertyType, script);
        final String result = evaluator.evaluateProperty(entityAdaptor, etpt);
        assertEquals(p1.valueAsString() + " " + p2.valueAsString(), result);
    }

    @Test
    public void testEvaluateProperties()
    {
        // check
        // - successful evaluation of varchar dynamic property
        // - successful evaluation of integer dynamic property
        // - error handling (error messages are stored as property values)
        // -- storing validation error when script evaluates to string instead of expected integer
        // -- storing python error when script tries to invoke nonexisting method

        Set<SamplePropertyPE> properties = new HashSet<SamplePropertyPE>();
        // create normal properties
        SamplePropertyPE p1 = createSampleProperty("p1", "v1");
        SamplePropertyPE p2 = createSampleProperty("p2", "v2");
        SamplePropertyPE p3 = createSampleProperty("p3", "v3");
        properties.add(p1);
        properties.add(p2);
        properties.add(p3);

        // create dynamic properties
        final ScriptPE script1 =
                createScript("s1", "entity.propertyValue('p1') + ' ' + entity.propertyValue('p2')");
        final ScriptPE script2 = createScript("s2", "entity.properties().size()");
        final String s3 = "entity.getCode()";
        final ScriptPE script3 = createScript("s3", s3);
        final SamplePropertyPE dp1 = createDynamicSampleProperty("dp1", script1);
        final SamplePropertyPE dp1Error =
                createDynamicSampleProperty(createPropertyType("dp1_error", DataTypeCode.INTEGER),
                        script1);
        final SamplePropertyPE dp2 =
                createDynamicSampleProperty(createPropertyType("dp2", DataTypeCode.INTEGER),
                        script2);
        final SamplePropertyPE dp3Error = createDynamicSampleProperty("dp3_error", script3);
        properties.add(dp1);
        properties.add(dp1Error);
        properties.add(dp2);
        properties.add(dp3Error);

        // create sample with all properties created above and evaluate dynamic properties
        final SamplePE sample = createSample("s1", properties);
        evaluator.evaluateProperties(sample);

        // check if evaluated values are correct
        final String expectedDp1Value = String.format("%s %s", p1.getValue(), p2.getValue());
        assertEquals(expectedDp1Value, dp1.getValue());
        final String expectedDp1ErrorValue =
                expectedErrorMessage("Integer value '" + expectedDp1Value
                        + "' has improper format.");
        assertEquals(expectedDp1ErrorValue, dp1Error.getValue());
        assertEquals(properties.size() + "", dp2.getValue());
        final String expectedDp3ErrorValue =
                expectedErrorMessage("Error evaluating '" + s3 + "': AttributeError: getCode");
        assertEquals(expectedDp3ErrorValue, dp3Error.getValue());
    }

    @Test
    public void testEvaluateVocabularyProperties()
    {
        // check handling of vocabulary properties
        Set<SamplePropertyPE> properties = new HashSet<SamplePropertyPE>();

        PropertyTypePE vocabularyPropertyType =
                createPropertyType("vp", DataTypeCode.CONTROLLEDVOCABULARY);
        VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.setCode("v");
        vocabulary.setChosenFromList(true);
        VocabularyTermPE term1 = createTerm("t1");
        VocabularyTermPE term2 = createTerm("t2");
        VocabularyTermPE term3 = createTerm("t3");
        vocabulary.addTerm(term1);
        vocabulary.addTerm(term2);
        vocabulary.addTerm(term3);
        vocabularyPropertyType.setVocabulary(vocabulary);

        SamplePropertyPE vp = createSampleVocabularyProperty(vocabularyPropertyType, term2);
        properties.add(vp);

        // create dynamic properties
        final ScriptPE script1 = createScript("s1", "entity.propertyValue('vp')");
        final SamplePropertyPE dpVarchar = createDynamicSampleProperty("dpVarchar", script1);
        final PropertyTypePE dynamicPropertyType =
                createPropertyType("dpVocabulary", DataTypeCode.CONTROLLEDVOCABULARY);
        dynamicPropertyType.setVocabulary(vocabulary);
        final SamplePropertyPE dpVocabulary =
                createDynamicSampleProperty(dynamicPropertyType, script1);
        final ScriptPE script2 = createScript("s2", "'fake_term'");
        final PropertyTypePE dynamicPropertyTypeError =
                createPropertyType("dpVocabularyError", DataTypeCode.CONTROLLEDVOCABULARY);
        dynamicPropertyTypeError.setVocabulary(vocabulary);
        final SamplePropertyPE dpVocabularyError =
                createDynamicSampleProperty(dynamicPropertyTypeError, script2);
        properties.add(dpVarchar);
        properties.add(dpVocabulary);
        properties.add(dpVocabularyError);

        // create sample with all properties created above and evaluate dynamic properties
        final SamplePE sample = createSample("s1", properties);
        evaluator.evaluateProperties(sample);

        // check if evaluated values are correct
        assertEquals(term2.getCode(), dpVarchar.getValue());
        assertEquals(null, dpVarchar.getVocabularyTerm());
        assertEquals(null, dpVocabulary.getValue());
        assertEquals(term2, dpVocabulary.getVocabularyTerm());
        final String expectedDpVocabularyErrorValue =
                expectedErrorMessage("Vocabulary value 'FAKE_TERM' is not valid. "
                        + "It must exist in '" + vocabulary.getCode()
                        + "' controlled vocabulary [T1, T2, T3]");
        assertEquals(expectedDpVocabularyErrorValue, dpVocabularyError.getValue());
        assertEquals(null, dpVocabularyError.getVocabularyTerm());
    }

    @Test
    public void testEvaluateMaterialProperties()
    {
        // check handling of material properties
        Set<SamplePropertyPE> properties = new HashSet<SamplePropertyPE>();

        PropertyTypePE materialPropertyType = createPropertyType("mp", DataTypeCode.MATERIAL);
        final String materialTypeCode = "M_TYPE";
        MaterialTypePE materialType = new MaterialTypePE();
        materialType.setCode(materialTypeCode);
        materialPropertyType.setMaterialType(materialType);

        final MaterialPE material = new MaterialPE();
        material.setCode("M_CODE");
        material.setMaterialType(materialType);

        SamplePropertyPE mp = createSampleMaterialProperty(materialPropertyType, material);
        properties.add(mp);

        // create dynamic properties
        final ScriptPE script1 = createScript("s1", "entity.propertyValue('mp')");

        final SamplePropertyPE dpVarchar = createDynamicSampleProperty("dpVarchar", script1);

        final PropertyTypePE dynamicPropertyType =
                createPropertyType("dpMaterial", DataTypeCode.MATERIAL);
        dynamicPropertyType.setMaterialType(materialType);
        final SamplePropertyPE dpMaterial =
                createDynamicSampleProperty(dynamicPropertyType, script1);

        final ScriptPE scriptError1 = createScript("se1", "'fake_material'");
        final PropertyTypePE dynamicPropertyTypeError1 =
                createPropertyType("dpMaterialError1", DataTypeCode.MATERIAL);
        dynamicPropertyTypeError1.setMaterialType(materialType);
        final SamplePropertyPE dpMaterialError1 =
                createDynamicSampleProperty(dynamicPropertyTypeError1, scriptError1);

        final MaterialIdentifier fakeTypeIdentifier = new MaterialIdentifier("fake", "fake_type");
        final ScriptPE scriptError2 =
                createScript("se2", "'" + fakeTypeIdentifier.toString() + "'");
        final PropertyTypePE dynamicPropertyTypeError2 =
                createPropertyType("dpMaterialError2", DataTypeCode.MATERIAL);
        dynamicPropertyTypeError2.setMaterialType(materialType);
        final SamplePropertyPE dpMaterialError2 =
                createDynamicSampleProperty(dynamicPropertyTypeError2, scriptError2);

        final MaterialIdentifier fakeMaterialIdentifier =
                new MaterialIdentifier("fake", materialTypeCode);
        final ScriptPE scriptError3 =
                createScript("se3", "'" + fakeMaterialIdentifier.toString() + "'");
        final PropertyTypePE dynamicPropertyTypeError3 =
                createPropertyType("dpMaterialError3", DataTypeCode.MATERIAL);
        dynamicPropertyTypeError3.setMaterialType(materialType);
        final SamplePropertyPE dpMaterialError3 =
                createDynamicSampleProperty(dynamicPropertyTypeError3, scriptError3);

        properties.add(dpVarchar);
        properties.add(dpMaterial);
        properties.add(dpMaterialError1);
        properties.add(dpMaterialError2);
        properties.add(dpMaterialError3);

        context.checking(new Expectations()
            {
                {
                    one(materialDAO).tryFindMaterial(sessionProvider.getSession(),
                            new MaterialIdentifier(material.getCode(), materialTypeCode));
                    will(returnValue(material));

                    one(materialDAO).tryFindMaterial(sessionProvider.getSession(),
                            fakeMaterialIdentifier);
                    will(returnValue(null));
                }
            });

        // create sample with all properties created above and evaluate dynamic properties
        final SamplePE sample = createSample("s1", properties);
        evaluator.evaluateProperties(sample);

        // check if evaluated values are correct
        final String materialIdentifier =
                MaterialIdentifier.print(material.getCode(), materialTypeCode);
        assertEquals(materialIdentifier, dpVarchar.getValue());
        assertEquals(null, dpVarchar.getMaterialValue());

        assertEquals(null, dpMaterial.getValue());
        assertEquals(material, dpMaterial.getMaterialValue());

        final String expectedDpMaterialError1Value =
                expectedErrorMessage("Material specification 'fake_material' has improper format. "
                        + "Expected '<CODE> (<TYPE>)'.");
        assertEquals(expectedDpMaterialError1Value, dpMaterialError1.getValue());
        assertEquals(null, dpMaterialError1.getVocabularyTerm());

        final String expectedDpMaterialError2Value =
                expectedErrorMessage("Material '" + fakeTypeIdentifier.toString()
                        + "' is of wrong type. Expected: '" + materialTypeCode + "'.");
        assertEquals(expectedDpMaterialError2Value, dpMaterialError2.getValue());
        assertEquals(null, dpMaterialError2.getVocabularyTerm());

        final String expectedDpMaterialError3Value =
                expectedErrorMessage("No material could be found for identifier '"
                        + fakeMaterialIdentifier.toString() + "'.");
        assertEquals(expectedDpMaterialError3Value, dpMaterialError3.getValue());
        assertEquals(null, dpMaterialError3.getVocabularyTerm());
    }

    @Test
    public void testEvaluatePropertyDependingOnAnotherDynamicProperty()
    {
        // check evaluation of dynamic properties that depend on other dynamic properties
        // (with and without cyclic dependencies)

        final SamplePropertyPE p1 = createSampleProperty("p1", "v1"); // normal property
        final ScriptPE scriptP1 = createScript("get p1", "entity.propertyValue('p1')");
        final ScriptPE scriptDp1 = createScript("get dp1", "entity.propertyValue('dp1')");
        final ScriptPE scriptDp2 = createScript("get dp2", "entity.propertyValue('dp2')");
        final ScriptPE scriptDp3 = createScript("get dp3", "entity.propertyValue('dp3')");

        // create sample with dependency
        // p1 <- dp1 <- dp2, dp3
        Set<SamplePropertyPE> properties = new LinkedHashSet<SamplePropertyPE>();
        SamplePropertyPE dp1 = createDynamicSampleProperty("dp1", scriptP1);
        SamplePropertyPE dp2 = createDynamicSampleProperty("dp2", scriptDp1);
        SamplePropertyPE dp3 = createDynamicSampleProperty("dp3", scriptDp1);
        properties.add(p1);
        properties.add(dp1);
        properties.add(dp2);
        properties.add(dp3);
        SamplePE sample = createSample("s1", properties);
        evaluator.evaluateProperties(sample);
        // all values should be equal to value of p1
        assertEquals(p1.getValue(), dp1.getValue());
        assertEquals(p1.getValue(), dp2.getValue());
        assertEquals(p1.getValue(), dp3.getValue());

        // create sample with circular dependency
        // dp1 -> dp1
        properties = new LinkedHashSet<SamplePropertyPE>();
        dp1 = createDynamicSampleProperty("dp1", scriptDp1);
        properties.add(p1);
        properties.add(dp1);
        sample = createSample("s1", properties);
        evaluator.evaluateProperties(sample);
        // cyclic dependency should be found
        assertEquals(expectedCyclicDependencyErrorMessage(dp1, dp1), dp1.getValue());

        // dp1 -> dp2 -> dp1
        properties = new LinkedHashSet<SamplePropertyPE>();
        dp1 = createDynamicSampleProperty("dp1", scriptDp2);
        dp2 = createDynamicSampleProperty("dp2", scriptDp1);
        properties.add(p1);
        properties.add(dp1);
        properties.add(dp2);
        sample = createSample("s1", properties);
        evaluator.evaluateProperties(sample);
        // cyclic dependency should be found
        assertEquals(expectedCyclicDependencyErrorMessage(dp2, dp1, dp2), dp1.getValue());
        assertEquals(expectedCyclicDependencyErrorMessage(dp2, dp1, dp2), dp2.getValue());

        // dp1 -> dp2 -> dp3 -> dp1
        properties = new LinkedHashSet<SamplePropertyPE>();
        dp1 = createDynamicSampleProperty("dp1", scriptDp2);
        dp2 = createDynamicSampleProperty("dp2", scriptDp3);
        dp3 = createDynamicSampleProperty("dp3", scriptDp1);
        properties.add(p1);
        properties.add(dp1);
        properties.add(dp2);
        properties.add(dp3);
        sample = createSample("s1", properties);
        evaluator.evaluateProperties(sample);
        // cyclic dependency should be found
        assertEquals(expectedCyclicDependencyErrorMessage(dp2, dp3, dp1, dp2), dp1.getValue());
        assertEquals(expectedCyclicDependencyErrorMessage(dp2, dp3, dp1, dp2), dp2.getValue());
        assertEquals(expectedCyclicDependencyErrorMessage(dp2, dp3, dp1, dp2), dp3.getValue());
    }

    //
    // helper methods
    //

    private static String expectedErrorMessage(String message)
    {
        return String.format("%sERROR: %s", BasicConstant.ERROR_PROPERTY_PREFIX, message);
    }

    private static String expectedCyclicDependencyErrorMessage(EntityPropertyPE... properties)
    {
        StringBuilder path = new StringBuilder();
        for (EntityPropertyPE property : properties)
        {
            path.append(property.getEntityTypePropertyType().getPropertyType().getCode()
                    .toUpperCase());
            path.append(" -> ");
        }
        path.delete(path.length() - 4, path.length());
        return expectedErrorMessage(String.format(
                "cycle of dependencies found between dynamic properties: %s", path.toString()));
    }

    private static SamplePE createSample(String code, Set<SamplePropertyPE> properties)
    {
        final SamplePE result = new SamplePE();
        result.setCode(code);
        result.setProperties(properties);
        return result;
    }

    private static ScriptPE createScript(String name, String script)
    {
        ScriptPE result = new ScriptPE();
        result.setName(name);
        result.setScript(script);
        return result;
    }

    private static PropertyTypePE createPropertyType(String propertyCode)
    {
        return createPropertyType(propertyCode, DataTypeCode.VARCHAR);
    }

    private static PropertyTypePE createPropertyType(String propertyCode, DataTypeCode dataTypeCode)
    {
        final PropertyTypePE propertyType = new PropertyTypePE();
        propertyType.setCode(propertyCode);
        final DataTypePE dataType = new DataTypePE();
        dataType.setCode(dataTypeCode);
        propertyType.setType(dataType);
        return propertyType;
    }

    private static SamplePropertyPE createSampleProperty(final String propertyTypeCode,
            final String value)
    {
        PropertyTypePE propertyType = createPropertyType(propertyTypeCode);
        return createSampleProperty(propertyType, value);
    }

    private static SamplePropertyPE createSampleProperty(final PropertyTypePE propertyType,
            final String value)
    {
        final SamplePropertyPE result = new SamplePropertyPE();
        final SampleTypePropertyTypePE entityTypePropertyType = new SampleTypePropertyTypePE();
        entityTypePropertyType.setPropertyType(propertyType);
        result.setEntityTypePropertyType(entityTypePropertyType);
        result.setValue(value);
        return result;
    }

    private static SamplePropertyPE createSampleVocabularyProperty(
            final PropertyTypePE propertyType, final VocabularyTermPE term)
    {
        final SamplePropertyPE result = new SamplePropertyPE();
        final SampleTypePropertyTypePE entityTypePropertyType = new SampleTypePropertyTypePE();
        entityTypePropertyType.setPropertyType(propertyType);
        result.setEntityTypePropertyType(entityTypePropertyType);
        result.setVocabularyTerm(term);
        return result;
    }

    private static SamplePropertyPE createSampleMaterialProperty(final PropertyTypePE propertyType,
            final MaterialPE material)
    {
        final SamplePropertyPE result = new SamplePropertyPE();
        final SampleTypePropertyTypePE entityTypePropertyType = new SampleTypePropertyTypePE();
        entityTypePropertyType.setPropertyType(propertyType);
        result.setEntityTypePropertyType(entityTypePropertyType);
        result.setMaterialValue(material);
        return result;
    }

    private static SamplePropertyPE createDynamicSampleProperty(final String propertyTypeCode,
            final ScriptPE script)
    {
        PropertyTypePE propertyType = createPropertyType(propertyTypeCode);
        return createDynamicSampleProperty(propertyType, script);
    }

    private static SamplePropertyPE createDynamicSampleProperty(final PropertyTypePE propertyType,
            final ScriptPE script)
    {
        final SamplePropertyPE result = new SamplePropertyPE();
        final SampleTypePropertyTypePE entityTypePropertyType =
                createDynamicSamplePropertyAssignment(propertyType, script);
        result.setEntityTypePropertyType(entityTypePropertyType);
        return result;
    }

    private static SampleTypePropertyTypePE createDynamicSamplePropertyAssignment(
            final PropertyTypePE propertyType, ScriptPE script)
    {
        final SampleTypePropertyTypePE assignment = new SampleTypePropertyTypePE();
        assignment.setPropertyType(propertyType);
        assignment.setDynamic(true);
        assignment.setScript(script);
        assignment.setEntityType(new SampleTypePE());
        return assignment;
    }

    private static IEntityAdaptor createEntity(final String code,
            final Collection<IEntityPropertyAdaptor> properties)
    {
        final AbstractEntityAdaptor result = new AbstractEntityAdaptor(code);
        if (properties != null)
        {
            for (IEntityPropertyAdaptor property : properties)
            {
                result.addProperty(property);
            }
        }
        return result;
    }

    private static IEntityPropertyAdaptor createProperty(final String propertyTypeCode,
            final String value)
    {
        return new BasicPropertyAdaptor(propertyTypeCode, value);
    }

    private static VocabularyTermPE createTerm(String termCode)
    {
        final VocabularyTermPE result = new VocabularyTermPE();
        result.setCode(termCode);
        return result;
    }

}
