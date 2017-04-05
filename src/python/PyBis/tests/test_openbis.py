import json
import random

import pytest
import time
from pybis import DataSet
from pybis import Openbis


def test_token(openbis_instance):
    assert openbis_instance.hostname is not None
    new_instance = Openbis(openbis_instance.url, verify_certificates=openbis_instance.verify_certificates)
    new_instance.login('admin', 'any_test_password')
    assert new_instance.token is not None
    assert new_instance.is_token_valid() is True
    new_instance.logout()
    assert new_instance.is_token_valid() is False

    invalid_connection = Openbis(openbis_instance.url, verify_certificates=openbis_instance.verify_certificates)
    with pytest.raises(Exception):
        invalid_connection.login('invalid_username', 'invalid_password')
    assert invalid_connection.token is None
    assert invalid_connection.is_token_valid() is False


def test_create_sample(openbis_instance):
    testname = time.strftime('%a_%y%m%d_%H%M%S').upper()
    s = openbis_instance.new_sample(sample_name=testname, space_name='TEST', sample_type="UNKNOWN")
    assert s is not None
    assert s.ident == '/TEST/' + testname
    s2 = openbis_instance.get_sample(s.permid)
    assert s2 is not None


def test_cached_token(openbis_instance):
    openbis_instance.save_token()
    assert openbis_instance.token_path is not None
    assert openbis_instance._get_cached_token() is not None

    another_instance = Openbis(openbis_instance.url, verify_certificates=openbis_instance.verify_certificates)
    assert another_instance.is_token_valid() is True

    openbis_instance.delete_token()
    assert openbis_instance._get_cached_token() is None


def test_get_sample_by_id(openbis_instance):
    ident = '/TEST/TEST-SAMPLE-2-CHILD-1'
    sample = openbis_instance.get_sample(ident)
    assert sample is not None
    assert sample.ident == ident
    assert sample.permid == '20130415095823341-405'


def test_get_sample_by_permid(openbis_instance):
    response = openbis_instance.get_sample('20130415091923485-402')
    assert response is not None
    assert response.permid == '20130415091923485-402'


def test_get_sample_parents(openbis_instance):
    id = '/TEST/TEST-SAMPLE-2'
    sample = openbis_instance.get_sample(id)
    assert sample is not None
    assert sample.parents is not None
    assert sample.parents[0]['identifier']['identifier'] == '/TEST/TEST-SAMPLE-2-PARENT'
    parents = sample.get_parents()
    assert isinstance(parents, list)
    assert parents[0].ident == '/TEST/TEST-SAMPLE-2-PARENT'


def test_get_sample_children(openbis_instance):
    id = '/TEST/TEST-SAMPLE-2'
    sample = openbis_instance.get_sample(id)
    assert sample is not None
    assert sample.children is not None
    assert sample.children[0]['identifier']['identifier'] == '/TEST/TEST-SAMPLE-2-CHILD-1'
    children = sample.get_children()
    assert isinstance(children, list)
    assert children[0].ident == '/TEST/TEST-SAMPLE-2-CHILD-1'


def test_get_dataset_parents(openbis_instance):
    permid = '20130415093804724-403'
    parent_permid = '20130415100158230-407'
    dataset = openbis_instance.get_dataset(permid)
    assert dataset is not None
    parents = dataset.get_parents()
    assert isinstance(parents, list)
    assert parents[0] is not None
    assert isinstance(parents[0], DataSet)
    assert parents[0].permid == parent_permid

    children = parents[0].get_children()
    assert isinstance(children, list)
    assert children[0] is not None
    assert isinstance(children[0], DataSet)


def test_get_dataset_by_permid(openbis_instance):
    permid = '20130412142942295-198'
    permid = '20130412153118625-384'
    dataset = openbis_instance.get_dataset(permid)
    assert dataset is not None
    assert isinstance(dataset, DataSet)
    assert 'dataStore' in dataset.data
    assert 'downloadUrl' in dataset.data['dataStore']
    file_list = dataset.get_file_list(recursive=False)
    assert file_list is not None
    assert isinstance(file_list, list)
    assert len(file_list) == 1

    file_list = dataset.get_file_list(recursive=True)
    assert file_list is not None
    assert len(file_list) > 10


