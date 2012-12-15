//
//  BadgeView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/15/12.
//
//

#import "BadgeView.h"

@implementation BadgeView

@synthesize badgeType = _badgeType;

-(id)initWithType:(BadgeType)badgeType andNumber:(int)number andPercent:(float)percent
{
    UIImage * bgImage = [UIImage imageNamed:@"puzzles_badge_bg"];
    self = [super initWithFrame:CGRectMake(0, 0, bgImage.size.width, bgImage.size.height)];
    if (self)
    {
        _badgeType = badgeType;
        [self setBackgroundImage:bgImage forState:UIControlStateNormal];
        
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
        float badgeScale = badgesUIImage.size.width / CGImageGetWidth(badgesImage);
        number--;
        
        CGImageRef badgeImage = CGImageCreateWithImageInRect(badgesImage, CGRectMake((number % 8) * badgeWidth, (number / 8) * badgeHeight, badgeWidth, badgeHeight));
        UIImageView * badge = [[UIImageView alloc] initWithImage:[UIImage imageWithCGImage:badgeImage]];
        CGImageRelease(badgeImage);
        badge.frame = CGRectMake(1, 1, badgeWidth * badgeScale, badgeHeight * badgeScale);
        badge.userInteractionEnabled = NO;
        [self addSubview:badge];
        
        UILabel * lblPercent = [[UILabel alloc] initWithFrame:CGRectMake(0, 53, self.frame.size.width, 30)];
        lblPercent.font = [UIFont fontWithName:@"DINPro-Bold" size:15];
        lblPercent.textColor = [UIColor colorWithRed:66/255.0f green:55/255.0f blue:49/255.0f alpha:1];
        lblPercent.shadowColor = [UIColor whiteColor];
        lblPercent.backgroundColor = [UIColor clearColor];
        lblPercent.shadowOffset = CGSizeMake(0, 1);
        lblPercent.textAlignment = NSTextAlignmentCenter;
        lblPercent.text = [NSString stringWithFormat:@"%d%%", (int)(percent * 100)];
        lblPercent.userInteractionEnabled = NO;
        [self addSubview:lblPercent];
        
        UIImage * imgProgress = [UIImage imageNamed:@"puzzles_badge_progress"];
        CGSize imageSize = imgProgress.size;
        if ([imgProgress respondsToSelector:@selector(resizableImageWithCapInsets:)])
        {
            imgProgress = [imgProgress resizableImageWithCapInsets:UIEdgeInsetsMake(imageSize.height / 2 - 1, imageSize.width / 2 - 1, imageSize.height / 2, imageSize.width / 2)];
        }
        else
        {
            imgProgress = [imgProgress stretchableImageWithLeftCapWidth:(imageSize.width / 2) topCapHeight:(imageSize.height / 2)];
        }
        UIImageView * progressImageView = [[UIImageView alloc] initWithImage:imgProgress];
        progressImageView.frame = CGRectMake(6, 80.5f, percent * 46, progressImageView.frame.size.height);
        progressImageView.userInteractionEnabled = NO;
        [self addSubview:progressImageView];
        
        self.userInteractionEnabled = YES;
    }
    return self;
}

-(id)initWithType:(BadgeType)badgeType andNumber:(int)number andScore:(int)score
{
    UIImage * bgImage = [UIImage imageNamed:@"puzzles_badge_done_bg"];
    self = [super initWithFrame:CGRectMake(0, 0, bgImage.size.width, bgImage.size.height)];
    if (self)
    {
        _badgeType = badgeType;
        [self setBackgroundImage:bgImage forState:UIControlStateNormal];
        
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
        float badgeScale = badgesUIImage.size.width / CGImageGetWidth(badgesImage);
        number--;
        
        CGImageRef badgeImage = CGImageCreateWithImageInRect(badgesImage, CGRectMake((number % 8) * badgeWidth, (number / 8) * badgeHeight, badgeWidth, badgeHeight));
        UIImageView * badge = [[UIImageView alloc] initWithImage:[UIImage imageWithCGImage:badgeImage]];
        CGImageRelease(badgeImage);
        badge.frame = CGRectMake(1, 1, badgeWidth * badgeScale, badgeHeight * badgeScale);
        badge.userInteractionEnabled = NO;
        [self addSubview:badge];
        
        UILabel * lblScore = [[UILabel alloc] initWithFrame:CGRectMake(0, 72, self.frame.size.width, 20)];
        lblScore.font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:11];
        lblScore.textColor = [UIColor colorWithRed:66/255.0f green:55/255.0f blue:49/255.0f alpha:1];
        lblScore.shadowColor = [UIColor whiteColor];
        lblScore.backgroundColor = [UIColor clearColor];
        lblScore.shadowOffset = CGSizeMake(0, 1);
        lblScore.textAlignment = NSTextAlignmentCenter;
        if (score < 1000)
        {
            lblScore.text = [NSString stringWithFormat:@"%d", score];
        }
        else
        {
            lblScore.text = [NSString stringWithFormat:@"%d %03d", score / 1000, score % 1000];
        }
        lblScore.userInteractionEnabled = NO;
        [self addSubview:lblScore];
        
        
        UIImage * imgStar = [UIImage imageNamed:@"puzzles_badge_star"];
        UIImageView * starImageView = [[UIImageView alloc] initWithImage:imgStar];
        CGSize scoreSize = [lblScore.text sizeWithFont:lblScore.font];
        float space = 5;
        float start = (self.frame.size.width - (imgStar.size.width + scoreSize.width + space)) / 2;
        lblScore.frame = CGRectMake(start + space + starImageView.frame.size.width, lblScore.frame.origin.y, scoreSize.width, lblScore.frame.size.height);
        starImageView.frame = CGRectMake(start, 76, starImageView.frame.size.width, starImageView.frame.size.height);
        starImageView.userInteractionEnabled = NO;
        [self addSubview:starImageView];
        
        self.userInteractionEnabled = NO;
    }
    return self;
}

+ (BadgeView *)badgeWithType:(BadgeType)badgeType andNumber:(int)number andPercent:(float)percent
{
    return [[BadgeView alloc] initWithType:badgeType andNumber:number andPercent:percent];

}

+(BadgeView *)badgeWithType:(BadgeType)badgeType andNumber:(int)number andScore:(int)score
{
    return [[BadgeView alloc] initWithType:badgeType andNumber:number andScore:score];
}

@end
