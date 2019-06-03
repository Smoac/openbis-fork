from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SampleIdentifier
import os

VocabularyTermDefinitionToCreationType = "VT"


def is_internal_namespace(property_value):
    return property_value and property_value.startswith(u'$')


def get_script_name_for(owner_code, script_path):
    return owner_code + '.' + get_filename_from_path(script_path)


def create_sample_identifier_string(sample_creation):
    spaceId = str(sample_creation.spaceId) if sample_creation.spaceId is not None else None
    projectId = str(sample_creation.projectId) if sample_creation.projectId is not None else None
    code = sample_creation.code
    sample_identifier = SampleIdentifier(spaceId, projectId, None, code)
    return sample_identifier.identifier


def get_filename_from_path(path):
    return os.path.splitext(os.path.basename(path))[0]


def get_version_vocabulary_name_for(name):
    return '${}_VERSION'.format(name)


def get_metadata_vocabulary_name_for(creation_type, creation):
    if creation_type == VocabularyTermDefinitionToCreationType:
        code = "{}_{}_{}".format(creation_type, str(creation.vocabularyId), creation.code)
    else:
        code = "{}_{}".format(creation_type, creation.code)
    code = code.replace("$", ".")
    code = code.upper()
    return code
