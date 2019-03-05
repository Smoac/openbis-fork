from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SampleIdentifier
import os


def is_internal_namespace(property_value):
    return property_value and property_value.startswith(u'$')


def get_script_name_for(owner_code, script_path):
    return owner_code + '.' + get_filename_from_path(script_path)


def create_sample_identifier_string(sample_creation):
    spaceId = sample_creation.spaceId.creationId if sample_creation.spaceId is not None else None
    projectId = sample_creation.projectId.creationId if sample_creation.projectId is not None else None
    code = sample_creation.code
    sample_identifier = SampleIdentifier(spaceId, projectId, None, code)
    return sample_identifier.identifier


def get_filename_from_path(path):
        return os.path.splitext(os.path.basename(path))[0]
