//
//  CISDOBSplitViewController.m
//  openBIS
//
//  Created by Fuentes Serna  Juan Mariano on 7/24/13.
//  Copyright (c) 2013 ETHZ, CISD. All rights reserved.
//

#import "CISDOBSplitViewController.h"

@interface CISDOBSplitViewController ()

@end

@implementation CISDOBSplitViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(switchAutoRotate:)
     name:@"BarcodeReaderSwitchAutoRotation"
     object:nil];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

static BOOL shouldAutorotate = YES;

- (void) switchAutoRotate:(NSNotification *) notification
{
    shouldAutorotate = !shouldAutorotate;
}

- (BOOL)shouldAutorotate
{
    return shouldAutorotate;
}

@end
