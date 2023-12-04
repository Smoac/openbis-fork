import numpy
from PIL import Image
import io
import base64
import sys
import json
import os
import sys
from spmpy_terry import spm   # <--- class spm defines objects of type spm with their attributes and class functions
import spmpy_terry as spmpy   # <--- spmpy has other methods
from datetime import datetime

import matplotlib.pyplot as plt
# %matplotlib inline


def load_image(path):
    return spm(path)


def get_lock_in(img):
    param_name = 'lock-in>lock-in status'
    param = img.get_param(param_name)
    return param


def get_channel(img, channel_name = 'z'):
    # channel_name = 'z'
    channel = img.get_channel(channel_name)
    return channel


file = sys.argv[1]
format = sys.argv[2]
image_config = json.loads(sys.argv[3])
image_metadata = json.loads(sys.argv[4])
preview_config = json.loads(sys.argv[5])
preview_metadata = json.loads(sys.argv[6])



folder_dir = os.path.join(file, 'original')
file_path = os.path.join(folder_dir, os.listdir(folder_dir)[0])
# print(file_path)


def generate_random_image(height, width):
    imarray = numpy.random.rand(height,width,3) * 255
    im = Image.fromarray(imarray.astype('uint8')).convert('RGBA')
    img_byte_arr = io.BytesIO()
    im.save(img_byte_arr, format=format)
    img_byte_arr = img_byte_arr.getvalue()
    encoded = base64.b64encode(img_byte_arr)
    preview = {'bytes': encoded.decode('utf-8'), 'width': int(width), 'height': int(height)}
    print(f'{json.dumps(preview)}')
    return encoded


def get_dat_image(channel_x, channel_y, x_axis, y_axis, colormap, scaling, grouping, print_legend, resolution):
    specs = spmpy.importall(folder_dir, '', 'spec')

    for spec in specs:
        date_time = spec.get_param('Saved Date')
        spec.date_time = datetime.strptime(date_time, "%d.%m.%Y %H:%M:%S")

    # sort measurements according to date
    specs.sort(key=lambda d: d.date_time)
    specs_sub = list(filter(lambda spec:spec.name in grouping, specs))

    print_legend = print_legend
    show = False
    fig = spmpy.specs_plot(specs_sub, channelx=channel_x, channely=channel_y, direction='forward',
                           print_legend=print_legend, show=show, colormap=colormap, scaling=scaling,
                           x_axis=x_axis, y_axis=y_axis)
    img_byte_arr = io.BytesIO()
    plt.savefig(img_byte_arr, format=format, dpi=resolution)

    fig = plt.figure()
    size = fig.get_size_inches()*fig.dpi

    img_byte_arr = img_byte_arr.getvalue()
    encoded = base64.b64encode(img_byte_arr)
    return size[0], size[1], encoded


def dat_mode(parameters):
    input_config = dict(
        channel_x=parameters['channel x'],
        channel_y=parameters['channel y'],
        x_axis=[float(x) for x in parameters['x-axis']],
        y_axis=[float(x) for x in parameters['y-axis']],
        colormap=parameters['colormap'],
        scaling=parameters['scaling'],
        grouping=parameters['grouping']
    )
    if "color" in parameters:
        input_config['color'] = parameters['color']
    if "print_legend" in parameters:
        input_config['print_legend'] = parameters['print_legend'].upper() == "TRUE"
    else:
        input_config['print_legend'] = True

    resolution = 'figure'
    if "resolution" in parameters:
        resolution = parameters['resolution'].upper()
        if resolution == "ORIGINAL":
            resolution = 'figure'
        elif resolution.endswith('DPI'):
            resolution = float(resolution[:-3])
        else:
            resolution = float(resolution)
    input_config['resolution'] = resolution


    width, height, image_bytes = get_dat_image(**input_config)
    preview = {'bytes': image_bytes.decode('utf-8'), 'width': int(width), 'height': int(height)}
    print(f'{json.dumps(preview)}')

params = preview_config
print(params)
if 'mode' in params:
    if params['mode'] == '1':
        generate_random_image(640, 640)
    elif params['mode'] == '2':
        dat_mode(preview_config)

else:
    dat_mode(preview_config)
