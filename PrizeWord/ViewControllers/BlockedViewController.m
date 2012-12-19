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
    view.autoresizesSubviews = NO;
    [view addSubview:borderView];
    [contentView addSubview:view];
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
    contentHeight += view.frame.size.height + VERTICAL_SPACE;
    contentView.frame = CGRectMake(0, 0, self.view.frame.size.width, contentHeight);
    scrollView.contentSize = contentView.frame.size;
}

-(void)resizeView:(UIView *)view newHeight:(float)height
{
    [self resizeView:view newHeight:height animated:NO];
}

-(void)resizeView:(UIView *)view newHeight:(float)height animated:(BOOL)animated
{
    if (height == view.frame.size.height)
    {
        return;
    }
    float delta = 0;
    for (UIView * subview in contentView.subviews)
    {
        if (subview == view)
        {
            delta = height - subview.frame.size.height;
            if (height == 0)
            {
                delta -= VERTICAL_SPACE;
            }
            else if (subview.frame.size.height == 0)
            {
                delta += VERTICAL_SPACE;
            }
            if (animated)
            {
                [UIView animateWithDuration:0.3 animations:^{
                    subview.frame = CGRectMake(subview.frame.origin.x, subview.frame.origin.y, subview.frame.size.width, height);
                }];
            }
            else
            {
                subview.frame = CGRectMake(subview.frame.origin.x, subview.frame.origin.y, subview.frame.size.width, height);
            }
        }
        else if (delta != 0)
        {
            if (animated)
            {
                [UIView animateWithDuration:0.3 animations:^{
                    subview.frame = CGRectMake(subview.frame.origin.x, subview.frame.origin.y + delta, subview.frame.size.width, subview.frame.size.height);
                }];
            }
            else
            {
                subview.frame = CGRectMake(subview.frame.origin.x, subview.frame.origin.y + delta, subview.frame.size.width, subview.frame.size.height);
            }
        }
    }
    if (animated)
    {
        [UIView animateWithDuration:0.3 animations:^{
            contentView.frame = CGRectMake(contentView.frame.origin.x, contentView.frame.origin.y, contentView.frame.size.width, contentView.frame.size.height + delta);
        }];
    }
    else
    {
        contentView.frame = CGRectMake(contentView.frame.origin.x, contentView.frame.origin.y, contentView.frame.size.width, contentView.frame.size.height + delta);
    }
    scrollView.contentSize = CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height + delta);
}

@end
