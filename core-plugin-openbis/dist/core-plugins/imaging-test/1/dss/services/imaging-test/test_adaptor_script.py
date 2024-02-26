
import numpy
from PIL import Image
import io
import base64
import json
import os
import sys

file = sys.argv[1]
format = sys.argv[2]
image_config = json.loads(sys.argv[3])
image_metadata = json.loads(sys.argv[4])
preview_config = json.loads(sys.argv[5])
preview_metadata = json.loads(sys.argv[6])



folder_dir = os.path.join(file, 'original')
file_path = os.path.join(folder_dir, os.listdir(folder_dir)[0])

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


params = preview_config
print(params)
generate_random_image(640, 640)