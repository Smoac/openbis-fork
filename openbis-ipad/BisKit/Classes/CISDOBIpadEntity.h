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
 *
 * Since information for the entities are downloaded progressively, users of this
 * class should treat all properties EXCEPT permId and refcon as being potentially
 * nil. A nil value means that the true value is not known. An empty string signifies
 * an empty value for string properties and an empty array signifies an empty value
 * for array properties.
 */
@class CISDOBIpadRawEntity;
@interface CISDOBIpadEntity : NSManagedObject

// Non-nil properties
@property (nonatomic, retain) NSString * permId;
@property (nonatomic, retain) NSString * refconJson;
@property (readonly) id refcon;

// Potentially nil properties
@property (nonatomic, retain) NSString * summaryHeader;
@property (nonatomic, retain) NSString * summary;
@property (nonatomic, retain) NSString * identifier;
@property (nonatomic, retain) NSString * category;
@property (nonatomic, retain) NSString * imageUrl;

@property (nonatomic, retain) NSString * childrenPermIdsJson;
@property (readonly)          NSArray * childrenPermIds;

@property (nonatomic, retain) NSString * propertiesJson;
@property (readonly)          NSArray * properties;

@property (nonatomic, retain) NSNumber * rootLevel;

///! The image contained at the imageUrl or nil if there is no image. Could be a UIImage or CIImage depending on the platform.
@property (nonatomic, strong) id image;

// Actions
//! Take the values from the raw entity.
- (void)initializeFromRawEntity:(CISDOBIpadRawEntity *)rawEntity;
- (void)updateFromRawEntity:(CISDOBIpadRawEntity *)rawEntity;

@end


