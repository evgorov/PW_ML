//
//  PrizeWordSwitchView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/25/12.
//
//

#import "PrizeWordSwitchView.h"
#import "AppDelegate.h"

@implementation PrizeWordSwitchView

@synthesize isOn;

-(id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        UITapGestureRecognizer * tapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapGesture:)];
        UISwipeGestureRecognizer * swipeLeftGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipeLeft:)];
        swipeLeftGestureRecognizer.direction = UISwipeGestureRecognizerDirectionLeft;
        UISwipeGestureRecognizer * swipeRightGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipeRight:)];
        swipeRightGestureRecognizer.direction = UISwipeGestureRecognizerDirectionRight;
        self.gestureRecognizers = [NSArray arrayWithObjects:tapGestureRecognizer, swipeLeftGestureRecognizer, swipeRightGestureRecognizer, nil];
        isOn = YES;
        self.backgroundColor = [UIColor clearColor];
        self.autoresizesSubviews = NO;
        self.clipsToBounds = NO;
    }
    return self;
}

- (IBAction)handleTapGesture:(id)sender
{
    if ([self isOn])
    {
        [self switchOffAnimated:YES];
    }
    else
    {
        [self switchOnAnimated:YES];
    }
    [self sendActionsForControlEvents:UIControlEventValueChanged];
}
- (IBAction)handleSwipeRight:(id)sender
{
    if (![self isOn])
    {
        [self switchOnAnimated:YES];
        [self sendActionsForControlEvents:UIControlEventValueChanged];
    }
}

- (IBAction)handleSwipeLeft:(id)sender
{
    if ([self isOn])
    {
        [self switchOffAnimated:YES];
        [self sendActionsForControlEvents:UIControlEventValueChanged];
    }
}

-(BOOL)isOn
{
    return isOn;
}

-(void)setOn:(BOOL)on animated:(BOOL)animated
{
    if (on)
    {
        [self switchOnAnimated:animated];
    }
    else
    {
        [self switchOffAnimated:animated];
    }
}

-(void)setEnabled:(BOOL)enabled
{
    [super setEnabled:enabled];
    float targetAlpha = enabled ? 1 : 0;
    [UIView animateWithDuration:0.3f delay:0 options:UIViewAnimationOptionAllowAnimatedContent|UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionBeginFromCurrentState|UIViewAnimationOptionCurveLinear animations:^{
        imgSlider.alpha = targetAlpha;
        offView.alpha = targetAlpha;
        onView.alpha = targetAlpha;
        imgDisabled.alpha = targetAlpha == 0 ? 0.3 : 0;
    } completion:nil];
}

-(void)switchOnAnimated:(BOOL)animated
{
    if (isOn)
    {
        return;
    }
    isOn = YES;
    float sliderX = self.frame.size.width - imgSlider.frame.size.width;
    if (animated)
    {
        [UIView animateWithDuration:0.2 delay:0 options:UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionBeginFromCurrentState|UIViewAnimationOptionCurveLinear animations:^{
            onView.frame = CGRectIntegral(CGRectMake(0, 0, self.frame.size.width, self.frame.size.height));
            offView.frame = CGRectIntegral(CGRectMake(self.frame.size.width, 0, 0, self.frame.size.height));
            imgSlider.frame = CGRectIntegral(CGRectMake(sliderX, imgSlider.frame.origin.y, imgSlider.frame.size.width, imgSlider.frame.size.height));
        } completion:nil];
    }
    else
    {
        onView.frame = CGRectIntegral(CGRectMake(0, 0, self.frame.size.width, self.frame.size.height));
        offView.frame = CGRectIntegral(CGRectMake(self.frame.size.width, 0, 0, self.frame.size.height));
        imgSlider.frame = CGRectIntegral(CGRectMake(sliderX, imgSlider.frame.origin.y, imgSlider.frame.size.width, imgSlider.frame.size.height));
    }
}

-(void)switchOffAnimated:(BOOL)animated
{
    if (!isOn)
    {
        return;
    }
    isOn = NO;
    if (animated)
    {
        [UIView animateWithDuration:0.2 delay:0 options:UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionBeginFromCurrentState|UIViewAnimationOptionCurveLinear animations:^{
            onView.frame = CGRectIntegral(CGRectMake(0, 0, 0, self.frame.size.height));
            offView.frame = CGRectIntegral(CGRectMake(0, 0, self.frame.size.width, self.frame.size.height));
            imgSlider.frame = CGRectIntegral(CGRectMake(0, imgSlider.frame.origin.y, imgSlider.frame.size.width, imgSlider.frame.size.height));
        } completion:nil];
    }
    else
    {
        onView.frame = CGRectIntegral(CGRectMake(0, 0, 0, self.frame.size.height));
        offView.frame = CGRectIntegral(CGRectMake(0, 0, self.frame.size.width, self.frame.size.height));
        imgSlider.frame = CGRectIntegral(CGRectMake(0, imgSlider.frame.origin.y, imgSlider.frame.size.width, imgSlider.frame.size.height));
    }
}

+(PrizeWordSwitchView *)switchView
{
    return [[[NSBundle mainBundle] loadNibNamed:@"PrizeWordSwitchView" owner:self options:nil] objectAtIndex:0];
}

@end
