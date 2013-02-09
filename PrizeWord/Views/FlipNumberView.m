//
//  FlipNumberView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 2/9/13.
//
//

#import "FlipNumberView.h"

@interface FlipNumberView (private)

-(void)animateFlip;
-(void)flipRecursive:(id)sender;

@end

@implementation FlipNumberView

static NSMutableArray * topParts = nil;
static NSMutableArray * bottomParts = nil;

void initParts()
{
    topParts = [NSMutableArray arrayWithCapacity:10];
    bottomParts = [NSMutableArray arrayWithCapacity:10];
    
    UIImage * numbers = [UIImage imageNamed:@"final_flip_numbers.png"];
    int numberWidth = CGImageGetWidth(numbers.CGImage) / 10;
    int numberHeight = CGImageGetHeight(numbers.CGImage);

    for (int i = 0; i < 10; ++i)
    {
        UIImage * topPart = [UIImage imageWithCGImage:CGImageCreateWithImageInRect(numbers.CGImage, CGRectMake(i * numberWidth, 0, numberWidth, numberHeight / 2))];
        UIImage * bottomPart = [UIImage imageWithCGImage:CGImageCreateWithImageInRect(numbers.CGImage, CGRectMake(i * numberWidth, numberHeight / 2, numberWidth, numberHeight / 2))];
        [topParts addObject:topPart];
        [bottomParts addObject:bottomPart];
    }
}

-(void)awakeFromNib
{
    self.backgroundColor = [UIColor clearColor];
    [self reset];
}

-(void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
    [self animateFlip];
}

-(void)animateFlip
{
    UIImageView * topView = [topViews lastObject];
    UIImageView * bottomView = [bottomViews objectAtIndex:0];
    [topViews removeLastObject];
    [bottomViews removeObjectAtIndex:0];
    [topViews insertObject:topView atIndex:0];
    [bottomViews addObject:bottomView];
    bottomView.frame = CGRectMake(0, self.frame.size.height / 2, self.frame.size.width, 0);
    [bottomView removeFromSuperview];
    [self addSubview:bottomView];
    
    [UIView animateWithDuration:0.25f delay:0 options:UIViewAnimationCurveEaseIn | UIViewAnimationOptionAllowUserInteraction animations:^{
        topView.frame = CGRectMake(0, self.frame.size.height / 2, self.frame.size.width, 0);
    } completion:^(BOOL finished) {
        [topView removeFromSuperview];
        topView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height / 2);
        [self insertSubview:topView atIndex:0];
        
        [UIView animateWithDuration:0.25f delay:0 options:UIViewAnimationCurveEaseOut | UIViewAnimationOptionAllowUserInteraction animations:^{
            bottomView.frame = CGRectMake(0, self.frame.size.height / 2, self.frame.size.width, self.frame.size.height / 2);
        } completion:nil];
    } ];
}

-(void)reset
{
    while (self.subviews.count > 0)
    {
        [[self.subviews lastObject] removeFromSuperview];
    }
    topViews = [NSMutableArray arrayWithCapacity:10];
    bottomViews = [NSMutableArray arrayWithCapacity:10];
    if (topParts == nil)
    {
        initParts();
    }
    for (int i = 0; i < 10; ++i)
    {
        UIImageView * topView = [[UIImageView alloc] initWithImage:[topParts objectAtIndex:i]];
        topView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height / 2);
        [self insertSubview:topView atIndex:0];
        [topViews insertObject:topView atIndex:0];
        
        UIImageView * bottomView = [[UIImageView alloc] initWithImage:[bottomParts objectAtIndex:i]];
        bottomView.frame = CGRectMake(0, self.frame.size.height / 2, self.frame.size.width, self.frame.size.height / 2);
        if (i == 0)
        {
            [self addSubview:bottomView];
            [bottomViews addObject:bottomView];
        }
        else
        {
            [self insertSubview:bottomView belowSubview:bottomViews.lastObject];
            [bottomViews insertObject:bottomView atIndex:bottomViews.count - 1];
        }
    }
}

-(void)flipNTimes:(uint)times
{
    if (times > 0)
    {
        [self animateFlip];
        [NSTimer scheduledTimerWithTimeInterval:0.0625f target:self selector:@selector(flipRecursive:) userInfo:[NSNumber numberWithUnsignedInt:times - 1] repeats:NO];
    }
}

-(void)flipRecursive:(id)sender
{
    NSTimer * timer = sender;
    uint times = [(NSNumber *)timer.userInfo unsignedIntValue];
    if (times > 0)
    {
        [self animateFlip];
        [NSTimer scheduledTimerWithTimeInterval:0.125f target:self selector:@selector(flipRecursive:) userInfo:[NSNumber numberWithUnsignedInt:times - 1] repeats:NO];
    }
}


@end
