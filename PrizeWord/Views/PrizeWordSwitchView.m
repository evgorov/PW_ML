//
//  PrizeWordSwitchView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/25/12.
//
//

#import "PrizeWordSwitchView.h"

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
}
- (IBAction)handleSwipeRight:(id)sender
{
    if (![self isOn])
    {
        [self switchOnAnimated:YES];
    }
}

- (IBAction)handleSwipeLeft:(id)sender
{
    if ([self isOn])
    {
        [self switchOffAnimated:YES];
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

-(void)switchOnAnimated:(BOOL)animated
{
    if (isOn)
    {
        return;
    }
    isOn = YES;
    if (animated)
    {
        [UIView animateWithDuration:0.5 delay:0 options:UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionBeginFromCurrentState|UIViewAnimationOptionCurveLinear animations:^{
            onView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
            offView.frame = CGRectMake(self.frame.size.width, 0, 0, self.frame.size.height);
            imgSlider.frame = CGRectMake(self.frame.size.width - imgSlider.frame.size.width * 3 / 4, imgSlider.frame.origin.y, imgSlider.frame.size.width, imgSlider.frame.size.height);
        } completion:nil];
    }
    else
    {
        onView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
        offView.frame = CGRectMake(self.frame.size.width, 0, 0, self.frame.size.height);
        imgSlider.frame = CGRectMake(self.frame.size.width - imgSlider.frame.size.width * 3 / 4, imgSlider.frame.origin.y, imgSlider.frame.size.width, imgSlider.frame.size.height);
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
        [UIView animateWithDuration:0.5 delay:0 options:UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionBeginFromCurrentState|UIViewAnimationOptionCurveLinear animations:^{
            onView.frame = CGRectMake(0, 0, 0, self.frame.size.height);
            offView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
            imgSlider.frame = CGRectMake(-imgSlider.frame.size.width / 4, imgSlider.frame.origin.y, imgSlider.frame.size.width, imgSlider.frame.size.height);
        } completion:nil];
    }
    else
    {
        onView.frame = CGRectMake(0, 0, 0, self.frame.size.height);
        offView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
        imgSlider.frame = CGRectMake(-imgSlider.frame.size.width / 4, imgSlider.frame.origin.y, imgSlider.frame.size.width, imgSlider.frame.size.height);
    }
}

+(PrizeWordSwitchView *)switchView
{
    return [[[NSBundle mainBundle] loadNibNamed:@"PrizeWordSwitchView" owner:self options:nil] objectAtIndex:0];
}

@end
