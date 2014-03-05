//
//  DataManager.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/5/13.
//
//

#import "DataManager.h"
#import "APIClient.h"
#import "GlobalData.h"
#import "SBJson.h"
#import "UserData.h"
#import "PuzzleData.h"
#import "NewsData.h"
#import "PuzzleSetData.h"
#import "PuzzleSetPackData.h"
#import "AppDelegate.h"
#import "AFHTTPRequestOperation.h"
#import "DataContext.h"
#import <StoreKit/SKProduct.h>
#import <StoreKit/SKProductsRequest.h>
#import "DataProxy.h"
#import "PuzzleProxy.h"
#import "PuzzleSetProxy.h"
#import "PuzzleSetPackProxy.h"
#import "QuestionProxy.h"

@interface DataManager()
{
    NSOperationQueue * backgroundOperationQueue;
}

- (void)fetchPuzzles:(NSArray *)ids completion:(ArrayDataFetchCallback)callback;
- (PuzzleSetPackData *)localGetSetsForMonth:(int)month year:(int)year;
- (NSArray *)localGetPuzzles:(NSArray *)ids;

- (NSArray *)proxyArray:(NSArray *)originArray;
- (NSSet *)proxySet:(NSSet *)originSet;
- (DataProxy *)proxyObject:(NSManagedObject *)originObject;

@end

@implementation DataManager

+ (DataManager *)sharedManager
{
    static DataManager * _sharedManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedManager = [DataManager new];
    });
    return _sharedManager;
}

- (id)init
{
    self = [super init];
    if (self != nil)
    {
        backgroundOperationQueue = [NSOperationQueue new];
        [backgroundOperationQueue setMaxConcurrentOperationCount:1];
    }
    return self;
}

- (void)dealloc
{
    [backgroundOperationQueue cancelAllOperations];
    backgroundOperationQueue = nil;
}

- (void)cancelAll
{
    [backgroundOperationQueue cancelAllOperations];
}

