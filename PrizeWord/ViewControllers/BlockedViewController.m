//
//  BlockedViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import "BlockedViewController.h"
#import "PrizeWordNavigationBar.h"
#import "RootViewController.h"
#import "AppDelegate.h"

@interface BlockedViewController (private)

-(void)handleMenuClick:(id)sender;

@end

@implementation BlockedViewController

static const int VERTICAL_SPACE = 20;

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
    scrollView.autoresizesSubviews = YES;
    scrollView.clipsToBounds = YES;
    scrollView.bounces = YES;
    scrollView.showsHorizontalScrollIndicator = NO;
    scrollView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_dark_tile.jpg"]];
    [self.view addSubview:scrollView];
    
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, scrollView.frame.size.width, 0)];
    contentView.autoresizesSubviews = NO;
    contentView.clipsToBounds = YES;
    contentView.backgroundColor = [UIColor clearColor];
    [scrollView addSubview:contentView];
}

-(void)viewDidUnload
{
    contentView = nil;
    scrollView = nil;
    blockViews = nil;
    [super viewDidUnload];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    UIImage * menuImage = [UIImage imageNamed:@"menu_btn"];
    UIImage * menuHighlightedImage = [UIImage imageNamed:@"menu_btn_down"];
    UIButton * menuButton = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, menuImage.size.width, menuImage.size.height)];
    [menuButton setBackgroundImage:menuImage forState:UIControlStateNormal];
    [menuButton setBackgroundImage:menuHighlightedImage forState:UIControlStateHighlighted];
    [menuButton addTarget:self action:@selector(handleMenuClick:) forControlEvents:UIControlEventTouchUpInside];
    menuItem = [[UIBarButtonItem alloc] initWithCustomView:
                [PrizeWordNavigationBar containerWithView:menuButton]];
    [self.navigationItem setLeftBarButtonItem:menuItem animated:animated];
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    menuItem = nil;
}


-(void)handleMenuClick:(id)sender
{
    RootViewController * rootViewController = [AppDelegate currentDelegate].rootViewController;
    if (rootViewController.isMenuHidden)
    {
        [rootViewController showMenuAnimated:YES];
    }
    else
    {
        [rootViewController hideMenuAnimated:YES];
    }
}


-(void)addFramedView:(UIView *)view
{
    int contentHeight = contentView.frame.size.height;
    float frameOffset = (self.view.frame.size.width - view.frame.size.width) / 2;
    view.frame = CGRectMake(frameOffset, contentHeight + VERTICAL_SPACE / 2, view.frame.size.width, view.frame.size.height);
    
    UIImage * border = [UIImage imageNamed:@"frame_border"];
    if ([border respondsToSelector:@selector(resizableImageWithCapInsets:)])
    {
        border = [border resizableImageWithCapInsets:UIEdgeInsetsMake(border.size.height / 2 - 1, border.size.width / 2 - 1, border.size.height / 2, border.size.width / 2)];
    }
    else
    {
        border = [border stretchableImageWithLeftCapWidth:(border.size.width / 2 - 1) topCapHeight:(border.size.height / 2 - 1)];
    }
    
    UIImageView * borderView = [[UIImageView alloc] initWithFrame:CGRectMake(-frameOffset, -frameOffset * 1.2f, self.view.frame.size.width, view.frame.size.height + frameOffset * 2.4f)];
    borderView.image = border;
    view.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_sand_tile.jpg"]];
    view.clipsToBounds = NO;
    view.autoresizesSubviews = YES;
    [view addSubview:borderView];
    [contentView addSubview:view];
    [blockViews addObject:view];
    contentHeight += view.frame.size.height + VERTICAL_SPACE;
    contentView.frame = CGRectMake(0, 0, self.view.frame.size.width, contentHeight);
    scrollView.contentSize = contentView.frame.size;
}

-(void)addSimpleView:(UIView *)view
{
    int contentHeight = contentView.frame.size.height;
    float frameOffset = (self.view.frame.size.width - view.frame.size.width) / 2;
    view.frame = CGRectMake(frameOffset, contentHeight + VERTICAL_SPACE / 2, view.frame.size.width, view.frame.size.height);
    
    view.clipsToBounds = NO;
    view.autoresizesSubviews = YES;
    [contentView addSubview:view];
    [blockViews addObject:view];
    contentHeight += view.frame.size.height + VERTICAL_SPACE;
    contentView.frame = CGRectMake(0, 0, self.view.frame.size.width, contentHeight);
    scrollView.contentSize = contentView.frame.size;
}

@end
