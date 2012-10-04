/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.application;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.DataSetType;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Script;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
public class PublicApiApplicationRunner implements ApplicationRunner
{

    private final UidGenerator uid;

    private ICommonServer commonServer;

    @SuppressWarnings("unused")
    private IETLLIMSService etlService;

    private IDssServiceRpcGeneric dss;

    private String session;

    private String lastSuccessfulLogin;

    public PublicApiApplicationRunner(String openbisUrl, String dssUrl, UidGenerator uid)
    {
        this.uid = uid;
        this.commonServer =
                HttpInvokerUtils.createServiceStub(ICommonServer.class,
                        openbisUrl + "/openbis/rmi-common", 60000);
        this.etlService =
                HttpInvokerUtils.createServiceStub(IETLLIMSService.class,
                        openbisUrl + "/openbis/rmi-etl", 60000);

        this.dss =
                HttpInvokerUtils.createStreamSupportingServiceStub(IDssServiceRpcGeneric.class,
                        dssUrl + "/datastore_server/rmi-dss-api-v1", 60000);

    }

    @Override
    public String uid()
    {
        return uid.uid();
    }

    @Override
    public void login(String userName, String password)
    {
        session =
                commonServer
                        .tryToAuthenticate(userName, password).getSessionToken();
        if (session != null)
        {
            lastSuccessfulLogin = userName;
        }
    }

    @Override
    public void logout()
    {
    }

    @Override
    public Space create(Space space)
    {
        return null;
    }

    @Override
    public void delete(Space space)
    {
    }

    @Override
    public Project create(Project project)
    {
        return null;
    }

    @Override
    public void delete(Project project)
    {
    }

    @Override
    public SampleType create(SampleType sampleType)
    {
        return null;
    }

    @Override
    public void update(SampleType sampleType)
    {
    }

    @Override
    public void delete(SampleType sampleType)
    {
    }

    @Override
    public ExperimentType create(ExperimentType experimentType)
    {
        return null;
    }

    @Override
    public void delete(ExperimentType experimentType)
    {
    }

    @Override
    public PropertyType create(PropertyType propertyType)
    {
        return null;
    }

    @Override
    public void delete(PropertyType propertyType)
    {
    }

    @Override
    public Vocabulary create(Vocabulary vocabulary)
    {
        return null;
    }

    @Override
    public void delete(Vocabulary vocabulary)
    {
    }

    @Override
    public Sample create(Sample sample)
    {
        return null;
    }

    @Override
    public Experiment create(Experiment experiment)
    {
        return null;
    }

    @Override
    public PropertyTypeAssignment create(PropertyTypeAssignment assignment)
    {
        return null;
    }

    @Override
    public DataSetType create(DataSetType type)
    {
        commonServer.registerDataSetType(session, convertToPublicApi(type));
        return type;
    }

    @Override
    public DataSet create(DataSet dataSet)
    {
        DataSetCreator creator = new DataSetCreator("data set content");
        String code = dss.putDataSet(session, creator.getMetadata(dataSet), creator.getData());
        dataSet.setCode(code);
        return dataSet;
    }

    private ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType convertToPublicApi(
            DataSetType type)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType dataSetType =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType();

        dataSetType.setCode(type.getCode());
        dataSetType.setDataSetKind(DataSetKind.PHYSICAL);
        return dataSetType;
    }

    @Override
    public String loggedInAs()
    {
        try
        {
            commonServer.checkSession(session);
            return lastSuccessfulLogin;
        } catch (InvalidSessionException e)
        {
            return null;
        }
    }

    @Override
    public Script create(Script script)
    {
        return null;
    }

}