- (void)fetchCurrentMonthSetsWithCompletion:(ArrayDataFetchCallback)callback
{
    NSBlockOperation * operation = [NSBlockOperation new];
    __block __weak NSBlockOperation * fetchOperation = operation;
    
    [fetchOperation addExecutionBlock:^{
        __block NSMutableURLRequest * request = nil;
        __block PuzzleSetPackData * localPack;
        __block NSManagedObjectContext * managedObjectContext = nil;
        
        if ([GlobalData globalData].sessionKey == nil || [GlobalData globalData].loggedInUser == nil || [GlobalData globalData].loggedInUser.user_id == nil)
        {
            if (callback != nil)
            {
                callback(nil, nil);
            }
            return;
        }

        [DataContext performSyncInDataQueue:^{
            NSDictionary * parameters = @{@"session_key": [GlobalData globalData].sessionKey
                                          , @"month": [NSNumber numberWithInt:[GlobalData globalData].currentMonth]
                                          , @"year": [NSNumber numberWithInt:[GlobalData globalData].currentYear]
                                          , @"mode": @"short"
                                          };
            
            managedObjectContext = [DataContext currentContext];
            [managedObjectContext.undoManager beginUndoGrouping];
            
            localPack = [self localGetSetsForMonth:[GlobalData globalData].currentMonth year:[GlobalData globalData].currentYear];
            if (localPack == nil)
            {
                return;
            }
            NSLog(@"local pack count: %d", localPack.puzzleSets.count);
            
            request = [[APIClient sharedClient] requestWithMethod:@"GET" path:@"published_sets" parameters:parameters];
            [request setValue:localPack.etag forHTTPHeaderField:@"If-None-Match"];
        }];
        
        if (request == nil) {
            return;
        }
        
        AFHTTPRequestOperation * requestOperation = [[APIClient sharedClient] HTTPRequestOperationWithRequest:request success:^(AFHTTPRequestOperation *operation, id responseObject) {
            [DataContext performSyncInDataQueue:^{
                if (operation.response.statusCode == 200)
                {
                    NSLog(@"sets fetched");
                    localPack.etag = [operation.response.allHeaderFields objectForKey:@"Etag"];
                    NSArray * setsData = [[SBJsonParser new] objectWithData:responseObject];
                    NSMutableArray * puzzleIdsToLoad = [NSMutableArray new];
                    for (NSDictionary * setData in setsData)
                    {
                        PuzzleSetData * puzzleSet = [PuzzleSetData puzzleSetWithDictionary:setData andUserId:[GlobalData globalData].loggedInUser.user_id];
                        if (puzzleSet == nil)
                        {
                            NSLog(@"puzzle set is nil");
                        }
                        [localPack addPuzzleSetsObject:puzzleSet];
                        if (!puzzleSet.bought.boolValue)
                        {
                            continue;
                        }
                        NSArray * puzzleIds = [setData objectForKey:@"puzzles"];
                        [puzzleIdsToLoad addObjectsFromArray:puzzleIds];
                        
                        if ([fetchOperation isCancelled])
                        {
                            [managedObjectContext.undoManager endUndoGrouping];
                            [managedObjectContext.undoManager undo];
                            return;
                        }
                    }
                    
                    __block NSBlockOperation * parentFetchOperation = fetchOperation;
                    [self fetchPuzzles:puzzleIdsToLoad completion:^(NSArray *puzzles, NSError *error) {
                        if ([parentFetchOperation isCancelled])
                        {
                            [managedObjectContext.undoManager endUndoGrouping];
                            [managedObjectContext.undoManager undo];
                            return;
                        }
                        if (puzzles != nil)
                        {
                            NSArray * archiveSets = [localPack.puzzleSets.allObjects sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
                                PuzzleSetData * set1 = obj1;
                                PuzzleSetData * set2 = obj2;
                                
                                return [set1.type compare:set2.type];
                            }];
                            
                            for (PuzzleProxy * puzzle in puzzles)
                            {
                                [puzzle prepareManagedObject];
                                for (PuzzleSetData * puzzleSet in archiveSets)
                                {
                                    if ([puzzleSet.puzzle_ids rangeOfString:puzzle.puzzle_id].location != NSNotFound)
                                    {
                                        [puzzleSet addPuzzlesObject:(PuzzleData *)puzzle.managedObject];
                                        break;
                                    }
                                }
                            }
                            [managedObjectContext.undoManager endUndoGrouping];
                            [managedObjectContext lock];
                            [managedObjectContext save:nil];
                            [managedObjectContext unlock];
                            if (callback != nil && ![parentFetchOperation isCancelled])
                            {
                                callback([self proxyArray:archiveSets], nil);
                            }
                        }
                        else
                        {
                            [managedObjectContext.undoManager endUndoGrouping];
                            [managedObjectContext.undoManager undo];
                            if (callback != nil && ![parentFetchOperation isCancelled])
                            {
                                callback(nil, error);
                            }
                        }
                    }];
                }
                else
                {
                    [managedObjectContext.undoManager endUndoGrouping];
                    [managedObjectContext lock];
                    [managedObjectContext save:nil];
                    [managedObjectContext unlock];
                    if (callback != nil && ![fetchOperation isCancelled])
                    {
                        NSArray * sortedSets = [[localPack.puzzleSets allObjects] sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
                            PuzzleSetData * set1 = obj1;
                            PuzzleSetData * set2 = obj2;
                            
                            return [set1.type compare:set2.type];
                        }];
                        callback([self proxyArray:sortedSets], nil);
                    }
                }
            }];

        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            [DataContext performSyncInDataQueue:^{
                [managedObjectContext.undoManager endUndoGrouping];
                if (callback != nil && ![fetchOperation isCancelled])
                {
                    if (localPack != nil && localPack.puzzleSets.count > 0)
                    {
                        [managedObjectContext lock];
                        [managedObjectContext save:nil];
                        [managedObjectContext unlock];
                        NSArray * sortedSets = [[localPack.puzzleSets allObjects] sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
                            PuzzleSetData * set1 = obj1;
                            PuzzleSetData * set2 = obj2;
                            
                            return [set1.type compare:set2.type];
                        }];
                        callback([self proxyArray:sortedSets], nil);
                    }
                    else
                    {
                        [managedObjectContext.undoManager undo];
                        callback(nil, error);
                    }
                }
            }];
        }];
        
        [[APIClient sharedClient] enqueueHTTPRequestOperation:requestOperation];
    }];
    
    [fetchOperation setThreadPriority:0.3];
    [backgroundOperationQueue addOperation:fetchOperation];
}

