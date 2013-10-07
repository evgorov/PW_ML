//
//  DataLayer.m
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
#import "AppDelegate.h"
#import "AFHTTPRequestOperation.h"

@interface DataManager()
{
    NSOperationQueue * backgroundOperationQueue;
}

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
        [backgroundOperationQueue setMaxConcurrentOperationCount:4];
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

- (void)fetchArchiveSetsForMonth:(int)month year:(int)year completion:(ArrayDataFetchCallback)callback
{
    NSBlockOperation * operation = [NSBlockOperation new];
    __block __weak NSBlockOperation * fetchOperation = operation;
    
    [fetchOperation addExecutionBlock:^{
        NSArray * archiveSets = [self localGetArchiveSetsForMonth:month year:year];
        if (archiveSets != nil && archiveSets.count > 0)
        {
            if (callback != nil && ![fetchOperation isCancelled])
            {
                callback(archiveSets, nil);
            }
            return;
        }
        
        NSDictionary * parameters = @{@"session_key": [GlobalData globalData].sessionKey
                                      , @"month": [NSNumber numberWithInt:month]
                                      , @"year": [NSNumber numberWithInt:year]
                                      , @"mode": @"short"
                                      };
        
        [[APIClient sharedClient] getPath:@"published_sets" parameters:parameters success:^(AFHTTPRequestOperation *operation, id responseObject) {
            NSLog(@"archive fetched");
            NSArray * setsData = [[SBJsonParser new] objectWithData:responseObject];
            __block NSMutableArray * sets = [NSMutableArray new];
            NSMutableArray * puzzleIdsToLoad = [NSMutableArray new];
            __block NSManagedObjectContext * managedObjectContext = [AppDelegate currentDelegate].managedObjectContext;
            [managedObjectContext.undoManager beginUndoGrouping];
            for (NSDictionary * setData in setsData)
            {
                PuzzleSetData * puzzleSet = [PuzzleSetData puzzleSetWithDictionary:setData andUserId:[GlobalData globalData].loggedInUser.user_id];
                if (!puzzleSet.bought.boolValue)
                {
                    continue;
                }
                [sets addObject:puzzleSet];
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
                    NSArray * archiveSets = [sets sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
                        PuzzleSetData * set1 = obj1;
                        PuzzleSetData * set2 = obj2;
                        
                        return [set1.type compare:set2.type];
                    }];
                    
                    for (PuzzleData * puzzle in puzzles)
                    {
                        for (PuzzleSetData * puzzleSet in archiveSets)
                        {
                            if ([puzzleSet.puzzle_ids rangeOfString:puzzle.puzzle_id].location != NSNotFound)
                            {
                                [puzzleSet addPuzzlesObject:puzzle];
                                break;
                            }
                        }
                    }
                    [managedObjectContext.undoManager endUndoGrouping];
                    if (callback != nil && ![parentFetchOperation isCancelled])
                    {
                        callback(archiveSets, nil);
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
        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            if (callback != nil && ![fetchOperation isCancelled])
            {
                callback(nil, error);
            }
        }];
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
        NSArray * localPuzzles = [self localGetPuzzles:ids];
        __block NSMutableArray * puzzles = [[NSMutableArray alloc] initWithCapacity:ids.count];
        if (localPuzzles != nil)
        {
            if (localPuzzles.count == ids.count)
            {
                if (callback != nil && ![fetchOperation isCancelled])
                {
                    callback(localPuzzles, nil);
                }
                return;
            }
            [puzzles addObjectsFromArray:localPuzzles];
            NSIndexSet * indexSet = [ids indexesOfObjectsPassingTest:^BOOL(id obj, NSUInteger idx, BOOL *stop) {
                for (PuzzleData * puzzle in localPuzzles) {
                    if ([puzzle.puzzle_id compare:obj] == NSOrderedSame)
                    {
                        return NO;
                    }
                }
                return YES;
            }];
            ids = [ids objectsAtIndexes:indexSet];
        }
        
        if ([fetchOperation isCancelled])
        {
            return;
        }
        NSLog(@"puzzles to load: %@", [ids componentsJoinedByString:@","]);
        NSDictionary * parameters = @{@"ids": [ids componentsJoinedByString:@","]
                                      , @"session_key": [GlobalData globalData].sessionKey
                                      };
        [[APIClient sharedClient] getPath:@"user_puzzles" parameters:parameters success:^(AFHTTPRequestOperation *operation, id responseObject) {
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
            
            if (puzzles.count != ids_.count)
            {
                NSLog(@"ERROR: count of fetched puzzles %d is not equal to count of requested ids %d", puzzles.count, ids.count);
            }
            
            if (callback != nil && ![fetchOperation isCancelled])
            {
                callback(puzzles, nil);
            }
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
        NSFetchRequest * fetchRequest = [[AppDelegate currentDelegate].managedObjectModel fetchRequestTemplateForName:@"NewsFetchRequest"];
        NSArray * results = [[AppDelegate currentDelegate].managedObjectContext executeFetchRequest:fetchRequest error:nil];
        __block NewsData * news = nil;
        if (results != nil && results.count > 0)
        {
            news = results.lastObject;
        }
        else
        {
            news = [NSEntityDescription insertNewObjectForEntityForName:@"News" inManagedObjectContext:[AppDelegate currentDelegate].managedObjectContext];
        }
        
        if ([fetchOperation isCancelled])
        {
            return;
        }
        
        NSDictionary * parameters = @{@"session_key": [GlobalData globalData].sessionKey};
        NSMutableURLRequest * request = [[APIClient sharedClient] requestWithMethod:@"GET" path:@"service_messages" parameters:parameters];
        NSLog(@"news etag: %@", news.etag);
        [request setValue:news.etag forHTTPHeaderField:@"If-None-Match"];
        
        AFHTTPRequestOperation * op = [[APIClient sharedClient] HTTPRequestOperationWithRequest:request success:^(AFHTTPRequestOperation *operation, id responseObject) {
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
                [[AppDelegate currentDelegate].managedObjectContext save:nil];
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
        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
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
        
        [[APIClient sharedClient] enqueueHTTPRequestOperation:op];
        
    }];
    
    [backgroundOperationQueue addOperation:operation];
}

#pragma mark local operations

- (NSArray *)localGetArchiveSetsForMonth:(int)month year:(int)year
{
    NSManagedObjectContext * managedObjectContext = [AppDelegate currentDelegate].managedObjectContext;
    NSFetchRequest * fetchRequest = [[AppDelegate currentDelegate].managedObjectModel fetchRequestFromTemplateWithName:@"PuzzleSetsFetchRequest" substitutionVariables:
                                     @{@"USER_ID": [GlobalData globalData].loggedInUser.user_id
                                     , @"MONTH": [NSNumber numberWithInt:month]
                                     , @"YEAR": [NSNumber numberWithInt:year]}];
    return [managedObjectContext executeFetchRequest:fetchRequest error:nil];
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

@end
