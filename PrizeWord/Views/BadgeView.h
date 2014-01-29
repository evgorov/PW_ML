//
//  BadgeView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/15/12.
//
//

#import <UIKit/UIKit.h>
#import "PuzzleProxy.h"
#import "PrizeWordButton.h"

@interface BadgeView : PrizeWordButton
{
    int badgeNumber;
    IBOutlet UIImageView *imgBg;
    IBOutlet UIImageView *badgeImage;
    IBOutlet UILabel *lblPercent;
    IBOutlet UILabel *lblScore;
    IBOutlet UIImageView *imgProgressBg;
    IBOutlet UIImageView *imgProgress;
    IBOutlet UIImageView *imgStar;
    IBOutlet UIView *imgOverlay;
}

+(BadgeView *)badgeForPuzzle:(PuzzleProxy *)puzzle andNumber:(int)number;
-(void)updateWithPuzzle:(PuzzleProxy *)puzzle;

@property (readonly) PuzzleProxy * puzzle;

@end
