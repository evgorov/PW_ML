//
//  PrizeWordSwitchView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/25/12.
//
//

#import "PrizeWordSwitchView.h"

@implementation PrizeWordSwitchView

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
    }
    return self;
}

- (IBAction)handleTapGesture:(id)sender
{
    if ([self isOn])
    {
        [UIView animateWithDuration:0.5 animations:^{
            onView.frame = CGRectMake(0, 0, 0, self.frame.size.height);
            offView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
            imgSlider.frame = CGRectMake(-imgSlider.frame.size.width / 4, imgSlider.frame.origin.y, imgSlider.frame.size.width, imgSlider.frame.size.height);
        }];
    }
    else
    {
        [UIView animateWithDuration:0.5 animations:^{
            onView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
            offView.frame = CGRectMake(self.frame.size.width, 0, 0, self.frame.size.height);
            imgSlider.frame = CGRectMake(self.frame.size.width - imgSlider.frame.size.width * 3 / 4, imgSlider.frame.origin.y, imgSlider.frame.size.width, imgSlider.frame.size.height);
        }];
    }
}
- (IBAction)handleSwipeRight:(id)sender
{
    if (![self isOn])
    {
        {
            [UIView animateWithDuration:0.5 animations:^{
                onView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
                offView.frame = CGRectMake(self.frame.size.width, 0, 0, self.frame.size.height);
                imgSlider.frame = CGRectMake(self.frame.size.width - imgSlider.frame.size.width * 3 / 4, imgSlider.frame.origin.y, imgSlider.frame.size.width, imgSlider.frame.size.height);
            }];
        }
    }
}

- (IBAction)handleSwipeLeft:(id)sender
{
    if ([self isOn])
    {
        [UIView animateWithDuration:0.5 animations:^{
            onView.frame = CGRectMake(0, 0, 0, self.frame.size.height);
            offView.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
            imgSlider.frame = CGRectMake(-imgSlider.frame.size.width / 4, imgSlider.frame.origin.y, imgSlider.frame.size.width, imgSlider.frame.size.height);
        }];
    }
}

-(BOOL)isOn
{
    return onView.frame.size.width > offView.frame.size.height;
}

+(PrizeWordSwitchView *)switchView
{
    return [[[NSBundle mainBundle] loadNibNamed:@"PrizeWordSwitchView" owner:self options:nil] objectAtIndex:0];
}

@end
