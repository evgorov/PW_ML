//
//  PrizeWordViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/11/12.
//
//

#import "PrizeWordViewController.h"

@interface PrizeWordViewController ()

@end

@implementation PrizeWordViewController

-(id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        NSLog(@"nib: %@", nibNameOrNil);
        activityIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
        activityIndicator.hidesWhenStopped = YES;
        activityIndicator.userInteractionEnabled = NO;
        activityIndicator.center = CGPointMake(self.view.frame.size.width / 2, self.view.frame.size.height / 2);
        [self.view addSubview:activityIndicator];
    }
    return self;
}

-(void)dealloc
{
    [activityIndicator removeFromSuperview];
    activityIndicator = nil;
}

-(void)showActivityIndicator
{
    [activityIndicator startAnimating];
    self.view.userInteractionEnabled = NO;
}

-(void)hideActivityIndicator
{
    [activityIndicator stopAnimating];
    self.view.userInteractionEnabled = YES;
}

-(BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
    return toInterfaceOrientation == UIInterfaceOrientationPortrait || toInterfaceOrientation == UIInterfaceOrientationPortraitUpsideDown;
}

-(BOOL)shouldAutorotate
{
    return YES;
}

-(NSUInteger)supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskPortrait | UIInterfaceOrientationMaskPortraitUpsideDown;
}

@end
