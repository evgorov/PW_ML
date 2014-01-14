//
//  StoreManager.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/9/13.
//
//

#import "StoreManager.h"
#import <StoreKit/StoreKit.h>
#import "GlobalData.h"
#import "EventManager.h"
#import "SBJsonParser.h"
#import "PrizewordStoreObserver.h"
#import "AppDelegate.h"
#import "UserData.h"
#import "PuzzleData.h"
#import "UserDataManager.h"
#import "DataContext.h"
#import "NSData+Base64.h"

NSString * PRODUCTID_PREFIX = @"com.prizeword.";
NSString * PRODUCTID_HINTS10 = @"com.prizeword.hints10";
NSString * PRODUCTID_HINTS20 = @"com.prizeword.hints20";
NSString * PRODUCTID_HINTS30 = @"com.prizeword.hints30";

@interface SKProductsRequestDelegateWithBlock : NSObject<SKProductsRequestDelegate>
{
    void (^block)(SKProductsRequestDelegateWithBlock *, SKProductsResponse *);
}

@property (nonatomic, retain) SKProductsRequest * request;

- (id)initWithBlock:(void(^)(SKProductsRequestDelegateWithBlock *, SKProductsResponse *))block;

@end

@implementation SKProductsRequestDelegateWithBlock

@synthesize request;

- (id)initWithBlock:(void (^)(SKProductsRequestDelegateWithBlock *, SKProductsResponse *))block_
{
    self = [super init];
    if (self != nil)
    {
        block = block_;
    }
    return self;
}

- (void)productsRequest:(SKProductsRequest *)request_ didReceiveResponse:(SKProductsResponse *)response
{
    if (block != nil)
    {
        block(self, response);
    }
}

- (void)dealloc
{
    if (request != nil)
    {
        request.delegate = nil;
        [request cancel];
        request = nil;
    }
}

@end

@interface StoreManager ()
{
    NSOperationQueue * backgroundOperationQueue;
    NSMutableSet * productRequests;
}

- (void)fetchPricesForProductIDs:(NSArray *)productIDs completion:(DictionaryDataFetchCallback)callback;
- (void)purchaseProduct:(SKProduct *)product;
- (void)buySet:(NSString *)setID withTransaction:(SKPaymentTransaction *)transaction;
//- (void)handleSetBoughtWithTransaction:(SKPaymentTransaction *)transaction;

@end

@implementation StoreManager

+ (StoreManager *)sharedManager
{
    static StoreManager * sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [StoreManager new];
    });
    return sharedInstance;
}

- (id)init
{
    self = [super init];
    if (self != nil)
    {
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_PRODUCT_BOUGHT];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_PRODUCT_ERROR];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_PRODUCT_FAILED];
        backgroundOperationQueue = [NSOperationQueue new];
        productRequests = [NSMutableSet new];
    }
    return self;
}

- (void)cancelAllOperations
{
    [backgroundOperationQueue cancelAllOperations];
}

- (void)fetchPricesForHintsWithCompletion:(ArrayDataFetchCallback)callback
{
    [self fetchPricesForProductIDs:@[PRODUCTID_HINTS10, PRODUCTID_HINTS20, PRODUCTID_HINTS30] completion:^(NSDictionary *data, NSError *error) {
        if (error != nil || data == nil || data.count != 3)
        {
            if (callback != nil)
            {
                callback(nil, error);
            }
            return;
        }
        
        NSArray * result = @[[data objectForKey:PRODUCTID_HINTS10], [data objectForKey:PRODUCTID_HINTS20], [data objectForKey:PRODUCTID_HINTS30]];
        if (callback != nil)
        {
            callback(result, nil);
        }
    }];
}

- (void)fetchPriceForSet:(NSString *)setId completion:(StringDataFetchCallback)callback
{
    if (setId == nil)
    {
        return;
    }
    [self fetchPricesForProductIDs:@[[NSString stringWithFormat:@"%@%@", PRODUCTID_PREFIX, setId]] completion:^(NSDictionary *data, NSError *error) {
        if (error != nil || data == nil)
        {
            if (callback != nil)
            {
                callback(nil, error);
            }
            return;
        }
        NSString * price = [data objectForKey:[NSString stringWithFormat:@"%@%@", PRODUCTID_PREFIX, setId]];
        if (callback != nil)
        {
            callback(price, nil);
        }
    }];
}

