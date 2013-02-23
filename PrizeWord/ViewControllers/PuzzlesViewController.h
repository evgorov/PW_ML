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

@interface PuzzlesViewController : BlockedViewController<SKProductsRequestDelegate, EventListenerDelegate>
{
    IBOutlet UIView *newsView;
    IBOutlet UIView *currentPuzzlesView;
    IBOutlet UIView *hintsView;
    IBOutlet UIView *archiveView;
    IBOutlet UIView *setToBuyView;
    
    IBOutlet UIPageControl *newsPaginator;
    IBOutlet UIScrollView *newsScrollView;

    IBOutlet UILabel *puzzlesViewCaption;

    IBOutlet UIButton *btnBuyHint1;
    IBOutlet UIButton *btnBuyHint2;
    IBOutlet UIButton *btnBuyHint3;
    IBOutlet UILabel *lblHintsLeft;
    NSMutableArray * hintsProducts;
    SKProductsRequest * productsRequest;
}

@end
