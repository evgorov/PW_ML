//
//  PrizeWordNavigationController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/5/12.
//
//

#import "PrizeWordNavigationController.h"
#import "PrizeWordNavigationBar.h"

@interface PrizeWordNavigationController ()

-(UIBarButtonItem *)backButtonItem;
-(void)handleBackTap:(UIButton *)sender;

@end

@implementation PrizeWordNavigationController

+(void)setTitleViewForViewController:(UIViewController *)viewController
{
    if (viewController.title != nil && viewController.title.length != 0)
    {
        UIFont * titleFont = [UIFont fontWithName:@"DINPro-Black" size:18];
        CGSize titleSize = [viewController.title sizeWithFont:titleFont];
        UILabel * titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, titleSize.width, titleSize.height)];
        titleLabel.font = titleFont;
        titleLabel.text = viewController.title;
        titleLabel.backgroundColor = [UIColor clearColor];
        titleLabel.textColor = [UIColor whiteColor];
        titleLabel.shadowColor = [UIColor colorWithWhite:0 alpha:0.5];
        titleLabel.shadowOffset = CGSizeMake(0, 1.5f);
        [viewController.navigationItem setTitleView:[PrizeWordNavigationBar containerWithView:titleLabel]];
    }
}

-(id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        self.delegate = self;
    }
    return self;
}

-(UIBarButtonItem *)backButtonItem
{
    UIImage * backButtonBg = [UIImage imageNamed:@"nav_back_btn"];
    UIImage * backButtonDownBg = [UIImage imageNamed:@"nav_back_btn_down"];
    UIButton * backButton = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, backButtonBg.size.width, backButtonBg.size.height)];
    [backButton setBackgroundImage:backButtonBg forState:UIControlStateNormal];
    [backButton setBackgroundImage:backButtonDownBg forState:UIControlStateHighlighted];
    [backButton addTarget:self action:@selector(handleBackTap:) forControlEvents:UIControlEventTouchUpInside];
    return [[UIBarButtonItem alloc] initWithCustomView:[PrizeWordNavigationBar containerWithView:backButton]];
}

-(void)pushViewController:(UIViewController *)viewController animated:(BOOL)animated
{
    [PrizeWordNavigationController setTitleViewForViewController:viewController];
    [super pushViewController:viewController animated:animated];
    if (viewController.navigationItem.leftBarButtonItem == nil) {
        [viewController.navigationItem setLeftBarButtonItem:self.backButtonItem animated:animated];
    }
}

-(NSArray *)popToRootViewControllerAnimated:(BOOL)animated
{
    NSArray * popped = [super popToRootViewControllerAnimated:animated];
    
    if (self.topViewController.navigationItem.leftBarButtonItem == nil)
    {
        [self.topViewController.navigationItem setLeftBarButtonItem:self.backButtonItem animated:animated];
    }
    [self setNavigationBarHidden:YES animated:animated];
    return popped;
}

-(void)navigationController:(UINavigationController *)navigationController didShowViewController:(UIViewController *)viewController animated:(BOOL)animated
{
    if ([[UIDevice currentDevice].systemVersion compare:@"5.0" options:NSNumericSearch] == NSOrderedAscending)
    {
        [viewController viewDidAppear:animated];
    }
}

-(void)navigationController:(UINavigationController *)navigationController willShowViewController:(UIViewController *)viewController animated:(BOOL)animated
{
    if ([[UIDevice currentDevice].systemVersion compare:@"5.0" options:NSNumericSearch] == NSOrderedAscending)
    {
        [viewController viewWillAppear:animated];
    }
}

-(void)handleBackTap:(UIButton *)sender
{
    [self popViewControllerAnimated:YES];
}


-(BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
    return [self.topViewController shouldAutorotateToInterfaceOrientation:toInterfaceOrientation];

}

-(BOOL)shouldAutorotate
{
    return [self.topViewController shouldAutorotate];
}

-(NSUInteger)supportedInterfaceOrientations
{
    return [self.topViewController supportedInterfaceOrientations];
}

@end
