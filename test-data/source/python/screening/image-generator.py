#!/usr/bin/python

# image-generator.py
#
# Create bitmap images in python
#

from Quartz import *
from Cocoa import *
from LaunchServices import * # for kUTTypePNG
import math

cs = CGColorSpaceCreateDeviceRGB()

def write_to_file(ctx, filename):
  image = CGBitmapContextCreateImage(ctx)
  fileUrl = NSURL.fileURLWithPath_(filename)
  dest = CGImageDestinationCreateWithURL(fileUrl, kUTTypePNG, 1, None);
  CGImageDestinationAddImage(dest, image, None);
  CGImageDestinationFinalize(dest);
  

def createBitmap(text, filename):
  pixelsWide = 256
  pixelsHigh = 256
  bitmapBytesPerRow   = (pixelsWide * 4);
  bitmapByteCount     = (bitmapBytesPerRow * pixelsHigh);  

  # Create an RGB bitmap context, transparent black background, 256x256
  ctx = CGBitmapContextCreate(None, pixelsWide, pixelsHigh, 8, bitmapBytesPerRow, cs, kCGImageAlphaPremultipliedLast)

  # Draw a yellow square with a red outline in the center

  with CGSavedGState(ctx):
    CGContextSetRGBStrokeColor(ctx, 0, 0, 0, 1) # black
    CGContextSetRGBFillColor(ctx, 0, 0, 0, 1) # black
    CGContextAddRect(ctx, CGRectMake(32.5, 32.5, 191, 191))
    CGContextDrawPath(ctx, kCGPathFillStroke)
    
  with CGSavedGState(ctx):
    CGContextSetRGBStrokeColor(ctx, 0, 0, 0, 1) # black
    CGContextSetRGBFillColor(ctx, 1, 1, 1, 1) # white
    CGContextSetTextMatrix(ctx, CGAffineTransformIdentity)
    CGContextSelectFont(ctx, "Helvetica Neue", 36, kCGEncodingMacRoman)
    CGContextSetTextDrawingMode(ctx, kCGTextFillStroke)
    CGContextShowTextAtPoint(ctx, 40, 118, text, len(text))

  # Draw some text at an angle (or not)
  # c.saveGState()
  # c.setRGBStrokeColor(0,0,0,1)
  # c.setRGBFillColor(1,1,1,1)
  # c.selectFont("Helvetica", 36, kCGEncodingMacRoman)
  # c.setTextPosition(40, 118)
  # c.setTextDrawingMode(kCGTextFillStroke)
  # c.showText(text, len(text))
  # c.restoreGState()

  # Write the bitmap to disk in PNG format

  write_to_file(ctx, filename)

createBitmap("hi!", "out.png")