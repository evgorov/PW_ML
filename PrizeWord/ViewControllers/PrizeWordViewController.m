//
//  PrizeWordViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/11/12.
//
//

#import "PrizeWordViewController.h"
#import "PrizeWordNavigationBar.h"

@interface PrizeWordViewController ()

@end

@implementation PrizeWordViewController

-(void)viewDidLoad
{
    [super viewDidLoad];
    activityIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    activityIndicator.hidesWhenStopped = YES;
    activityIndicator.userInteractionEnabled = NO;
    CGRect frame = activityIndicator.frame;
    frame.origin = CGPointMake(self.view.frame.size.width / 2 - frame.size.width / 2, self.view.frame.size.height / 2 - frame.size.height / 2);
    activityIndicator.frame = frame;
    [self.view addSubview:activityIndicator];
}

-(void)viewDidUnload
{
    [activityIndicator removeFromSuperview];
    activityIndicator = nil;
    [super viewDidUnload];
}

-(void)setTitle:(NSString *)title
{
    [super setTitle:title];
    UINavigationItem * navigationItem = self.navigationItem;
    UIFont * titleFont = [UIFont fontWithName:@"DINPro-Black" size:18];
    CGSize titleSize = [title sizeWithFont:titleFont];
    UILabel * titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, titleSize.width, titleSize.height)];
    titleLabel.font = titleFont;
    titleLabel.text = title;
    titleLabel.backgroundColor = [UIColor clearColor];
    titleLabel.textColor = [UIColor whiteColor];
    titleLabel.shadowColor = [UIColor colorWithWhite:0 alpha:0.5];
    titleLabel.shadowOffset = CGSizeMake(0, 1.5f);
    [navigationItem setTitleView:[PrizeWordNavigationBar containerWithView:titleLabel]];
}

-(void)showActivityIndicator
{
    [self.view bringSubviewToFront:activityIndicator];
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
    return toInterfaceOrientation == UIInterfaceOrientationPortrait;
}

-(BOOL)shouldAutorotate
{
    return NO;
}

-(void)orientationChanged:(UIDeviceOrientation)orientation
{
}

@end
