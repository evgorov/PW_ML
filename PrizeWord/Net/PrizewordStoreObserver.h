//
//  PrizewordStoreObserver.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 2/17/13.
//
//

#import <UIKit/UIKit.h>
#import <StoreKit/SKPaymentQueue.h>
#import "EventListenerDelegate.h"

@interface PrizewordStoreObserver : NSObject<SKPaymentTransactionObserver, EventListenerDelegate>

@end