- (void)fetchArchiveSetsForMonth:(int)month year:(int)year completion:(ArrayDataFetchCallback)callback
{
    NSBlockOperation * operation = [NSBlockOperation new];
    __block __weak NSBlockOperation * fetchOperation = operation;
    
    [fetchOperation addExecutionBlock:^{
        __block NSManagedObjectContext * managedObjectContext = nil;
        __block PuzzleSetPackData * archivePack;
        __block NSMutableURLRequest * request = nil;
        
        if ([GlobalData globalData].sessionKey == nil || [GlobalData globalData].loggedInUser == nil)
        {
            if (callback != nil)
            {
                callback(nil, nil);
            }
            return;
        }

        [DataContext performSyncInDataQueue:^{
            archivePack = [self localGetSetsForMonth:month year:year];
            if (archivePack != nil && archivePack.puzzleSets.count > 0)
            {
                if (callback != nil && ![fetchOperation isCancelled])
                {
                    NSArray * sortedSets = [archivePack.puzzleSets.allObjects sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
                        PuzzleSetData * set1 = obj1;
                        PuzzleSetData * set2 = obj2;
                        
                        return [set1.type compare:set2.type];
                    }];
                    callback([self proxyArray:sortedSets], nil);
                }
                return;
            }
            
            managedObjectContext = [DataContext currentContext];
            [managedObjectContext.undoManager beginUndoGrouping];
            
            NSDictionary * parameters = @{@"session_key": [GlobalData globalData].sessionKey
                                          , @"month": [NSNumber numberWithInt:month]
                                          , @"year": [NSNumber numberWithInt:year]
                                          , @"mode": @"short"
                                          };
            
            request = [[APIClient sharedClient] requestWithMethod:@"GET" path:@"published_sets" parameters:parameters];
            [request setValue:archivePack.etag forHTTPHeaderField:@"If-None-Match"];
        }];
        
        if (request == nil)
        {
            return;
        }
        
        AFHTTPRequestOperation * requestOperation = [[APIClient sharedClient] HTTPRequestOperationWithRequest:request success:^(AFHTTPRequestOperation *operation, id responseObject) {
            [DataContext performSyncInDataQueue:^{
                if (operation.response.statusCode == 200) {
                    archivePack.etag = [operation.response.allHeaderFields objectForKey:@"Etag"];
                    NSLog(@"archive fetched");
                    NSArray * setsData = [[SBJsonParser new] objectWithData:responseObject];
                    NSMutableArray * puzzleIdsToLoad = [NSMutableArray new];
                    for (NSDictionary * setData in setsData)
                    {
                        PuzzleSetData * puzzleSet = [PuzzleSetData puzzleSetWithDictionary:setData andUserId:[GlobalData globalData].loggedInUser.user_id];
                        if (puzzleSet == nil)
                        {
                            NSLog(@"WARNING: puzzle set is nil!");
                            continue;
                        }
                        if (puzzleSet.puzzleSetPack != nil)
                        {
                            NSLog(@"pack is not nil");
                        }
                        if (archivePack.managedObjectContext != puzzleSet.managedObjectContext) {
                            NSLog(@"managed object contexts are not equal");
                        }
                        [archivePack addPuzzleSetsObject:puzzleSet];
                        if (!puzzleSet.bought.boolValue)
                        {
                            continue;
                        }
                        NSArray * puzzleIds = [setData objectForKey:@"puzzles"];
                        [puzzleIdsToLoad addObjectsFromArray:puzzleIds];
                        
                        if ([fetchOperation isCancelled])
                        {
                            [managedObjectContext.undoManager endUndoGrouping];
                            [managedObjectContext.undoManager undo];
                            return;
                        }
                    }
                    
                    __block NSBlockOperation * parentFetchOperation = fetchOperation;
                    [self fetchPuzzles:puzzleIdsToLoad completion:^(NSArray *puzzles, NSError *error) {
                        [DataContext performAsyncInDataQueue: ^{
                            if ([parentFetchOperation isCancelled])
                            {
                                [managedObjectContext.undoManager endUndoGrouping];
                                [managedObjectContext.undoManager undo];
                                return;
                            }
                            if (puzzles != nil)
                            {
                                NSArray * archiveSets = [archivePack.puzzleSets.allObjects sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
                                    PuzzleSetData * set1 = obj1;
                                    PuzzleSetData * set2 = obj2;
                                    
                                    return [set1.type compare:set2.type];
                                }];
                                
                                for (PuzzleProxy * puzzle in puzzles)
                                {
                                    [puzzle prepareManagedObject];
                                    for (PuzzleSetData * puzzleSet in archiveSets)
                                    {
                                        if ([puzzleSet.puzzle_ids rangeOfString:puzzle.puzzle_id].location != NSNotFound)
                                        {
                                            [puzzleSet addPuzzlesObject:(PuzzleData *)puzzle.managedObject];
                                            break;
                                        }
                                    }
                                }
                                [managedObjectContext.undoManager endUndoGrouping];
                                [managedObjectContext lock];
                                [managedObjectContext save:nil];
                                [managedObjectContext unlock];
                                if (callback != nil && ![parentFetchOperation isCancelled])
                                {
                                    callback([self proxyArray:archiveSets], nil);
                                }
                            }
                            else
                            {
                                [managedObjectContext.undoManager endUndoGrouping];
                                [managedObjectContext.undoManager undo];
                                if (callback != nil && ![parentFetchOperation isCancelled])
                                {
                                    callback(nil, error);
                                }
                            }
                        }];
                    }];
                }
                else
                {
                    [managedObjectContext.undoManager endUndoGrouping];
                    [managedObjectContext.undoManager undo];
                    if (callback != nil && ![fetchOperation isCancelled])
                    {
                        // TODO :: generate error
                        callback(nil, nil);
                    }
                }
            }];

        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            [DataContext performSyncInDataQueue:^{
                [managedObjectContext.undoManager endUndoGrouping];
                [managedObjectContext.undoManager undo];
                if (callback != nil && ![fetchOperation isCancelled])
                {
                    callback(nil, error);
                }
            }];
        }];
        
        [[APIClient sharedClient] enqueueHTTPRequestOperation:requestOperation];
    }];
    
    [fetchOperation setThreadPriority:0.3];
    [backgroundOperationQueue addOperation:fetchOperation];
}

