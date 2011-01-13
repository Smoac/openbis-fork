import ch.systemsx.cisd.openbis.generic.shared.dto.identifier as identifier
import java.io as io
import ch.systemsx.cisd.openbis.generic.shared.basic.dto as dto

# Create the Experiment Identifier
identifier = identifier.ExperimentIdentifierFactory("/SPACE/PROJECT/EXP-CODE").createIdentifier()

# Register data set 1
registrationDetails = factory.createRegistrationDetails()
dataSetInformation = registrationDetails.getDataSetInformation()
dataSetInformation.setExperimentIdentifier(identifier)
registrationDetails.setDataSetType(dto.DataSetType("O1"));
service.queueDataSetRegistration(io.File(incoming, "sub_data_set_1"), registrationDetails)

# Register data set 2
registrationDetails = factory.createRegistrationDetails()
dataSetInformation = registrationDetails.getDataSetInformation()
dataSetInformation.setExperimentIdentifier(identifier)
registrationDetails.setDataSetType(dto.DataSetType("O1"));
service.queueDataSetRegistration(io.File(incoming, "sub_data_set_2"), registrationDetails)
