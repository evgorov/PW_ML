//
//  StoreManager.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/9/13.
//
//

#import <Foundation/Foundation.h>
#import "EventListenerDelegate.h"
#import "DataManager.h"

@interface StoreManager : NSObject<EventListenerDelegate>

+ (StoreManager *)sharedManager;

- (void)cancelAllOperations;

- (void)fetchPricesForHintsWithCompletion:(ArrayDataFetchCallback)callback;
- (void)fetchPriceForSet:(NSString *)setId completion:(StringDataFetchCallback)callback;

- (void)purchaseHints:(int)count;
- (void)purchaseSet:(NSString *)setId;

@end
