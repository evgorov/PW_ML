//
//  DataContext.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/8/13.
//
//

#import "DataContext.h"
#import "AppDelegate.h"

@implementation DataContext

static NSMutableDictionary * contexts = nil;
static DataContext *gMainSharedInstance;
static dispatch_queue_t dataQueue;
static NSMutableDictionary *requests;

+(void) initialize {
    if (self == [DataContext class])
    {
        gMainSharedInstance = [[DataContext alloc] initWithConcurrencyType:NSMainQueueConcurrencyType];
        contexts = [NSMutableDictionary new];
//        dataQueue = dispatch_queue_create("ru.aipmedia.prizeword.DataQueue", DISPATCH_QUEUE_SERIAL);
        dataQueue = dispatch_get_main_queue();
        requests = [NSMutableDictionary new];
    }
}

+(DataContext *)mainContext
{
    return gMainSharedInstance;
}

+(DataContext *)dcInstance
{
    DataContext *context = [[DataContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
    [context setParentContext:gMainSharedInstance];
    return context;
}

+(NSManagedObjectContext *)currentContext
{
    if(dispatch_get_main_queue() == dispatch_get_current_queue())
        return [self mainContext];

    dispatch_queue_t current_queue = dispatch_get_current_queue();
    if (current_queue != dataQueue)
    {
        NSLog(@"unknown queue: %@", current_queue);
    }
    NSNumber * identifier = [NSNumber numberWithLongLong:(long long)current_queue];
    
    DataContext *context = [contexts objectForKey:identifier];
    if(!context)
    {
        context = [self dcInstance];
        assert(context);
        [contexts setObject:context forKey:identifier];
    }
    return context;
}

+ (void)performAsyncInDataQueue:(void (^)())block
{
//    NSLog(@"performAsyncInDataQueue %@", [NSThread callStackSymbols]);
//    [DataContext performSyncInDataQueue:block];
    dispatch_async(dataQueue, block);
}

+ (void)performSyncInDataQueue:(void (^)())block
{
    /*
    NSString * callstack = [NSThread callStackSymbols].description;
    NSNumber * number = [requests objectForKey:callstack];
    if (number == nil)
    {
        number = [NSNumber numberWithInt:1];
    }
    else
    {
        number = [NSNumber numberWithInt:number.intValue + 1];
    }
    [requests setObject:number forKey:callstack];
    NSLog(@"start performSyncInDataQueue %d %@", requests.count, callstack);
    */
    if (dispatch_get_current_queue() == dataQueue)
    {
//        NSLog(@"dispatch from current dataQueue");
        block();
    }
    else
    {
//        NSLog(@"dispatch sync from dataQueue");
        dispatch_sync(dataQueue, block);
    }
    /*
    number = [requests objectForKey:callstack];
    if (number == nil || number.intValue == 1)
    {
        [requests removeObjectForKey:callstack];
    }
    else
    {
        number = [NSNumber numberWithInt:number.intValue - 1];
        [requests setObject:number forKey:callstack];
    }
    NSLog(@"finish performSyncInDataQueue %d %@", requests.count, callstack);
    */
}

-(id)initWithConcurrencyType:(NSManagedObjectContextConcurrencyType)ct
{
    self = [super initWithConcurrencyType:ct];
    if(ct == NSMainQueueConcurrencyType)
        [self initContext];
    return self;
}

- (NSManagedObjectModel *)initializeManagedObjectModel
{
    NSURL *modelURL = [[NSBundle mainBundle] URLForResource:@"PrizeWord" withExtension:@"momd"];
    NSManagedObjectModel * managedObjectModel = [[NSManagedObjectModel alloc] initWithContentsOfURL:modelURL];
    return managedObjectModel;
}

- (NSPersistentStoreCoordinator *)initializePersistentStoreCoordinator
{
    NSURL *storeURL = [[[AppDelegate currentDelegate] applicationDocumentsDirectory] URLByAppendingPathComponent:@"PrizeWord.sqlite"];
    
    NSError *error = nil;
    NSPersistentStoreCoordinator * persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:[self initializeManagedObjectModel]];
    
    NSDictionary *options = [NSDictionary dictionaryWithObjectsAndKeys:
                             [NSNumber numberWithBool:YES], NSMigratePersistentStoresAutomaticallyOption,
                             [NSNumber numberWithBool:YES], NSInferMappingModelAutomaticallyOption, nil];
    
    if (![persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:storeURL options:options error:&error]) {
        [[NSFileManager defaultManager] removeItemAtURL:storeURL error:nil];
        persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:[self initializeManagedObjectModel]];
        
        NSDictionary *options = [NSDictionary dictionaryWithObjectsAndKeys:
                                 [NSNumber numberWithBool:YES], NSMigratePersistentStoresAutomaticallyOption,
                                 [NSNumber numberWithBool:YES], NSInferMappingModelAutomaticallyOption, nil];
        if (![persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:storeURL options:options error:&error]) {
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
            abort();
        }
    }
    return persistentStoreCoordinator;
}

-(void)initContext
{
    NSLog(@"INIT CONTEXT");
    NSPersistentStoreCoordinator *coordinator = [self initializePersistentStoreCoordinator];
    if (coordinator != nil) {
        [self setUndoManager:[NSUndoManager new]];
        [self setPersistentStoreCoordinator:coordinator];
    }
}

@end