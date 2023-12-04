#   Copyright ETH 2023 Zürich, Scientific IT Services
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
# Hacky way to import imaging script
import sys
import os
parent_dir = os.path.dirname(os.path.realpath(__file__))
sys.path.append(parent_dir)
import imaging.imaging as imaging

from pybis import Openbis
import json
import numpy as np

from spmpy_terry import spm
import spmpy_terry as spmpy
from datetime import datetime

SXM_ADAPTOR = "ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.adaptor.NanonisSxmAdaptor"
DAT_ADAPTOR = "ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.adaptor.NanonisDatAdaptor"
VERBOSE = True

def get_instance(url=None):
    base_url = "http://localhost:8888/openbis"
    # base_url = "https://alaskowski:8443/openbis"
    # base_url = "https://openbis-sis-ci-sprint.ethz.ch/"
    if url == None:
        url = base_url
    openbis_instance = Openbis(
        url=base_url,
        verify_certificates=False,
        allow_http_but_do_not_use_this_in_production_and_only_within_safe_networks=True
    )
    token = openbis_instance.login('admin', 'changeit')
    print(f'Connected to {base_url} -> token: {token}')
    return openbis_instance


def get_color_scale_range(img, channel):
    minimum = str(np.min(img.get_channel(channel)[0]))
    maximum = str(np.max(img.get_channel(channel)[0]))
    step = str(0.01)
    return [minimum, maximum, step]


def create_sxm_dataset(openbis, experiment, file_path, sample=None):
    img = spm(file_path)
    channels = [x['ChannelNickname'] for x in img.SignalsList]

    imaging_control = imaging.ImagingControl(openbis)

    color_scale_visibility = [
        imaging.ImagingDataSetControlVisibility(
            "Channel",
            [channel],
            get_color_scale_range(img, channel),
            img.get_channel(channel)[1])
        for channel in channels]

    exports = [imaging.ImagingDataSetControl('include', "Dropdown", values=['image', 'raw data'], multiselect=True),
               imaging.ImagingDataSetControl('image-format', "Dropdown", values=['png', 'svg', 'csv']),
               imaging.ImagingDataSetControl('archive-format', "Dropdown", values=['zip', 'tar']),
               imaging.ImagingDataSetControl('resolution', "Dropdown", values=['original', '150dpi', '300dpi'])]

    inputs = [
        imaging.ImagingDataSetControl('Channel', "Dropdown", values=channels),
        imaging.ImagingDataSetControl('X-axis', "Range", values_range=["0", str(img.get_param('width')[0]), "0.01"]),
        imaging.ImagingDataSetControl('Y-axis', "Range", values_range=["0", str(img.get_param('height')[0]), "0.01"]),
        imaging.ImagingDataSetControl('Color-scale', "Range", visibility=color_scale_visibility),
        imaging.ImagingDataSetControl('Colormap', "Dropdown", values=['gray', 'YlOrBr', 'viridis', 'cividis', 'inferno', 'rainbow', 'Spectral', 'RdBu', 'RdGy']),
        imaging.ImagingDataSetControl('Scaling', "Dropdown", values=['linear', 'logarithmic']),
    ]

    imaging_config = imaging.ImagingDataSetConfig(
        SXM_ADAPTOR,
        1.0,
        ['original', '200x200', '2000x2000'],
        True,
        [1000, 2000, 5000],
        exports,
        inputs,
        {})

    images = [imaging.ImagingDataSetImage(previews=[imaging.ImagingDataSetPreview(preview_format="png")])]
    imaging_property_config = imaging.ImagingDataSetPropertyConfig(imaging_config, images)
    if VERBOSE:
        print(imaging_property_config.to_json())

    return imaging_control.create_imaging_dataset(
        dataset_type="IMAGING_DATA",
        config=imaging_property_config,
        experiment=experiment,
        sample=sample,
        files=[file_path])


