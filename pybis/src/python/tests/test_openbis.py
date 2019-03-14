import json
import random
import re

import pytest
import time
from pybis import DataSet
from pybis import Openbis


def test_token(openbis_instance):
    assert openbis_instance.token is not None
    assert openbis_instance.is_token_valid(openbis_instance.token) is True
    assert openbis_instance.is_session_active() is True


def test_http_only(openbis_instance):
    with pytest.raises(Exception):
        new_instance = Openbis('http://localhost')
        assert new_instance is None

    new_instance = Openbis(url='http://localhost', allow_http_but_do_not_use_this_in_production_and_only_within_safe_networks=True)
    assert new_instance is not None


def test_cached_token(openbis_instance):
    openbis_instance.save_token()
    assert openbis_instance.token_path is not None
    assert openbis_instance._get_cached_token() is not None

    another_instance = Openbis(openbis_instance.url, verify_certificates=openbis_instance.verify_certificates)
    assert another_instance.is_token_valid() is True

    openbis_instance.delete_token()
    assert openbis_instance._get_cached_token() is None


def test_create_permId(openbis_instance):
    permId = openbis_instance.create_permId()
    assert permId is not None
    m = re.search('([0-9]){17}-([0-9]*)', permId)
    ts = m.group(0)
    assert ts is not None
    count = m.group(1)
    assert count is not None
