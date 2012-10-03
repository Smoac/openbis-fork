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
//  CISDOBIpadEntity.m
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 10/1/12.
//
//

#import "CISDOBIpadEntity.h"
#import "CISDOBIpadService.h"


@implementation CISDOBIpadEntity

@dynamic summaryHeader;
@dynamic summary;
@dynamic identifier;
@dynamic permId;
@dynamic entityKind;
@dynamic entityType;
@dynamic propertiesJson;

- (void)initializeFromRawEntity:(CISDOBIpadRawEntity *)rawEntity
{
    self.summaryHeader = rawEntity.summaryHeader;
    self.summary = rawEntity.summary;
    self.identifier = rawEntity.identifier;
    self.permId = rawEntity.permId;
    self.entityKind = rawEntity.entityKind;
    self.entityType = rawEntity.entityType;
    self.propertiesJson = rawEntity.properties;
}

@end