def create_dat_dataset(openbis, folder_path, file_prefix='', sample=None, experiment=None):
    assert experiment is not None or sample is not None, "Either sample or experiment needs to be provided!"
    data = spmpy.importall(folder_path, file_prefix, 'spec')

    imaging_control = imaging.ImagingControl(openbis)

    for d in data:
        if d.type == 'scan':
            date = d.get_param('rec_date')
            time = d.get_param('rec_time')
            date_time = '%s %s' % (date, time)
            d.date_time = datetime.strptime(date_time, "%d.%m.%Y %H:%M:%S")

        if d.type == 'spec':
            date_time = d.get_param('Saved Date')
            d.date_time = datetime.strptime(date_time, "%d.%m.%Y %H:%M:%S") if date_time is not None else datetime.now()

    data.sort(key=lambda da: da.date_time)

    channels = list(set([(channel['ChannelNickname'], channel['ChannelUnit'], channel['ChannelScaling']) for spec in data for channel in spec.SignalsList]))

    color_scale_visibility = []
    for (channel, unit, scaling) in channels:
        minimum = []
        maximum = []
        for spec in data:
            minimum += [np.min(spec.get_channel(f'{channel}')[0])]
            maximum += [np.max(spec.get_channel(f'{channel}')[0])]
        minimum = np.min(minimum)
        maximum = np.max(maximum)
        step = round((maximum - minimum) / 100, 2)

        if step == 0.0:
            step = (maximum - minimum) / 100

        color_scale_visibility += [imaging.ImagingDataSetControlVisibility(
            "Channel",
            [channel],
            [minimum, maximum, step],
            unit
        )]

    exports = [imaging.ImagingDataSetControl('include', "Dropdown", values=['image', 'raw data'], multiselect=True),
               imaging.ImagingDataSetControl('image-format', "Dropdown", values=['png', 'svg', 'csv']),
               imaging.ImagingDataSetControl('archive-format', "Dropdown", values=['zip', 'tar']),
               imaging.ImagingDataSetControl('resolution', "Dropdown", values=['original', '150dpi', '300dpi'])]

    inputs = [
        imaging.ImagingDataSetControl('Channel X', "Dropdown", values=[channel[0] for channel in channels]),
        imaging.ImagingDataSetControl('Channel Y', "Dropdown", values=[channel[0] for channel in channels]),
        imaging.ImagingDataSetControl('X-axis', "Range", visibility=color_scale_visibility),
        imaging.ImagingDataSetControl('Y-axis', "Range", visibility=color_scale_visibility),
        imaging.ImagingDataSetControl('Grouping', "Dropdown", values=[d.name for d in data]),
        imaging.ImagingDataSetControl('Colormap', "Dropdown", values=['gray', 'YlOrBr', 'viridis', 'cividis', 'inferno', 'rainbow', 'Spectral', 'RdBu', 'RdGy']),
        imaging.ImagingDataSetControl('Scaling', "Dropdown", values=['lin-lin', 'lin-log', 'log-lin', 'log-log']),
    ]

    imaging_config = imaging.ImagingDataSetConfig(
        DAT_ADAPTOR,
        1.0,
        ['original', '200x200', '2000x2000'],
        True,
        [1000, 2000, 5000],
        exports,
        inputs,
        {})

    images = [imaging.ImagingDataSetImage()]
    imaging_property_config = imaging.ImagingDataSetPropertyConfig(imaging_config, images)
    if VERBOSE:
        print(imaging_property_config.to_json())

    return imaging_control.create_imaging_dataset(
        dataset_type="IMAGING_DATA",
        config=imaging_property_config,
        experiment=experiment,
        sample=sample,
        files=[d.path for d in data])


def create_preview(openbis, perm_id, config, preview_format="png", image_index=0):
    imaging_control = imaging.ImagingControl(openbis)
    preview = imaging.ImagingDataSetPreview(preview_format, config)
    preview = imaging_control.make_preview(perm_id, image_index, preview)
    return preview


def update_image_with_preview(openbis, perm_id, image_id, preview: imaging.ImagingDataSetPreview):
    imaging_control = imaging.ImagingControl(openbis)
    config = imaging_control.get_property_config(perm_id)
    image = config.images[image_id]
    if len(image.previews) > preview.index:
        image.previews[preview.index] = preview
    else:
        preview.index = len(image.previews)
        image.add_preview(preview)

    imaging_control.update_property_config(perm_id, config)


