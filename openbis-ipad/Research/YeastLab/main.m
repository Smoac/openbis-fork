//
//  main.m
//  YeastLab
//
//  Created by Ramakrishnan  Chandrasekhar on 12/12/12.
//
//

#import "CISDOBIpadServiceManager.h"
#import "CISDOBShared.h"
#import "CISDOBAsyncCall.h"

static NSManagedObjectModel *ManagedObjectModel()
{
    static NSManagedObjectModel *model = nil;
    if (model != nil) {
        return model;
    }
    
    NSString *path = @"persistent-data-model";
    path = [path stringByDeletingPathExtension];
    NSURL *modelURL = [NSURL fileURLWithPath:[path stringByAppendingPathExtension:@"momd"]];
    model = [[NSManagedObjectModel alloc] initWithContentsOfURL: modelURL];
    
    return model;
}

static NSURL *StoreUrl()
{
    NSString *path = [[NSProcessInfo processInfo] arguments][0];
    path = [path stringByDeletingPathExtension];
    NSURL *url = [NSURL fileURLWithPath:[path stringByAppendingPathExtension:@"sqlite"]];
    
    return url;
}

static NSManagedObjectContext *ManagedObjectContext()
{
    static NSManagedObjectContext *context = nil;
    if (context != nil) {
        return context;
    }

    @autoreleasepool {
        context = [[NSManagedObjectContext alloc] init];
        
        NSPersistentStoreCoordinator *coordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:ManagedObjectModel()];
        [context setPersistentStoreCoordinator:coordinator];
        
        NSString *STORE_TYPE = NSSQLiteStoreType;
        NSURL *url = StoreUrl();
        
        NSError *error;
        NSPersistentStore *newStore = [coordinator addPersistentStoreWithType:STORE_TYPE configuration:nil URL:url options:nil error:&error];
        
        if (newStore == nil) {
            NSLog(@"Store Configuration Failure %@", ([error localizedDescription] != nil) ? [error localizedDescription] : @"Unknown Error");
        }
    }
    return context;
}

int main(int argc, const char * argv[])
{

    @autoreleasepool {
        NSManagedObjectContext *context = ManagedObjectContext();

        NSError *error = nil;
        
        NSURL *storeUrl = StoreUrl();
        NSURL *bisUrl = [NSURL URLWithString: @"https://localhost:8443/"];
        CISDOBIpadServiceManager* openbis =
            [[CISDOBIpadServiceManager alloc]
                initWithStoreUrl: storeUrl openbisUrl: bisUrl trusted: YES error: &error];
        CISDOBAsyncCall *login = [openbis loginUser: @"admin" password: @"password"];
        [login start];

        // Wait 10 secs for completion
        int seconds = 10;
        BOOL __block callCompleted = NO;
        SuccessBlock success = ^(id result) { callCompleted = YES; };
        FailBlock fail = ^(NSError *error) { NSLog(@"Error %@", error); callCompleted = YES; };
        login.success = success;
        login.fail = fail;
        int i;
        for(i = 0; i < seconds && !callCompleted; ++i) {
            // Run the runloop until an answer is returned
            CFRunLoopRunInMode(kCFRunLoopDefaultMode, 1, 0);
        }
        
        NSLog(@"Took %is to login", i);
        
        // Save the managed object context
        if (![context save:&error]) {
            NSLog(@"Error while saving %@", ([error localizedDescription] != nil) ? [error localizedDescription] : @"Unknown Error");
            exit(1);
        }
    }
    return 0;
}

