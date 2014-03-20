//
//  BadgeView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/15/12.
//
//

#import "BadgeView.h"
#import "AppDelegate.h"
#import "PuzzleSetProxy.h"

@interface BadgeView (private)

-(void)initWithPuzzle:(PuzzleProxy *)puzzle andNumber:(int)number;

@end

@implementation BadgeView

@synthesize puzzle = _puzzle;

-(void)initWithPuzzle:(PuzzleProxy *)puzzle andNumber:(int)number
{
    if (self)
    {
        badgeNumber = number;
        _puzzle = puzzle;
        
        NSString * badgesImageName = puzzle.progress == 1 ? @"puzzles_badges_done_" : @"puzzles_badges_";
        UIImage * badgesUIImage;
        switch (_puzzle.puzzleSet.type.intValue) {
            case PUZZLESET_BRILLIANT:
                badgesImageName = [badgesImageName stringByAppendingString:@"brilliant"];
                break;
                
            case PUZZLESET_GOLD:
                badgesImageName = [badgesImageName stringByAppendingString:@"gold"];
                break;
                
            case PUZZLESET_SILVER:
            case PUZZLESET_SILVER2:
                badgesImageName = [badgesImageName stringByAppendingString:@"silver"];
                break;
                
            case PUZZLESET_FREE:
                badgesImageName = [badgesImageName stringByAppendingString:@"free"];
                break;
                
            default:
                return;
        }
        badgesUIImage = [UIImage imageNamed:badgesImageName];
        CGImageRef badgesImage = badgesUIImage.CGImage;
        int badgeWidth = CGImageGetWidth(badgesImage) / 8;
        int badgeHeight = CGImageGetHeight(badgesImage) / 4;
        number--;
        
        CGImageRef badgeCGImage = CGImageCreateWithImageInRect(badgesImage, CGRectMake((number % 8) * badgeWidth, (number / 8) * badgeHeight, badgeWidth, badgeHeight));
        badgeImage.image = [UIImage imageWithCGImage:badgeCGImage];
        CGImageRelease(badgeCGImage);
        
        if (puzzle.progress < 1)
        {
            lblScore.hidden = YES;
            imgStar.hidden = YES;
            imgFlag.hidden = ![[[NSUserDefaults standardUserDefaults] stringForKey:@"puzzleInProgress"] isEqualToString:puzzle.puzzle_id];
            
            lblPercent.hidden = NO;
            imgProgress.hidden = NO;
            lblPercent.font = [UIFont fontWithName:@"DINPro-Bold" size:([AppDelegate currentDelegate].isIPad ? 17 : 15)];
            lblPercent.text = [NSString stringWithFormat:@"%d%%", (int)(_puzzle.progress * 100)];
            
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
            imgProgressBg.hidden = NO;
            imgProgress.image = image;
            imgProgress.frame = CGRectMake(imgProgress.frame.origin.x, imgProgress.frame.origin.y, _puzzle.progress * ([AppDelegate currentDelegate].isIPad ? 57 : 47), imgProgress.frame.size.height);
        }
        else
        {
            lblScore.hidden = NO;
            imgStar.hidden = NO;
            imgFlag.hidden = YES;
            
            lblPercent.hidden = YES;
            imgProgress.hidden = YES;
            imgProgressBg.hidden = YES;
            int score = _puzzle.score.intValue;
            if (score == 0)
            {
                lblScore.hidden = YES;
                imgStar.hidden = YES;
            }
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
            lblScore.frame = CGRectIntegral(CGRectMake(start + space + imgStar.frame.size.width, lblScore.frame.origin.y, scoreSize.width, lblScore.frame.size.height));
            imgStar.frame = CGRectIntegral(CGRectMake(start, imgStar.frame.origin.y, imgStar.frame.size.width, imgStar.frame.size.height));
        }
        [puzzle synchronize];
    }
}

-(void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    imgOverlay.hidden = NO;
    [super touchesBegan:touches withEvent:event];
}

-(void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event
{
    imgOverlay.hidden = YES;
    [super touchesCancelled:touches withEvent:event];
}

-(void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
    imgOverlay.hidden = YES;
    [super touchesEnded:touches withEvent:event];
}

+(BadgeView *)badgeForPuzzle:(PuzzleProxy *)puzzle andNumber:(int)number
{
    BadgeView * badgeView = (BadgeView *)[[[NSBundle mainBundle] loadNibNamed:@"BadgeView" owner:self options:nil] objectAtIndex:0];
    [badgeView initWithPuzzle:puzzle andNumber:number];
    return badgeView;
}

-(void)updateWithPuzzle:(PuzzleProxy *)puzzle
{
    [self initWithPuzzle:puzzle andNumber:badgeNumber];
}

@end