- (void)fetchPuzzles:(NSArray *)ids_ completion:(ArrayDataFetchCallback)callback
{
    NSBlockOperation * operation = [NSBlockOperation new];
    __block __weak NSBlockOperation * fetchOperation = operation;
    __block NSArray * ids = ids_;
    
    [fetchOperation addExecutionBlock:^{
        __block NSDictionary * parameters;
        __block NSMutableArray * puzzles;
        [DataContext performSyncInDataQueue:^{
            NSArray * localPuzzles = [self localGetPuzzles:ids];
            puzzles = [[NSMutableArray alloc] initWithCapacity:ids.count];
            if (localPuzzles != nil)
            {
                if (localPuzzles.count == ids.count)
                {
                    if (callback != nil && ![fetchOperation isCancelled])
                    {
                        callback([self proxyArray:localPuzzles], nil);
                    }
                    return;
                }
                [puzzles addObjectsFromArray:localPuzzles];
                NSIndexSet * indexSet = [ids indexesOfObjectsPassingTest:^BOOL(id obj, NSUInteger idx, BOOL *stop) {
                    for (PuzzleData * puzzle in localPuzzles)
                    {
                        if ([puzzle.puzzle_id compare:obj] == NSOrderedSame)
                        {
                            return NO;
                        }
                    }
                    return YES;
                }];
                ids = [ids objectsAtIndexes:indexSet];
            }
            
            NSLog(@"puzzles to load: %@", [ids componentsJoinedByString:@","]);
            parameters = @{@"ids": [ids componentsJoinedByString:@","]
                                          , @"session_key": [GlobalData globalData].sessionKey
                                          };
        }];
        if ([fetchOperation isCancelled] || parameters == nil)
        {
            return;
        }
        NSLog(@"user_puzzles parameters: %@", parameters);

        [[APIClient sharedClient] getPath:@"user_puzzles" parameters:parameters success:^(AFHTTPRequestOperation *operation, id responseObject) {
            [DataContext performSyncInDataQueue:^{
                NSLog(@"user_puzzles loaded");
                if ([fetchOperation isCancelled])
                {
                    return;
                }
                NSArray * puzzlesData = [[SBJsonParser new] objectWithData:responseObject];
                for (NSDictionary * puzzleData in puzzlesData)
                {
                    PuzzleData * puzzle = [PuzzleData puzzleWithDictionary:puzzleData andUserId:[GlobalData globalData].loggedInUser.user_id];
                    if (puzzle != nil)
                    {
                        [puzzles addObject:puzzle];
                    }
                }
                
                [[DataContext currentContext] save:nil];
                
                if (puzzles.count != ids_.count)
                {
                    NSLog(@"ERROR: count of fetched puzzles %d is not equal to count of requested ids %d", puzzles.count, ids.count);
                }
                
                if (callback != nil && ![fetchOperation isCancelled])
                {
                    callback([self proxyArray:puzzles], nil);
                }
            }];

        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            if (callback && ![fetchOperation isCancelled])
            {
                callback(nil, error);
            }
        }];
        
    }];
    
    [fetchOperation setThreadPriority:0.3];
    [backgroundOperationQueue addOperation:fetchOperation];
}

