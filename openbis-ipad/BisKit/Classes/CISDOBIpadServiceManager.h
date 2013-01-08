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
//  CISDOBIpadServiceManager.h
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 10/30/12.
//
//

#import <Foundation/Foundation.h>
#import "CISDOBShared.h"

//
// The names of the notifications posted by the service manager.
//
// The notifications include a userInfo dictionary with the following
// keys and values:
//      NSUnderlyingErrorKey If in the dictionary, the error that occured when making the call.
//
FOUNDATION_EXPORT NSString *const CISDOBIpadServiceWillLoginNotification;
FOUNDATION_EXPORT NSString *const CISDOBIpadServiceDidLoginNotification;

FOUNDATION_EXPORT NSString *const CISDOBIpadServiceWillRetrieveRootLevelEntitiesNotification;
FOUNDATION_EXPORT NSString *const CISDOBIpadServiceDidRetrieveRootLevelEntitiesNotification;

FOUNDATION_EXPORT NSString *const CISDOBIpadServiceWillDrillOnEntityNotification;
FOUNDATION_EXPORT NSString *const CISDOBIpadServiceDidDrillOnEntityNotification;

FOUNDATION_EXPORT NSString *const CISDOBIpadServiceWillRetrieveDetailsForEntityNotification;
FOUNDATION_EXPORT NSString *const CISDOBIpadServiceDidRetrieveDetailsForEntityNotification;

FOUNDATION_EXPORT NSString *const CISDOBIpadServiceWillSynchEntitiesNotification;
FOUNDATION_EXPORT NSString *const CISDOBIpadServiceDidSynchEntitiesNotification;


//
// Errors that can happen in the service manager
//
//! The error domain for errors in the service manager layer
FOUNDATION_EXPORT NSString *const CISDOBIpadServiceManagerErrorDomain;

enum CISDOBIpadServiceManagerErrorCode {
    kCISDOBIpadServiceManagerError_ImageRetrievalCouldNotConnectToServer = 1,
};

@class CISDOBIpadService, CISDOBAsyncCall, CISDOBIpadEntity;

//
// Typedefs
//
typedef void (^AuthenticationChallengeBlock)(CISDOBAsyncCall *call, NSURLAuthenticationChallenge *challange);



/**
 * \brief A class that manages a connection to the openBIS iPad service, caching data locally as CISDOBIpadEntity objects.
 */
@interface CISDOBIpadServiceManager : NSObject

@property (readonly) NSURL *openbisUrl;
@property (readonly, strong) CISDOBIpadService *service;
@property (readonly, strong) NSURL *storeUrl;
@property (readonly, strong) NSManagedObjectContext *managedObjectContext;
@property (readonly, strong) NSManagedObjectModel *managedObjectModel;
@property (readonly, strong) NSPersistentStoreCoordinator *persistentStoreCoordinator;
@property (readonly, strong) NSEntityDescription *ipadEntityDescription;
@property (readonly, strong) NSOperationQueue *queue;
@property (readonly) NSString *sessionToken;
@property (copy, nonatomic) AuthenticationChallengeBlock authenticationChallengeBlock;


// Initialization
- (id)initWithStoreUrl:(NSURL *)storeUrl openbisUrl:(NSURL *)openbisUrl trusted:(BOOL)trusted error:(NSError **)error;

// Properties
- (void)setOpenbisUrl:(NSURL *)openbisUrl trusted:(BOOL)trusted;

// Actions

//! Log the user into the openBIS instance
- (CISDOBAsyncCall *)loginUser:(NSString *)user password:(NSString *)password;

//! Get all root-level entities from the openBIS ipad service and store the results in the managedObjectContext.
- (CISDOBAsyncCall *)retrieveRootLevelEntities;

//! Get drill information from the openBIS ipad service and store the results in the managedObjectContext.
- (CISDOBAsyncCall *)drillOnEntity:(CISDOBIpadEntity *)entity;

//! Get detail information from the openBIS ipad service and store the results in the managedObjectContext.
- (CISDOBAsyncCall *)detailsForEntity:(CISDOBIpadEntity *)entity;

//! Get images for the entity, if there are any. The success block will be called with a CISDOBIpadImage object that describes the location of the image and contains the bytes for the image.
- (CISDOBAsyncCall *)imagesForEntity:(CISDOBIpadEntity *)entity;

// Local Actions -- actions that do not require a network connection
- (NSArray *)allIpadEntitiesOrError:(NSError **)error;
- (NSArray *)entitiesByPermId:(NSArray *)permIds error:(NSError **)error;
- (NSArray *)entitiesNotUpdatedSince:(NSDate *)date error:(NSError **)error;

- (NSFetchRequest *)fetchRequestForEntities;
- (NSFetchRequest *)fetchRequestForEntitiesByPermId:(NSArray *)permIds;
- (NSFetchRequest *)fetchRequestForEntitiesNotUpdatedSince:(NSDate *)date;

- (NSArray *)executeFetchRequest:(NSFetchRequest *)fetchRequest error:(NSError **)error;

@end

@interface CISDOBIpadServiceManager (CISDOBAsyncCallDelegate)

- (void)asyncCall:(CISDOBAsyncCall *)call didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)authenticationChallenge;

@end

/**
 * \brief An abstraction for an image to display in openBIS.
 */
@interface CISDOBIpadImage : NSObject

@property(strong, readonly) NSData *imageData;

- (id)initWithImageData:(NSData *)imageData;

@end
