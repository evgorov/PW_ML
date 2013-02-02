//
//  BadgeView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/15/12.
//
//

#import <UIKit/UIKit.h>
#import "PuzzleData.h"
#import "EventListenerDelegate.h"

@interface BadgeView : UIButton<EventListenerDelegate>
{
    int badgeNumber;
    IBOutlet UIImageView *imgBg;
    IBOutlet UIImageView *badgeImage;
    IBOutlet UILabel *lblPercent;
    IBOutlet UILabel *lblScore;
    IBOutlet UIImageView *imgProgress;
    IBOutlet UIImageView *imgStar;
    IBOutlet UIView *imgOverlay;
}

+ (BadgeView *)badgeForPuzzle:(PuzzleData *)puzzle andNumber:(int)number;

@property (readonly) PuzzleData * puzzle;

@end
