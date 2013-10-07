//
//  PuzzlesViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/15/12.
//
//

#import <UIKit/UIKit.h>
#import "BlockedViewController.h"
#import <StoreKit/SKProductsRequest.h>
#import "EventListenerDelegate.h"

@class FISound;

@interface PuzzlesViewController : BlockedViewController<SKProductsRequestDelegate, EventListenerDelegate, UIScrollViewDelegate, UITableViewDataSource, UITableViewDelegate>

@end
