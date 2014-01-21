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

+(void) initialize {
    if (self == [DataContext class])
    {
        gMainSharedInstance = [[DataContext alloc] initWithConcurrencyType:NSMainQueueConcurrencyType];
        contexts = [NSMutableDictionary new];
        dataQueue = dispatch_queue_create("DataQueue", DISPATCH_QUEUE_SERIAL);
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
    dispatch_async(dataQueue, block);
}

+ (void)performSyncInDataQueue:(void (^)())block
{
    if (dispatch_get_current_queue() == dataQueue)
    {
        block();
    }
    else
    {
        dispatch_sync(dataQueue, block);
    }
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