def export_image(openbis: Openbis, perm_id: str, image_id: int, path_to_download: str,
                 include=None, image_format='original', archive_format="zip", resolution='original'):
    if include is None:
        include = ['image', 'raw data']
    imaging_control = imaging.ImagingControl(openbis)
    export_config = {
        "include": include,
        "image-format": image_format,
        "archive-format": archive_format,
        "resolution": resolution
    }
    imaging_export = imaging.ImagingDataSetExport(export_config)
    imaging_control.single_export_download(perm_id, imaging_export, image_id, path_to_download)




o = get_instance()


def demo_sxm_flow(openbis):

    # dataset_sxm = create_sxm_dataset(
    #     openbis=o,
    #     experiment='/DEFAULT/DEFAULT/DEFAULT',
    #     file_path='data/img_0150.sxm')
    # print(dataset_sxm.permId)

    config_sxm_preview = {
        "channel": "z",  # usually one of these: ['z', 'I', 'dIdV', 'dIdV_Y']
        "x-axis": [0, 3.0],  # file dependent
        "y-axis": [0, 3.0],  # file dependent
        "color-scale": [-700.0, 700.0],  # file dependent
        "colormap": "YlOrBr",  # [gray, YlOrBr, viridis, cividis, inferno, rainbow, Spectral, RdBu, RdGy]
        "scaling": "linear",  # ['linear', 'logarithmic']
        # "mode": 3 # debug
    }

    config_preview = config_sxm_preview
    # perm_id = dataset_sxm.permId
    # perm_id = '20231129145334261-68'
    perm_id = '20231204133025390-73'

    preview = create_preview(o, perm_id, config_preview)
    preview.save_to_file('/home/alaskowski/PREMISE/DEMO/my_sxm_preview.png')

    # preview.index = 0
    # update_image_with_preview(o, perm_id, 0, preview)
    #
    # config_preview['colormap'] = 'inferno'
    # preview = create_preview(o, perm_id, config_preview)
    # preview.index = 1
    # update_image_with_preview(o, perm_id, 0, preview)
    #
    # config_preview['x-axis'] = [1.2, 3]
    # config_preview['y-axis'] = [1.2, 3]
    # config_preview['scaling'] = 'logarithmic'
    # preview = create_preview(o, perm_id, config_preview)
    # preview.index = 2
    # update_image_with_preview(o, perm_id, 0, preview)
    #
    # export_image(o, perm_id, 0, '/home/alaskowski/PREMISE/DEMO')


def demo_dat_flow(openbis):

    dataset_dat = create_dat_dataset(
        openbis=o,
        experiment='/DEFAULT/DEFAULT/DEFAULT',
        # sample='/DEFAULT/DEFAULT/DEFAULT',
        folder_path='data',
        file_prefix='didv_')
    print(dataset_dat.permId)

    config_dat_preview = {
        "channel x": "V",
        "channel y": "dIdV",
        "x-axis": [-2.1, 1],
        "y-axis": [0.00311e-11, 0.39334e-11],
        "grouping": ["didv_00063.dat", "didv_00064.dat", "didv_00065.dat", "didv_00066.dat",
                     "didv_00067.dat", "didv_00068.dat", "didv_00069.dat", "didv_00070.dat"],
        "colormap": "rainbow",
        "scaling": "lin-lin",  # ['lin-lin', 'lin-log', 'log-lin', 'log-log']
        # "print_legend": "false", # disable legend in image
        # "mode": 1 # debug
    }

    config_preview = config_dat_preview
    perm_id = dataset_dat.permId

    preview = create_preview(o, perm_id, config_preview)
    preview.save_to_file('/home/alaskowski/PREMISE/DEMO/my_dat_preview.png')

    preview.index = 0
    update_image_with_preview(o, perm_id, 0, preview)

    config_preview["grouping"] = ["didv_00063.dat", "didv_00064.dat", "didv_00068.dat", "didv_00070.dat"]
    preview = create_preview(o, perm_id, config_preview)
    preview.index = 1
    update_image_with_preview(o, perm_id, 0, preview)

    config_preview["print_legend"] = "false"
    preview = create_preview(o, perm_id, config_preview)
    preview.index = 2
    update_image_with_preview(o, perm_id, 0, preview)

    export_image(o, perm_id, 0, '/home/alaskowski/PREMISE/DEMO')


demo_sxm_flow(o)

# demo_dat_flow(o)

