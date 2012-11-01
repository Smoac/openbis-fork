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

@class CISDOBIpadService, CISDOBAsyncCall, CISDOBIpadEntity;
/**
 * \brief A class that manages a connection to the openBIS iPad service, caching data locally as CISDOBIpadEntity objects.
 */
@interface CISDOBIpadServiceManager : NSObject

@property (readonly, strong) CISDOBIpadService *service;
@property (readonly, strong) NSURL *storeUrl;
@property (readonly, strong) NSManagedObjectContext *managedObjectContext;
@property (readonly, strong) NSManagedObjectModel *managedObjectModel;
@property (readonly, strong) NSPersistentStoreCoordinator *persistentStoreCoordinator;
@property (readonly, strong) NSEntityDescription *ipadEntityDescription;
@property (readonly, strong) NSOperationQueue *queue;


// Initialization
- (id)initWithStoreUrl:(NSURL *)storeUrl openbisUrl:(NSURL *)openbisUrl trusted:(BOOL)trusted error:(NSError **)error;

// Actions

//! Log the user into the openBIS instance
- (CISDOBAsyncCall *)loginUser:(NSString *)user password:(NSString *)password;

//! Get all root-level entities from the openBIS ipad service and store the results in the managedObjectContext.
- (CISDOBAsyncCall *)retrieveRootLevelEntities;

//! Get drill information from the openBIS ipad service and store the results in the managedObjectContext.
- (CISDOBAsyncCall *)drillOnEntity:(CISDOBIpadEntity *)entity;

//! Get detail information from the openBIS ipad service and store the results in the managedObjectContext.
- (CISDOBAsyncCall *)detailsForEntity:(CISDOBIpadEntity *)entity;

// Local Actions -- actions that do not require a network connection
- (NSArray *)allIpadEntitiesOrError:(NSError **)error;
- (NSArray *)entitiesByPermId:(NSArray *)permIds error:(NSError **)error;
- (NSArray *)entitiesNotUpdatedSince:(NSDate *)date error:(NSError **)error;

- (NSFetchRequest *)entityFetchRequest;
- (NSArray *)executeFetchRequest:(NSFetchRequest *)fetchRequest error:(NSError **)error;

@end
