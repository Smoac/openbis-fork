/*
 * Copyright 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import <AudioToolbox/AudioToolbox.h>
#import <AVFoundation/AVFoundation.h>
#import "CISDOBBarcodeViewController.h"

@interface CISDOBBarcodeViewController ()

@property (nonatomic, retain) ZXCapture* capture;
/*
@property (nonatomic, retain) AVCaptureSession* cameraSession;
@property (nonatomic, retain) AVCaptureVideoPreviewLayer* previewLayer;
@property (nonatomic, retain) AVCaptureDevice *videoDevice;
@property (nonatomic, retain) AVCaptureDeviceInput *videoIn;
*/
@property (nonatomic, retain) IBOutlet UILabel* decodedLabel;
@property (nonatomic, retain) IBOutlet UIView* cameraView;
@property (nonatomic, retain) IBOutlet UIBarButtonItem* backButton;


- (NSString*)displayForResult:(ZXResult*)result;

@end

@implementation CISDOBBarcodeViewController

@synthesize capture;
@synthesize decodedLabel;
@synthesize cameraView;

#pragma mark - Creation/Deletion Methods

- (IBAction) dismissModalViewController {
    [self dismissViewControllerAnimated:YES completion:nil];
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"DissmissNotification"
     object:nil];
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"BarcodeReaderSwitchAutoRotation"
     object:nil];
}

#pragma mark - Creation/Deletion Methods
- (void)dealloc {
    [self.capture.layer removeFromSuperlayer];
    [self.capture stop];

    
    [self.capture release];
    [self.decodedLabel release];
    [self.cameraView release];
    [self.backButton release];
    
    [super dealloc];
}
#pragma mark - View Controller Methods

- (void)viewWillAppear:(BOOL)animated {
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"BarcodeReaderSwitchAutoRotation"
     object:nil];
    /*
    self.videoDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    NSError *error;
    self.videoIn = [AVCaptureDeviceInput deviceInputWithDevice:self.videoDevice error:&error];
    self.cameraSession = [[AVCaptureSession alloc] init];
    [self.cameraSession addInput:self.videoIn];
    self.previewLayer = [[AVCaptureVideoPreviewLayer alloc] initWithSession:self.cameraSession];
    self.previewLayer.frame = CGRectMake(0.f, 0.f, 360.f, 360.f);
    [self.previewLayer setVideoGravity:AVLayerVideoGravityResizeAspectFill];
    
    [self.cameraView.layer addSublayer: self.previewLayer];
    [self.cameraSession startRunning];
    */
    
    [NSThread detachNewThreadSelector:@selector(loadBarcodeReader) toTarget:self withObject:nil];
    [super viewWillAppear:animated];
}

- (void)loadBarcodeReader {
    self.backButton.enabled = NO;
    self.capture = [[ZXCapture alloc] init];
    self.capture.delegate = self;
    self.capture.rotation = 90.0f;
    
    // Use the back camera
    // self.capture.camera = self.capture.back;
    self.capture.layer.frame = CGRectMake(0.f, 0.f, 768.f, 960.f);
    
    [self.cameraView.layer addSublayer: self.capture.layer];
    
    [self.cameraView bringSubviewToFront:self.decodedLabel];
    
    UIImageView *overlayImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"overlaygraphic.png"]];
    [overlayImageView setFrame:CGRectMake((768/2 - 180), 100, 360, 277)];
    [self.cameraView addSubview:overlayImageView];
    [overlayImageView release];
    self.backButton.enabled = YES;
    //[self.cameraSession stopRunning];
    //[self.previewLayer removeFromSuperlayer];
}


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
	return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (NSUInteger)supportedInterfaceOrientations{
    return UIInterfaceOrientationMaskPortrait;
}

#pragma mark - Private Methods

- (NSString*)displayForResult:(ZXResult*)result {
    [self dismissModalViewController];
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"SearchNotification"
     object:result.text];
    
    return result.text;
}

- (NSString*)displayForResultDemo:(ZXResult*)result {
    
    NSString *formatString;
    switch (result.barcodeFormat) {
        case kBarcodeFormatAztec:
            formatString = @"Aztec";
            break;
            
        case kBarcodeFormatCodabar:
            formatString = @"CODABAR";
            break;
            
        case kBarcodeFormatCode39:
            formatString = @"Code 39";
            break;
            
        case kBarcodeFormatCode93:
            formatString = @"Code 93";
            break;
            
        case kBarcodeFormatCode128:
            formatString = @"Code 128";
            break;
            
        case kBarcodeFormatDataMatrix:
            formatString = @"Data Matrix";
            break;
            
        case kBarcodeFormatEan8:
            formatString = @"EAN-8";
            break;
            
        case kBarcodeFormatEan13:
            formatString = @"EAN-13";
            break;
            
        case kBarcodeFormatITF:
            formatString = @"ITF";
            break;
            
        case kBarcodeFormatPDF417:
            formatString = @"PDF417";
            break;
            
        case kBarcodeFormatQRCode:
            formatString = @"QR Code";
            break;
            
        case kBarcodeFormatRSS14:
            formatString = @"RSS 14";
            break;
            
        case kBarcodeFormatRSSExpanded:
            formatString = @"RSS Expanded";
            break;
            
        case kBarcodeFormatUPCA:
            formatString = @"UPCA";
            break;
            
        case kBarcodeFormatUPCE:
            formatString = @"UPCE";
            break;
            
        case kBarcodeFormatUPCEANExtension:
            formatString = @"UPC/EAN extension";
            break;
            
        default:
            formatString = @"Unknown";
            break;
    }
    
    NSString *resultText = [NSString stringWithFormat:@"Scanned!\nFormat: %@\nContents:\n%@", formatString, result.text];

    return resultText;
}

#pragma mark - ZXCaptureDelegate Methods

- (void)captureResult:(ZXCapture*)capture result:(ZXResult*)result {
    if (result) {
        // We got a result. Display information about the result onscreen.
        [self.decodedLabel performSelectorOnMainThread:@selector(setText:) withObject:[self displayForResult:result] waitUntilDone:YES];
    }
}

- (void)captureSize:(ZXCapture*)capture width:(NSNumber*)width height:(NSNumber*)height {
}

@end
