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
    IBOutlet UILabel *newsLbl1;
    IBOutlet UILabel *newsLbl2;
    IBOutlet UILabel *newsLbl3;
    IBOutlet UIView *currentPuzzlesView;
    IBOutlet UIView *hintsView;
    IBOutlet UIView *archiveView;
    IBOutlet UIView *setToBuyView;
    
    IBOutlet UIPageControl *newsPaginator;
    IBOutlet UIScrollView *newsScrollView;

    IBOutlet UILabel *puzzlesViewCaption;

    IBOutlet PrizeWordButton *btnBuyHint1;
    IBOutlet PrizeWordButton *btnBuyHint2;
    IBOutlet PrizeWordButton *btnBuyHint3;
    IBOutlet UILabel *lblHintsLeft;
    NSMutableArray * hintsProducts;
    SKProductsRequest * productsRequest;
}

@end
