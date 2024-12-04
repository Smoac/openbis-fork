
import numpy
from PIL import Image, ImageDraw
import io
import base64
import json
import os
import sys
import datetime

file = sys.argv[1]
format = sys.argv[2]
image_config = json.loads(sys.argv[3])
image_metadata = json.loads(sys.argv[4])
preview_config = json.loads(sys.argv[5])
preview_metadata = json.loads(sys.argv[6])



folder_dir = os.path.join(file, 'original')
file_path = os.path.join(folder_dir, os.listdir(folder_dir)[0])

def generate_random_image(height, width):
    imarray = numpy.random.rand(int(height/64),int(width/64),3) * 255
    im = Image.fromarray(imarray.astype('uint8')).convert('RGBA')
    im = im.resize((height,width), resample=Image.BOX)
    draw = ImageDraw.Draw(im)
    draw.text((5,5), sys.version, fill='white')
    draw.text((5, 20), sys.executable, fill='white')
    img_byte_arr = io.BytesIO()
    im.save(img_byte_arr, format=format)
    img_byte_arr = img_byte_arr.getvalue()
    encoded = base64.b64encode(img_byte_arr)
    preview = {'bytes': encoded.decode('utf-8'), 'width': int(width), 'height': int(height)}
    preview['python version'] = sys.version
    preview['python path'] = sys.executable
    now = datetime.datetime.now().strftime("%c")
    preview['preview creation time'] = now
    preview['comment'] = 'some preview comment from preview creation script: %s' % now
    print(f'{json.dumps(preview)}')
    return preview


params = preview_config
print(params)
x = float(params['X-axis'][0])
y = float(params['Y-axis'][0])
if x < 64 or x > 640:
    x = 640
if y < 64 or y > 640:
    y = 640
generate_random_image(int(x), int(y))
