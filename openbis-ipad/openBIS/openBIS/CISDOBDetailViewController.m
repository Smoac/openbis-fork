/*
 * Copyright 2012 ETH Zuerich, CISD
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
 //
//  CISDOBDetailViewController.m
//  openBIS
//
//  Created by Ramakrishnan  Chandrasekhar on 10/3/12.
//  Copyright (c) 2012 ETHZ, CISD. All rights reserved.
//

#import "CISDOBDetailViewController.h"
#import "CISDOBIpadEntity.h"
#import "CISDOBOpenBisModel.h"
#import "CISDOBIpadServiceManager.h"

@interface NSURLRequest (NSURLRequestDebug)
+ (BOOL)allowsAnyHTTPSCertificateForHost:(NSString *)host;
+ (void)setAllowsAnyHTTPSCertificate:(BOOL)allow forHost:(NSString *)host;
@end

@interface CISDOBDetailViewController ()
@property (strong, nonatomic) UIPopoverController *masterPopoverController;
@property (readonly) CISDOBIpadEntity *detailItem;

- (void)requestServerSync;
- (void)configureViewProvisionally;
- (void)configureView;
- (void)configureCell:(UITableViewCell *)cell atIndexPath:(NSIndexPath *)indexPath;
@end

@implementation CISDOBDetailViewController

#pragma mark - Managing the detail item
- (void)requestServerSync
{
    // Ask the server to synchronize the detail object and nofiy me when the complete data is available
    SuccessBlock success = ^(id result) { [self configureView]; };
    [self.openBisModel syncSelectedObjectForDetailOnSuccess: success];
}

- (void)selectionDidChange
{
    // Update the view.
   [self selectionIsChanging];
   
    if (self.masterPopoverController != nil) {
        [self.masterPopoverController dismissPopoverAnimated:YES];
    }
}

- (void)selectionIsChanging
{
    // Update the view, but do not dissmiss the popover
    [self configureViewProvisionally];
    [self requestServerSync];
}

- (CISDOBIpadEntity *)detailItem { return [self.openBisModel selectedObject]; }

- (void)configureViewProvisionally
{
    // We have a detail item which might not be up-to-date. Update the user interface. 
    [self configureView];
}

- (void)configureView
{
    // The detail item is now up-to-date. Update the user interface.
    if (!self.detailItem) return;

    self.title = self.detailItem.summaryHeader;
    self.summaryHeaderLabel.text = self.detailItem.summaryHeader;
    self.summaryLabel.text = self.detailItem.summary;
    self.identifierLabel.text = self.detailItem.identifier;
    
    NSURL *url = (self.detailItem.imageUrlString) ?
        [self.openBisModel urlFromUrlString: self.detailItem.imageUrlString] :
        [NSURL URLWithString: @"about:blank"];

    // No need to fiddle with the web view if the URL is the same
    BOOL updateWebView = ![self.webView.request.URL isEqual: url];
    if (updateWebView) {
        NSURLRequest *request = [NSURLRequest requestWithURL: url];
        [self.webView loadRequest: request];
        if (self.detailItem.imageUrlString) {
            self.webView.hidden = NO;
            self.webView.scrollView.hidden = NO;
        } else {
            self.webView.hidden = NO;
            self.webView.scrollView.hidden = YES;
        }
    }
    
    [self.propertiesTableView reloadData];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    [self configureViewProvisionally];
    [self registerForNotifications];
    [self.webView setDelegate: self];
    
    // DEBUG
    [NSURLRequest setAllowsAnyHTTPSCertificate: YES forHost: @"localhost"];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Split view

- (void)splitViewController:(UISplitViewController *)splitController willHideViewController:(UIViewController *)viewController withBarButtonItem:(UIBarButtonItem *)barButtonItem forPopoverController:(UIPopoverController *)popoverController
{
    barButtonItem.title = NSLocalizedString(@"Entity", @"Entity");
    [self.navigationItem setLeftBarButtonItem:barButtonItem animated:YES];
    self.masterPopoverController = popoverController;
}

- (void)splitViewController:(UISplitViewController *)splitController willShowViewController:(UIViewController *)viewController invalidatingBarButtonItem:(UIBarButtonItem *)barButtonItem
{
    // Called when the view is shown again in the split view, invalidating the button and popover controller.
    [self.navigationItem setLeftBarButtonItem:nil animated:YES];
    self.masterPopoverController = nil;
}

#pragma mark - Table View (Properties)

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (!self.detailItem) return 0;
    
    return [self.detailItem.properties count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{

    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Property" forIndexPath:indexPath]; //  This only works in iOS 6,
//    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell"]; // iOS5
    [self configureCell:cell atIndexPath:indexPath];
    return cell;
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    return NO;
}

- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    return NO;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{

}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{

}

- (void)configureCell:(UITableViewCell *)cell atIndexPath:(NSIndexPath *)indexPath
{
    if (!self.detailItem) return;
    NSDictionary *object = [self.detailItem.properties objectAtIndex: [indexPath indexAtPosition: 1]];
    cell.textLabel.text = [object valueForKey:@"key"];
    cell.detailTextLabel.text = [object valueForKey:@"value"];
}

#pragma mark - Status Updates
- (void)registerForNotifications
{    
    NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
    [notificationCenter
        addObserverForName: nil
        object: self.openBisModel.serviceManager
        queue: [NSOperationQueue mainQueue]
        usingBlock: ^(NSNotification *note) {
            [self processNotification: note];
        }];
}

- (void)processNotification:(NSNotification *)note
{
    if ([CISDOBIpadServiceWillLoginNotification isEqualToString: [note name]]) {
        [self setStatusText: @"Logging in..."];
    } else if ([CISDOBIpadServiceDidLoginNotification isEqualToString: [note name]]) {
        [self clearStatusText];
    } else if ([CISDOBIpadServiceWillRetrieveRootLevelEntitiesNotification isEqualToString: [note name]]) {
        [self setStatusText: @"Retrieving root entities..."];
    } else if ([CISDOBIpadServiceDidRetrieveRootLevelEntitiesNotification isEqualToString: [note name]]) {
        [self clearStatusText];
    } else if ([CISDOBIpadServiceWillSynchEntitiesNotification isEqualToString: [note name]]) {
        [self setStatusText: @"Synching entities with cache..."];
    } else if ([CISDOBIpadServiceDidSynchEntitiesNotification isEqualToString: [note name]]) {
        [self clearStatusText];
    } else if ([CISDOBIpadServiceWillDrillOnEntityNotification isEqualToString: [note name]]) {
        [self setStatusText: @"Retrieving drill information..."];
    } else if ([CISDOBIpadServiceDidDrillOnEntityNotification isEqualToString: [note name]]) {
        [self clearStatusText];
    } else if ([CISDOBIpadServiceWillRetrieveDetailsForEntityNotification isEqualToString: [note name]]) {
        [self setStatusText: @"Retrieving detail information..."];
    } else if ([CISDOBIpadServiceDidRetrieveDetailsForEntityNotification isEqualToString: [note name]]) {
        [self clearStatusText];
    }    
}

- (void)setStatusText:(NSString *)text
{
    // TODO Keep a FIFO of status updates and apply them at a maxiumum rate.
    self.statusLabel.text = text;
}

- (void)clearStatusText
{
    self.statusLabel.text = @"";
}

#pragma - UIWebViewDelegate
- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
    NSLog(@"Load failed %@", error);
}


@end