- (void)purchaseHints:(int)count
{
    NSString * productID = nil;
    if (count == 10)
    {
        productID = PRODUCTID_HINTS10;
    }
    else if (count == 20)
    {
        productID = PRODUCTID_HINTS20;
    }
    else if (count == 30)
    {
        productID = PRODUCTID_HINTS30;
    }
    else
    {
        return;
    }
    SKProduct * product = [[GlobalData globalData].products objectForKey:productID];
    [self purchaseProduct:product];
}

- (void)purchaseSet:(NSString *)setId
{
    NSString * productID = [NSString stringWithFormat:@"%@%@", PRODUCTID_PREFIX, setId];
    SKProduct * product = [[GlobalData globalData].products objectForKey:productID];
    if (product == nil)
    {
        NSLog(@"WARNING: there is no product for set. May be it is free?");
        [self buySet:setId withTransaction:nil];
    }
    else
    {
        [self purchaseProduct:product];
    }
}

#pragma mark EventListenerDelegate
- (void)handleEvent:(Event *)event
{
    if (event.type == EVENT_PRODUCT_BOUGHT)
    {
        //        [self hideActivityIndicator];
        
        SKPaymentTransaction * paymentTransaction = event.data;
        if (paymentTransaction.transactionState == SKPaymentTransactionStatePurchased)
        {
            NSLog(@"EVENT_PRODUCT_BOUGHT: %@", paymentTransaction.payment.productIdentifier);
            
            if ([paymentTransaction.payment.productIdentifier compare:PRODUCTID_HINTS10] == NSOrderedSame)
            {
                [[UserDataManager sharedManager] addHints:10];
            }
            else if ([paymentTransaction.payment.productIdentifier compare:PRODUCTID_HINTS20] == NSOrderedSame)
            {
                [[UserDataManager sharedManager] addHints:20];
            }
            else if ([paymentTransaction.payment.productIdentifier compare:PRODUCTID_HINTS30] == NSOrderedSame)
            {
                [[UserDataManager sharedManager] addHints:30];
            }
            else
            {
                [self buySet:[paymentTransaction.payment.productIdentifier substringFromIndex:PRODUCTID_PREFIX.length] withTransaction:paymentTransaction];
            }
        }
        else if (paymentTransaction.error != nil)
        {
            NSLog(@"payment error: %@", paymentTransaction.error.localizedDescription);
        }
        
    }
    else if (event.type == EVENT_PRODUCT_ERROR)
    {
        NSLog(@"EVENT_PRODUCT_ERROR");
        //        [self hideActivityIndicator];
        NSError * error = event.data;
        UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:error.localizedDescription delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alertView show];
    }
    else if (event.type == EVENT_PRODUCT_FAILED)
    {
        NSLog(@"EVENT_PRODUCT_FAILED");
        //        [self hideActivityIndicator];
        SKPaymentTransaction * paymentTransaction = event.data;
        if (paymentTransaction.error != nil)
        {
            NSLog(@"error: %@", paymentTransaction.error.description);
        }
    }
}

#pragma mark private

- (void)buySet:(NSString *)setID withTransaction:(SKPaymentTransaction *)transaction
{
    if (setID == nil)
    {
        NSLog(@"WARNING: try to purchase nil set");
        return;
    }
    NSLog(@"buy set: %@", setID);

    NSMutableDictionary * params = @{@"id": setID
                                     , @"session_key": [GlobalData globalData].sessionKey}.mutableCopy;
    // transaction == nil for free sets
    if (transaction != nil)
    {
        [params setObject:[transaction.transactionReceipt base64EncodedString] forKey:@"receipt-data"];
    }
    
    [[APIClient sharedClient] postPath:[NSString stringWithFormat:@"sets/%@/buy", setID] parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
        NSLog(@"set bought! %@", setID);
        //        [self hideActivityIndicator];
        if (operation.response.statusCode == 200)
        {
            dispatch_async(dispatch_get_main_queue(), ^{
                __block PuzzleSetData * puzzleSet = [[DataManager sharedManager] localGetSet:setID];
                if (puzzleSet == nil)
                {
                    NSLog(@"ERROR: cannot get local puzzle set");
                    return;
                }
                
                NSDictionary * params = @{@"session_key": [GlobalData globalData].sessionKey
                                          , @"ids": puzzleSet.puzzle_ids};
                //            [self showActivityIndicator];
                [[APIClient sharedClient] getPath:@"user_puzzles" parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
                    NSLog(@"puzzles loaded!");
                    dispatch_async(dispatch_get_main_queue(), ^{
                        NSArray * puzzlesData = [[SBJsonParser new] objectWithData:operation.responseData];
                        for (NSDictionary * puzzleData in puzzlesData)
                        {
                            PuzzleData * puzzle = [PuzzleData puzzleWithDictionary:puzzleData andUserId:[GlobalData globalData].loggedInUser.user_id inMOC:[DataContext currentContext]];
                            if (puzzle != nil)
                            {
                                [puzzleSet addPuzzlesObject:puzzle];
                            }
                        }
                        
                        [puzzleSet setBought:[NSNumber numberWithBool:YES]];
                        NSAssert(puzzleSet.managedObjectContext != nil, @"managed object context of managed object in nil");
                        [puzzleSet.managedObjectContext save:nil];
                        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_SET_BOUGHT andData:puzzleSet.set_id]];
                        NSLog(@"view for puzzles created!");
                    });
                } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
                    [(PrizewordStoreObserver *)[AppDelegate storeObserver] setShouldIgnoreWarnings:YES];
                    NSLog(@"puzzles error: %@", error.description);
                }];
            });
        }
        else if (![(PrizewordStoreObserver *)[AppDelegate storeObserver] shouldIgnoreWarnings])
        {
            if (operation.response.statusCode >= 400 && operation.response.statusCode < 500)
            {
                [(PrizewordStoreObserver *)[AppDelegate storeObserver] setShouldIgnoreWarnings:YES];
                NSDictionary * data = [[SBJsonParser new] objectWithData:operation.responseData];
                NSString * message = [data objectForKey:@"message"];
                if (message == nil)
                {
                    message = NSLocalizedString(@"Unknown error", @"Unknown error on server");
                }
                UIAlertView * alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:message delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                [alert show];
                return;
            }
            
        }
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"set error: %@", error.description);
    }];
}

