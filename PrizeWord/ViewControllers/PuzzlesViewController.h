//
//  PuzzlesViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/15/12.
//
//

#import <UIKit/UIKit.h>

@interface PuzzlesViewController : UIViewController
{
    IBOutlet UIScrollView *scrollView;
    IBOutlet UIView *contentView;
    
    IBOutlet UIView *newsView;
    IBOutlet UIView *currentPuzzlesView;
    IBOutlet UIImageView *currentPuzzlesBorder;
    
    IBOutlet UIView *hintsView;
    IBOutlet UIImageView *hintsBorder;
    
    IBOutlet UIView *archiveView;
    IBOutlet UIImageView *archiveBorder;
    
    NSMutableArray * currentGoldBadges;
    NSMutableArray * currentSilverBadges;
    NSMutableArray * archiveBadges;
}

@end
