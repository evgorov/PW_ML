//
//  BadgeView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/15/12.
//
//

#import <UIKit/UIKit.h>
#import "PuzzleData.h"

typedef enum BadgeType
{
    BADGE_BRILLIANT = 0,
    BADGE_GOLD,
    BADGE_SILVER,
    BADGE_FREE
} BadgeType;

@interface BadgeView : UIButton
{
    IBOutlet UIImageView *badgeImage;
    IBOutlet UILabel *lblPercent;
    IBOutlet UILabel *lblScore;
    IBOutlet UIImageView *imgProgress;
    IBOutlet UIImageView *imgStar;
    IBOutlet UIView *imgOverlay;
}

+ (BadgeView *)badgeWithType:(BadgeType)badgeType andNumber:(int)number andPercent:(float)percent;
+ (BadgeView *)badgeWithType:(BadgeType)badgeType andNumber:(int)number andScore:(int)score;

@property (readonly) BadgeType badgeType;

@end
