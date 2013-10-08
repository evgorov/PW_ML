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
#import "PuzzleSetData.h"
#import "PuzzleSetPackData.h"
#import "AppDelegate.h"
#import "AFHTTPRequestOperation.h"
#import "DataContext.h"
#import <StoreKit/SKProduct.h>
#import <StoreKit/SKProductsRequest.h>

@interface SKProductsRequestDelegateWithBlock : NSObject<SKProductsRequestDelegate>
{
    void (^block)(SKProductsResponse *);
}

@property (nonatomic, retain) SKProductsRequest * request;

- (id)initWithBlock:(void(^)(SKProductsResponse *))block;

@end

@implementation SKProductsRequestDelegateWithBlock

@synthesize request;

- (id)initWithBlock:(void (^)(SKProductsResponse *))block_
{
    self = [super init];
    if (self != nil)
    {
        block = block_;
    }
    return self;
}

- (void)productsRequest:(SKProductsRequest *)request didReceiveResponse:(SKProductsResponse *)response
{
    if (block != nil)
    {
        block(response);
    }
}

- (void)dealloc
{
    if (request != nil)
    {
        [request cancel];
        request.delegate = nil;
        request = nil;
    }
}

@end

@interface DataManager()
{
    NSOperationQueue * backgroundOperationQueue;
}

- (void)fetchPuzzles:(NSArray *)ids completion:(ArrayDataFetchCallback)callback;

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

