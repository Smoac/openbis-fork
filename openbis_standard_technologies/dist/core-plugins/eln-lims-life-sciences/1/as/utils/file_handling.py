import os
import sys

TYPES_FOLDER = "%s/life-sciences-types/" % [p for p in sys.path if p.find('core-plugins') >= 0][0]
SCRIPTS = os.path.join(TYPES_FOLDER + 'scripts')


def get_script(script_path):
    assert './' not in script_path
    script_path = os.path.join(SCRIPTS, script_path)
    return script_path


def get_filename_from_path(path):
    return os.path.splitext(os.path.basename(path))[0]


def list_xls_files():
    for f in os.listdir(TYPES_FOLDER):
        if f.endswith('.xls') or f.endswith('.xlsx'):
            yield os.path.join(TYPES_FOLDER, f)