def test_dataset_upload(openbis_instance):
    datastores = openbis_instance.get_datastores()
    assert datastores is not None
    #    assert isinstance(datastores, list)
    # filename = 'testfile.txt'
    # with open(filename, 'w') as f:
    #    f.write('test-data')

    # ds = openbis_instance.new_dataset(
    #    name        = "My Dataset",
    #    description = "description",
    #    type        = "UNKNOWN",
    #    sample      = sample,
    #    files       = ["testfile.txt"],
    # )

    # analysis = openbis_instance.new_analysis(
    #    name = "My analysis",                       # * name of the container
    #    description = "a description",              #
    #    sample = sample,                            #   my_dataset.sample is the default

    #    # result files will be registered as JUPYTER_RESULT datatype
    #    result_files = ["my_wonderful_result.txt"], #   path of my results

    #    # jupyter notebooks file will be registered as JUPYTER_NOTEBOOk datatype
    #    notebook_files = ["notebook.ipynb"],        #   specify a specific notebook
    #    #notebook_files = "~/notebooks/",           #   path of notebooks
    #    parents = [parent_dataset],                 # other parents are optional, my_dataset is the default parent
    # )

    # analysis.save     # start registering process


def create_external_data_management_system(openbis_instance):
    code = "TEST-GIT-{:04d}".format(random.randint(0, 9999))
    result = openbis_instance.create_external_data_management_system(code, 'Test git', 'localhost:~openbis/repo')
    return code, result


def test_create_external_data_management_system(openbis_instance):
    code, result = create_external_data_management_system(openbis_instance)
    assert result is not None
    assert result.code == code
    assert result.label == 'Test git'
    assert result.addressType == 'FILE_SYSTEM'
    assert result.address == 'localhost:~openbis/repo'


def test_new_git_data_set(openbis_instance):
    dms_code, dms = create_external_data_management_system(openbis_instance)
    result = openbis_instance.new_git_data_set("GIT_REPO", "./", '12345', dms_code, "/DEFAULT/DEFAULT")
    assert result is not None
    openbis_instance.delete_entity('DataSet', result.code, 'Testing.', capitalize=False)
    # TODO Delete the externaldms (deleteExternalDataManagementSystems)
    # see http://svnsis.ethz.ch/doc/openbis/S250.0/ch/ethz/sis/openbis/generic/asapi/v3/IApplicationServerApi.html


def test_new_git_data_set_with_code(openbis_instance):
    dms_code, dms = create_external_data_management_system(openbis_instance)
    data_set_code = openbis_instance.create_perm_id()
    result = openbis_instance.new_git_data_set("GIT_REPO", "./", '12345', dms_code, "/DEFAULT/DEFAULT",
                                               data_set_code=data_set_code)
    assert result is not None
    assert result.code == data_set_code
    openbis_instance.delete_entity('DataSet', result.code, 'Testing.', capitalize=False)


def test_new_git_data_set_with_parent(openbis_instance):
    dms_code, dms = create_external_data_management_system(openbis_instance)
    result = openbis_instance.new_git_data_set("GIT_REPO", "./", '12345', dms_code, "/DEFAULT/DEFAULT")
    assert result is not None
    parent_code = result.code
    result = openbis_instance.new_git_data_set("GIT_REPO", "./", '23456', dms_code, "/DEFAULT/DEFAULT",
                                               parents=parent_code)
    assert result.code != parent_code
    assert len(result.parents) == 1
    assert result.parents[0] == parent_code
    openbis_instance.delete_entity('DataSet', parent_code, 'Testing.', capitalize=False)
    openbis_instance.delete_entity('DataSet', result.code, 'Testing.', capitalize=False)


def test_new_git_data_set_with_property(openbis_instance):
    dms_code, dms = create_external_data_management_system(openbis_instance)
    data_set_code = openbis_instance.create_perm_id()
    result = openbis_instance.new_git_data_set("GIT_REPO", "./", '12345', dms_code, "/DEFAULT/DEFAULT",
                                               data_set_code=data_set_code,
                                               properties={"DESCRIPTION": 'This is a description'})
    assert result is not None
    assert result.code == data_set_code
    openbis_instance.delete_entity('DataSet', result.code, 'Testing.', capitalize=False)


def test_create_perm_id(openbis_instance):
    perm_id = openbis_instance.create_perm_id()
    assert perm_id is not None
