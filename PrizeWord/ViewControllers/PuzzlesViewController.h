//
//  PuzzlesViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/15/12.
//
//

#import <UIKit/UIKit.h>
#import "BlockedViewController.h"

@interface PuzzlesViewController : BlockedViewController
{
    IBOutlet UIView *newsView;
    IBOutlet UIView *currentPuzzlesView;
    IBOutlet UIView *hintsView;
    IBOutlet UIView *archiveView;
    
    NSMutableArray * currentGoldBadges;
    NSMutableArray * currentSilverBadges;
    NSMutableArray * archiveBadges;
}

@end
