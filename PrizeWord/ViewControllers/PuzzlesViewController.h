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

@interface PuzzlesViewController : UIViewController<SKProductsRequestDelegate, EventListenerDelegate, UIScrollViewDelegate, UITableViewDataSource, UITableViewDelegate>
{
    __weak IBOutlet UITableView *tableView;
    
    IBOutlet UIView *currentPuzzlesView;
    IBOutlet UIView *hintsView;
    IBOutlet UIView *archiveView;
    IBOutlet UIView *setToBuyView;
    
    IBOutlet UILabel *puzzlesViewCaption;
    IBOutlet UIImageView *puzzlesTimeLeftBg;
    IBOutlet UILabel *puzzlesTimeLeftCaption;

    IBOutlet PrizeWordButton *btnBuyHint1;
    IBOutlet PrizeWordButton *btnBuyHint2;
    IBOutlet PrizeWordButton *btnBuyHint3;
    IBOutlet UILabel *lblHintsLeft;
    NSMutableArray * hintsProducts;
    SKProductsRequest * productsRequest;
    
    int archiveLastMonth;
    int archiveLastYear;
    BOOL archiveNeedLoading;
    BOOL archiveLoading;
    
    FISound * buySetSound;
    FISound * openSetSound;
    FISound * closeSetSound;
}

@end
