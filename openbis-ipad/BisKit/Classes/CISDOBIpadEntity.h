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
//  CISDOBIpadEntity.h
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 10/1/12.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


/**
 * \brief A persistent version of an entity from the iPad server.
 */
@class CISDOBIpadRawEntity;
@interface CISDOBIpadEntity : NSManagedObject

@property (nonatomic, retain) NSString * summaryHeader;
@property (nonatomic, retain) NSString * summary;
@property (nonatomic, retain) NSString * identifier;
@property (nonatomic, retain) NSString * permId;
@property (nonatomic, retain) NSString * entityKind;
@property (nonatomic, retain) NSString * entityType;
@property (nonatomic, retain) NSString * imageUrl;
@property (nonatomic, retain) NSString * propertiesJson;
@property (readonly)          NSArray * properties;

// Actions
//! Take the values from the raw entity.
- (void)initializeFromRawEntity:(CISDOBIpadRawEntity *)rawEntity;

@end