- (void)fetchPricesForProductIDs:(NSArray *)productIDs completion:(DictionaryDataFetchCallback)callback
{
    NSBlockOperation * operation = [NSBlockOperation new];
    __block __weak NSBlockOperation * fetchOperation = operation;
    NSLog(@"PRODUCT REQUEST: inilialize with products: %@", productIDs.description);
    
    [fetchOperation addExecutionBlock:^{
        __block NSMutableDictionary * prices = [NSMutableDictionary new];
        __block NSMutableSet * unknownProducts = [NSMutableSet new];
        
        for (NSString * productID in productIDs)
        {
            SKProduct * product = [[GlobalData globalData].products objectForKey:productID];
            if (product != nil)
            {
                NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
                [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
                [formatter setLocale:product.priceLocale];
                NSString *localizedMoneyString = [formatter stringFromNumber:product.price];
                [prices setObject:localizedMoneyString forKey:productID];
                NSLog(@"PRODUCT REQUEST: product found: %@", product.productIdentifier);
            }
            else
            {
                NSLog(@"PRODUCT REQUEST: product unknown: %@", productID);
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
        
        SKProductsRequestDelegateWithBlock * delegate = [[SKProductsRequestDelegateWithBlock alloc] initWithBlock:^(SKProductsRequestDelegateWithBlock * delegate, SKProductsResponse * response) {
            if (response != nil && response.products != nil)
            {
                NSMutableArray * productsIDs = [NSMutableArray new];
                for (SKProduct * product in response.products)
                {
                    NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
                    [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
                    [formatter setLocale:product.priceLocale];
                    NSString *localizedMoneyString = [formatter stringFromNumber:product.price];
                    [prices setObject:localizedMoneyString forKey:product.productIdentifier];
                    [[GlobalData globalData].products setObject:product forKey:product.productIdentifier];
                    [productsIDs addObject:product.productIdentifier];
                }
                NSLog(@"PRODUCT REQUEST: products requested: %@", productsIDs);
                if (callback != nil && ![fetchOperation isCancelled])
                {
                    callback(prices, nil);
                }
            }
            else
            {
                NSLog(@"PRODUCT REQUEST: request failed");
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                [productRequests removeObject:delegate];
            });
        }];
        SKProductsRequest * productsRequest = [[SKProductsRequest alloc] initWithProductIdentifiers:unknownProducts];
        delegate.request = productsRequest;
        productsRequest.delegate = delegate;
        [productsRequest start];
        [productRequests addObject:delegate];
    }];
    
    [operation setThreadPriority:0.3];
    [backgroundOperationQueue addOperation:operation];
}

- (void)purchaseProduct:(SKProduct *)product
{
    if (product != nil)
    {
        SKPayment * payment = [SKPayment paymentWithProduct:product];
        NSLog(@"enqueue product request: %@", payment.productIdentifier);
        [[SKPaymentQueue defaultQueue] addPayment:payment];
    }
    else
    {
        NSLog(@"ERROR: product was not requested");
    }
}


@end
