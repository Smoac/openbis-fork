
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
    imarray = numpy.random.rand(int(height/8),int(width/8),3) * 255
    im = Image.fromarray(imarray.astype('uint8')).convert('RGBA')
    im = im.resize((height,width), resample=Image.BOX)
    img_byte_arr = io.BytesIO()
    im.save(img_byte_arr, format=format)
    img_byte_arr = img_byte_arr.getvalue()
    encoded = base64.b64encode(img_byte_arr)
    preview = {'bytes': encoded.decode('utf-8'), 'width': int(width), 'height': int(height)}
    print(f'{json.dumps(preview)}')
    return encoded


params = preview_config
print(params)
x = float(params['X-axis'][0])
y = float(params['Y-axis'][0])
if x < 8 or x > 640:
    x = 640
if y < 8 or y > 640:
    y = 640
generate_random_image(int(x), int(y))
