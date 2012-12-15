//
//  BadgeView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/15/12.
//
//

#import <UIKit/UIKit.h>

typedef enum BadgeType
{
    BADGE_BRILLIANT = 0,
    BADGE_GOLD,
    BADGE_SILVER,
    BADGE_FREE
} BadgeType;

@interface BadgeView : UIButton

-(id)initWithType:(BadgeType)badgeType andNumber:(int)number andPercent:(float)percent;

+ (BadgeView *)badgeWithType:(BadgeType)badgeType andNumber:(int)number andPercent:(float)percent;

@property (readonly) BadgeType badgeType;

@end