- (void)fetchNewsWithCompletion:(ArrayDataFetchCallback)callback
{
    NSBlockOperation * operation = [NSBlockOperation new];
    __block __weak NSBlockOperation * fetchOperation = operation;
    
    [fetchOperation addExecutionBlock:^{
        __block NewsData * news = nil;
        __block NSMutableURLRequest * request = nil;
        [DataContext performSyncInDataQueue:^{
            NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
            NSFetchRequest * fetchRequest = [[AppDelegate currentDelegate].managedObjectModel fetchRequestTemplateForName:@"NewsFetchRequest"];
            [managedObjectContext lock];
            NSArray * results = [managedObjectContext executeFetchRequest:fetchRequest error:nil];
            if (results != nil && results.count > 0)
            {
                news = results.lastObject;
            }
            else
            {
                news = [NSEntityDescription insertNewObjectForEntityForName:@"News" inManagedObjectContext:managedObjectContext];
            }
            
            if ([fetchOperation isCancelled])
            {
                return;
            }
            [managedObjectContext unlock];
            
            NSDictionary * parameters = @{@"session_key": [GlobalData globalData].sessionKey};
            request = [[APIClient sharedClient] requestWithMethod:@"GET" path:@"service_messages" parameters:parameters];
            NSLog(@"news etag: %@", news.etag);
            [request setValue:news.etag forHTTPHeaderField:@"If-None-Match"];
        }];
        
        if (request == nil)
        {
            return;
        }
        
        AFHTTPRequestOperation * op = [[APIClient sharedClient] HTTPRequestOperationWithRequest:request success:^(AFHTTPRequestOperation *operation, id responseObject) {
            [DataContext performSyncInDataQueue:^{
                if (operation.response.statusCode == 200)
                {
                    NSDictionary * messages = [[SBJsonParser new] objectWithData:responseObject];
                    news.news1 = nil;
                    news.news2 = nil;
                    news.news3 = nil;
                    news.etag = [operation.response.allHeaderFields objectForKey:@"Etag"];
                    NSLog(@"news new etag: %@", news.etag);
                    if ([messages objectForKey:@"message1"] != (id)[NSNull null])
                    {
                        news.news1 = [messages objectForKey:@"message1"];
                    }
                    if ([messages objectForKey:@"message2"] != (id)[NSNull null])
                    {
                        news.news2 = [messages objectForKey:@"message2"];
                    }
                    if ([messages objectForKey:@"message3"] != (id)[NSNull null])
                    {
                        news.news3 = [messages objectForKey:@"message3"];
                    }
                    [news.managedObjectContext lock];
                    [news.managedObjectContext save:nil];
                    [news.managedObjectContext unlock];
                }
                
                if (callback != nil && ![fetchOperation isCancelled])
                {
                    NSMutableArray * messagesArray = [NSMutableArray new];
                    if (news.news1 != nil)
                    {
                        [messagesArray addObject:news.news1];
                    }
                    if (news.news2 != nil)
                    {
                        [messagesArray addObject:news.news2];
                    }
                    if (news.news3 != nil)
                    {
                        [messagesArray addObject:news.news3];
                    }
                    callback(messagesArray, nil);
                }
            }];

        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            if (callback != nil && ![fetchOperation isCancelled])
            {
                [DataContext performSyncInDataQueue:^{
                    NSMutableArray * messagesArray = [NSMutableArray new];
                    if (news.news1 != nil)
                    {
                        [messagesArray addObject:news.news1];
                    }
                    if (news.news2 != nil)
                    {
                        [messagesArray addObject:news.news2];
                    }
                    if (news.news3 != nil)
                    {
                        [messagesArray addObject:news.news3];
                    }
                    callback(messagesArray, nil);
                }];
            }
        }];
        
        [[APIClient sharedClient] enqueueHTTPRequestOperation:op];
        
    }];
    
    [operation setThreadPriority:0.3];
    [backgroundOperationQueue addOperation:operation];
}

