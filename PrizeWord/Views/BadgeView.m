//
//  BadgeView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/15/12.
//
//

#import "BadgeView.h"
#import "AppDelegate.h"

@interface BadgeView (private)

-(id)initWithType:(BadgeType)badgeType andNumber:(int)number andPercent:(float)percent;
-(id)initWithType:(BadgeType)badgeType andNumber:(int)number andScore:(int)score;

@end

@implementation BadgeView

@synthesize badgeType = _badgeType;

-(id)initWithType:(BadgeType)badgeType andNumber:(int)number andPercent:(float)percent
{
    if (self)
    {
        _badgeType = badgeType;
        
        UIImage * badgesUIImage;
        switch (badgeType) {
            case BADGE_BRILLIANT:
                badgesUIImage = [UIImage imageNamed:@"puzzles_badges_brilliant"];
                break;
                
            case BADGE_GOLD:
                badgesUIImage = [UIImage imageNamed:@"puzzles_badges_gold"];
                break;
                
            case BADGE_SILVER:
                badgesUIImage = [UIImage imageNamed:@"puzzles_badges_silver"];
                break;
                
            case BADGE_FREE:
                badgesUIImage = [UIImage imageNamed:@"puzzles_badges_free"];
                break;
                
            default:
                return nil;
                break;
        }
        CGImageRef badgesImage = badgesUIImage.CGImage;
        float badgeWidth = CGImageGetWidth(badgesImage) / 8;
        float badgeHeight = CGImageGetHeight(badgesImage) / 4;
        number--;
        
        CGImageRef badgeCGImage = CGImageCreateWithImageInRect(badgesImage, CGRectMake((number % 8) * badgeWidth, (number / 8) * badgeHeight, badgeWidth, badgeHeight));
        badgeImage.image = [UIImage imageWithCGImage:badgeCGImage];
        CGImageRelease(badgeCGImage);
        
        lblPercent.font = [UIFont fontWithName:@"DINPro-Bold" size:([AppDelegate currentDelegate].isIPad ? 17 : 15)];
        lblPercent.text = [NSString stringWithFormat:@"%d%%", (int)(percent * 100)];
        
        UIImage * image = [UIImage imageNamed:@"puzzles_badge_progress"];
        CGSize imageSize = image.size;
        if ([image respondsToSelector:@selector(resizableImageWithCapInsets:)])
        {
            image = [image resizableImageWithCapInsets:UIEdgeInsetsMake(imageSize.height / 2 - 1, imageSize.width / 2 - 1, imageSize.height / 2, imageSize.width / 2)];
        }
        else
        {
            image = [image stretchableImageWithLeftCapWidth:(imageSize.width / 2) topCapHeight:(imageSize.height / 2)];
        }
        imgProgress.image = image;
        imgProgress.frame = CGRectMake(imgProgress.frame.origin.x, imgProgress.frame.origin.y, percent * ([AppDelegate currentDelegate].isIPad ? 56 : 46), imgProgress.frame.size.height);
    }
    return self;
}

-(id)initWithType:(BadgeType)badgeType andNumber:(int)number andScore:(int)score
{
    if (self)
    {
        _badgeType = badgeType;
        
        UIImage * badgesUIImage;
        switch (badgeType) {
            case BADGE_BRILLIANT:
                badgesUIImage = [UIImage imageNamed:@"puzzles_badges_done_brilliant"];
                break;
                
            case BADGE_GOLD:
                badgesUIImage = [UIImage imageNamed:@"puzzles_badges_done_gold"];
                break;
                
            case BADGE_SILVER:
                badgesUIImage = [UIImage imageNamed:@"puzzles_badges_done_silver"];
                break;
                
            case BADGE_FREE:
                badgesUIImage = [UIImage imageNamed:@"puzzles_badges_done_free"];
                break;
                
            default:
                return nil;
                break;
        }
        CGImageRef badgesImage = badgesUIImage.CGImage;
        float badgeWidth = CGImageGetWidth(badgesImage) / 8;
        float badgeHeight = CGImageGetHeight(badgesImage) / 4;
        number--;
        
        CGImageRef badgeCGImage = CGImageCreateWithImageInRect(badgesImage, CGRectMake((number % 8) * badgeWidth, (number / 8) * badgeHeight, badgeWidth, badgeHeight));
        badgeImage.image = [UIImage imageWithCGImage:badgeCGImage];
        CGImageRelease(badgeCGImage);
        
        if (score < 1000)
        {
            lblScore.text = [NSString stringWithFormat:@"%d", score];
        }
        else
        {
            lblScore.text = [NSString stringWithFormat:@"%d %03d", score / 1000, score % 1000];
        }
        
        
        CGSize scoreSize = [lblScore.text sizeWithFont:lblScore.font];
        float space = 5;
        float start = (self.frame.size.width - (imgStar.frame.size.width + scoreSize.width + space)) / 2;
        lblScore.frame = CGRectMake(start + space + imgStar.frame.size.width, lblScore.frame.origin.y, scoreSize.width, lblScore.frame.size.height);
        imgStar.frame = CGRectMake(start, imgStar.frame.origin.y, imgStar.frame.size.width, imgStar.frame.size.height);
    }
    return self;
}

+ (BadgeView *)badgeWithType:(BadgeType)badgeType andNumber:(int)number andPercent:(float)percent
{
    BadgeView * badgeView = (BadgeView *)[[[NSBundle mainBundle] loadNibNamed:@"BadgeView" owner:self options:nil] objectAtIndex:0];
    return [badgeView initWithType:badgeType andNumber:number andPercent:percent];

}

+(BadgeView *)badgeWithType:(BadgeType)badgeType andNumber:(int)number andScore:(int)score
{
    BadgeView * badgeView = (BadgeView *)[[[NSBundle mainBundle] loadNibNamed:@"BadgeView" owner:self options:nil] objectAtIndex:1];
    return [badgeView initWithType:badgeType andNumber:number andScore:score];
}

@end
