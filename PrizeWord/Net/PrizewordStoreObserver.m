//
//  PrizewordStoreObserver.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 2/17/13.
//
//

#import "PrizewordStoreObserver.h"
#import "EventManager.h"
#import <StoreKit/SKProduct.h>
#import <StoreKit/SKPayment.h>
#import <StoreKit/SKPaymentTransaction.h>

@implementation PrizewordStoreObserver

@synthesize shouldIgnoreWarnings;

-(id)init
{
    self = [super init];
    if (self)
    {
        shouldIgnoreWarnings = NO;
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_REQUEST_PRODUCT];
        NSArray * transactions = [[SKPaymentQueue defaultQueue].transactions copy];
        for (SKPaymentTransaction * transaction in transactions)
        {
            if (transaction.transactionState != SKPaymentTransactionStatePurchasing)
            {
                [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
            }
        }
    }
    return self;
}

-(void)dealloc
{
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_REQUEST_PRODUCT];
}

-(void)paymentQueue:(SKPaymentQueue *)queue removedTransactions:(NSArray *)transactions
{
    NSLog(@"paymentQueue removedTransactions");
}

-(void)paymentQueue:(SKPaymentQueue *)queue restoreCompletedTransactionsFailedWithError:(NSError *)error
{
    NSLog(@"paymentQueue restoreCompletedTransactionsFailedWithError: %@", error.description);
    
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_PRODUCT_ERROR andData:error]];
}

-(void)paymentQueue:(SKPaymentQueue *)queue updatedDownloads:(NSArray *)downloads
{
    NSLog(@"paymentQueue updatedDownloads");
    
}

-(void)paymentQueue:(SKPaymentQueue *)queue updatedTransactions:(NSArray *)transactions
{
    NSLog(@"paymentQueue updatedTransactions");
    
    shouldIgnoreWarnings = NO;
    
    for (SKPaymentTransaction * transaction in transactions)
    {
        if (transaction.transactionState == SKPaymentTransactionStatePurchased || transaction.transactionState == SKPaymentTransactionStateRestored)
        {
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_PRODUCT_BOUGHT andData:transaction]];
            [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
        }
        else if (transaction.transactionState == SKPaymentTransactionStateFailed)
        {
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_PRODUCT_FAILED andData:transaction]];
            [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
        }
    }
}

-(void)paymentQueueRestoreCompletedTransactionsFinished:(SKPaymentQueue *)queue
{
    NSLog(@"paymentQueueRestoreCompletedTransactionsFinished");
    
}

-(void)handleEvent:(Event *)event
{
    if (event.type == EVENT_REQUEST_PRODUCT)
    {
        SKProduct * product = event.data;
        SKPayment * payment = [SKPayment paymentWithProduct:product];
        NSLog(@"enqueue product request: %@", payment.productIdentifier);
        [[SKPaymentQueue defaultQueue] addPayment:payment];
    }
}

@end