#pragma mark local operations

- (PuzzleSetPackData *)localGetSetsForMonth:(int)month year:(int)year
{
    if ([GlobalData globalData].loggedInUser.user_id == nil)
    {
        return nil;
    }
    PuzzleSetPackData * result = [PuzzleSetPackData puzzleSetPackWithYear:year andMonth:month andUserId:[GlobalData globalData].loggedInUser.user_id];
    return result;
}

- (NSArray *)localGetPuzzles:(NSArray *)ids
{
    NSMutableArray * puzzles = [NSMutableArray arrayWithCapacity:ids.count];
    for (NSString * puzzleId in ids)
    {
        PuzzleData * puzzle = [PuzzleData puzzleWithId:puzzleId andUserId:[GlobalData globalData].loggedInUser.user_id];
        if (puzzle != nil)
        {
            [puzzles addObject:puzzle];
        }
    }
    return puzzles;
}

- (PuzzleSetData *)localGetSet:(NSString *)setID
{
    return [PuzzleSetData puzzleSetWithId:setID andUserId:[GlobalData globalData].loggedInUser.user_id];
}

- (NSArray *)proxyArray:(NSArray *)originArray
{
    NSMutableArray * array = [NSMutableArray arrayWithCapacity:originArray.count];
    for (NSManagedObject * object in originArray) {
        [array addObject:[self proxyObject:object]];
    }
    return array;
}

- (NSSet *)proxySet:(NSSet *)originSet
{
    NSMutableSet * set = [NSMutableSet setWithCapacity:originSet.count];
    for (NSManagedObject * object in originSet) {
        [set addObject:[self proxyObject:object]];
    }
    return set;
}

- (DataProxy *)proxyObject:(NSManagedObject *)originObject
{
    if ([originObject isKindOfClass:[PuzzleData class]]) {
        return [[PuzzleProxy alloc] initWithObjectID:originObject.objectID];
    }
    else if ([originObject isKindOfClass:[PuzzleSetData class]]) {
        return [[PuzzleSetProxy alloc] initWithObjectID:originObject.objectID];
    }
    else if ([originObject isKindOfClass:[PuzzleSetPackData class]]) {
        return [[PuzzleSetPackProxy alloc] initWithObjectID:originObject.objectID];
    }
    else if ([originObject isKindOfClass:[QuestionData class]]) {
        return [[QuestionProxy alloc] initWithObjectID:originObject.objectID];
    }
    NSLog(@"cannot proxy instance of the class %@", originObject.class);
    return nil;
}

@end