- (void)fetchCurrentMonthSetsWithCompletion:(ArrayDataFetchCallback)callback
{
    NSBlockOperation * operation = [NSBlockOperation new];
    __block __weak NSBlockOperation * fetchOperation = operation;
    
    [fetchOperation addExecutionBlock:^{
        __block dispatch_queue_t current_dispatch_queue = dispatch_get_current_queue();
        __block NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
        [managedObjectContext.undoManager beginUndoGrouping];

        __block PuzzleSetPackData * localPack = [self localGetSetsForMonth:[GlobalData globalData].currentMonth year:[GlobalData globalData].currentYear];
        NSLog(@"local pack count: %d", localPack.puzzleSets.count);
        
        NSDictionary * parameters = @{@"session_key": [GlobalData globalData].sessionKey
                                      , @"month": localPack.month
                                      , @"year": localPack.year
                                      , @"mode": @"short"
                                      };
        NSMutableURLRequest * request = [[APIClient sharedClient] requestWithMethod:@"GET" path:@"published_sets" parameters:parameters];
        [request setValue:localPack.etag forHTTPHeaderField:@"If-None-Match"];
        
        AFHTTPRequestOperation * requestOperation = [[APIClient sharedClient] HTTPRequestOperationWithRequest:request success:^(AFHTTPRequestOperation *operation, id responseObject) {
            dispatch_async(current_dispatch_queue, ^{
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
                        if (localPack.managedObjectContext != puzzleSet.managedObjectContext)
                        {
                            NSLog(@"managed object contexts are not equal");
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
                        if (current_dispatch_queue != dispatch_get_current_queue())
                        {
                            NSLog(@"Different threads. It is bad!");
                        }
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
                            [managedObjectContext save:nil];
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
                }
                else
                {
                    [managedObjectContext.undoManager endUndoGrouping];
                    [managedObjectContext save:nil];
                    if (callback != nil && ![fetchOperation isCancelled])
                    {
                        callback([localPack.puzzleSets allObjects], nil);
                    }
                }
            });

        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            dispatch_async(current_dispatch_queue, ^{
                [managedObjectContext.undoManager endUndoGrouping];
                if (callback != nil && ![fetchOperation isCancelled])
                {
                    if (localPack != nil && localPack.puzzleSets.count > 0)
                    {
                        [managedObjectContext save:nil];
                        callback(localPack.puzzleSets.allObjects, nil);
                    }
                    else
                    {
                        [managedObjectContext.undoManager undo];
                        callback(nil, error);
                    }
                }
            });
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
        __block dispatch_queue_t current_dispatch_queue = dispatch_get_current_queue();
        __block NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
        [managedObjectContext.undoManager beginUndoGrouping];
        
        __block PuzzleSetPackData * archivePack = [self localGetSetsForMonth:month year:year];
        if (archivePack != nil && archivePack.puzzleSets.count > 0)
        {
            [managedObjectContext.undoManager endUndoGrouping];
            [managedObjectContext save:nil];
            if (callback != nil && ![fetchOperation isCancelled])
            {
                callback(archivePack.puzzleSets.allObjects, nil);
            }
            return;
        }
        
        NSDictionary * parameters = @{@"session_key": [GlobalData globalData].sessionKey
                                      , @"month": [NSNumber numberWithInt:month]
                                      , @"year": [NSNumber numberWithInt:year]
                                      , @"mode": @"short"
                                      };
        
        NSMutableURLRequest * request = [[APIClient sharedClient] requestWithMethod:@"GET" path:@"published_sets" parameters:parameters];
        [request setValue:archivePack.etag forHTTPHeaderField:@"If-None-Match"];
        
        AFHTTPRequestOperation * requestOperation = [[APIClient sharedClient] HTTPRequestOperationWithRequest:request success:^(AFHTTPRequestOperation *operation, id responseObject) {
            dispatch_async(current_dispatch_queue, ^{
                if (operation.response.statusCode == 200) {
                    archivePack.etag = [operation.response.allHeaderFields objectForKey:@"Etag"];
                    NSLog(@"archive fetched");
                    NSArray * setsData = [[SBJsonParser new] objectWithData:responseObject];
                    NSMutableArray * puzzleIdsToLoad = [NSMutableArray new];
                    for (NSDictionary * setData in setsData)
                    {
                        PuzzleSetData * puzzleSet = [PuzzleSetData puzzleSetWithDictionary:setData andUserId:[GlobalData globalData].loggedInUser.user_id];
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
                        if (current_dispatch_queue != dispatch_get_current_queue())
                        {
                            NSLog(@"Different threads. It is bad!");
                        }
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
                            [managedObjectContext save:nil];
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
            });

        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            dispatch_async(current_dispatch_queue, ^{
                [managedObjectContext.undoManager endUndoGrouping];
                [managedObjectContext.undoManager undo];
                if (callback != nil && ![fetchOperation isCancelled])
                {
                    callback(nil, error);
                }
            });
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
        NSArray * localPuzzles = [self localGetPuzzles:ids];
        __block dispatch_queue_t current_dispatch_queue = dispatch_get_current_queue();
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
            dispatch_async(current_dispatch_queue, ^{
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
            });

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
        __block dispatch_queue_t current_dispatch_queue = dispatch_get_current_queue();
        NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
        NSFetchRequest * fetchRequest = [[AppDelegate currentDelegate].managedObjectModel fetchRequestTemplateForName:@"NewsFetchRequest"];
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
        
        NSDictionary * parameters = @{@"session_key": [GlobalData globalData].sessionKey};
        NSMutableURLRequest * request = [[APIClient sharedClient] requestWithMethod:@"GET" path:@"service_messages" parameters:parameters];
        NSLog(@"news etag: %@", news.etag);
        [request setValue:news.etag forHTTPHeaderField:@"If-None-Match"];
        
        AFHTTPRequestOperation * op = [[APIClient sharedClient] HTTPRequestOperationWithRequest:request success:^(AFHTTPRequestOperation *operation, id responseObject) {
            dispatch_async(current_dispatch_queue, ^{
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
                    [managedObjectContext save:nil];
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
            });

        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            if (callback != nil && ![fetchOperation isCancelled])
            {
                dispatch_async(current_dispatch_queue, ^{
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
                });
            }
        }];
        
        [[APIClient sharedClient] enqueueHTTPRequestOperation:op];
        
    }];
    
    [operation setThreadPriority:0.3];
    [backgroundOperationQueue addOperation:operation];
}

- (void)fetchPricesForProductIDs:(NSArray *)productIDs completion:(DictionaryDataFetchCallback)callback
{
    NSBlockOperation * operation = [NSBlockOperation new];
    __block __weak NSBlockOperation * fetchOperation = operation;
    
    [fetchOperation addExecutionBlock:^{
        __block NSMutableDictionary * prices = [NSMutableDictionary new];
        __block NSMutableSet * unknownProducts = [NSMutableSet new];

        for (NSString * productID in productIDs)
        {
            SKProduct * product = [[GlobalData globalData].products objectForKey:productID];
            if (product != nil)
            {
                [prices setObject:[product.price descriptionWithLocale:product.priceLocale] forKey:productID];
            }
            else
            {
                [unknownProducts addObject:productID];
            }
        }
        
        if (unknownProducts.count == 0)
        {
            if (callback != nil && ![fetchOperation isCancelled])
            {
                callback(prices, nil);
            }
            return;
        }
        
        if ([fetchOperation isCancelled])
        {
            return;
        }
        
        SKProductsRequestDelegateWithBlock * delegate = [[SKProductsRequestDelegateWithBlock alloc] initWithBlock:^(SKProductsResponse * response) {
            if (response != nil && response.products != nil)
            {
                for (SKProduct * product in response.products)
                {
                    [prices setObject:product.productIdentifier forKey:[product.price descriptionWithLocale:product.priceLocale]];
                    [[GlobalData globalData].products setObject:product forKey:product.productIdentifier];
                }
                if (callback != nil && ![fetchOperation isCancelled])
                {
                    callback(prices, nil);
                }
            }
        }];
        SKProductsRequest * productsRequest = [[SKProductsRequest alloc] initWithProductIdentifiers:unknownProducts];
        delegate.request = productsRequest;
        productsRequest.delegate = delegate;
        [productsRequest start];
    }];
    
    [operation setThreadPriority:0.3];
    [backgroundOperationQueue addOperation:operation];}

#pragma mark local operations

- (PuzzleSetPackData *)localGetSetsForMonth:(int)month year:(int)year
{
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

@end
