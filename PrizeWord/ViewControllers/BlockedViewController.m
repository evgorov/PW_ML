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
#import "PrizeWordButton.h"

@interface BlockedViewController (private)

-(void)handleMenuClick:(id)sender;
-(void)handleSwipeLeft:(id)sender;
-(void)handleSwipeRight:(id)sender;

@end

@implementation BlockedViewController

static int VERTICAL_SPACE = 23;

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
    scrollView.autoresizesSubviews = YES;
    scrollView.clipsToBounds = YES;
    scrollView.bounces = YES;
    scrollView.showsHorizontalScrollIndicator = NO;
    scrollView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_dark_tile.jpg"]];
    
    if ([[UIDevice currentDevice].systemVersion compare:@"7.0" options:NSNumericSearch] != NSOrderedAscending)
    {
        scrollView.contentInset = UIEdgeInsetsMake([AppDelegate currentDelegate].isIPad ? 72 : 60, 0, 0, 0);
        scrollView.scrollIndicatorInsets = scrollView.contentInset;
    }
    
    
    [self.view addSubview:scrollView];
    
    contentView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, scrollView.frame.size.width, 0)];
    contentView.autoresizesSubviews = NO;
    contentView.clipsToBounds = YES;
    contentView.backgroundColor = [UIColor clearColor];
    [scrollView addSubview:contentView];
/*
    UISwipeGestureRecognizer * swipeLeftGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipeLeft:)];
    swipeLeftGestureRecognizer.direction = UISwipeGestureRecognizerDirectionLeft;
    UISwipeGestureRecognizer * swipeRightGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipeRight:)];
    swipeRightGestureRecognizer.direction = UISwipeGestureRecognizerDirectionRight;
    self.view.gestureRecognizers = [NSArray arrayWithObjects:swipeLeftGestureRecognizer, swipeRightGestureRecognizer, nil];
*/
    if ([AppDelegate currentDelegate].isIPad)
    {
        VERTICAL_SPACE = 27;
    }
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
    CGRect frameRect = scrollView.frame;
    frameRect.size.height = self.view.frame.size.height;
    scrollView.frame = frameRect;
    
    if (![AppDelegate currentDelegate].isIPad)
    {
        UIImage * menuImage = [UIImage imageNamed:@"menu_btn"];
        UIImage * menuHighlightedImage = [UIImage imageNamed:@"menu_btn_down"];
        PrizeWordButton * menuButton = [[PrizeWordButton alloc] initWithFrame:CGRectMake(0, 0, menuImage.size.width, menuImage.size.height)];
        [menuButton setBackgroundImage:menuImage forState:UIControlStateNormal];
        [menuButton setBackgroundImage:menuHighlightedImage forState:UIControlStateHighlighted];
        [menuButton addTarget:self action:@selector(handleMenuClick:) forControlEvents:UIControlEventTouchUpInside];
        menuItem = [[UIBarButtonItem alloc] initWithCustomView:
                    [PrizeWordNavigationBar containerWithView:menuButton]];
        [self.navigationItem setLeftBarButtonItem:menuItem animated:animated];
    }
    else
    {
        [self.navigationItem setLeftBarButtonItem:[[UIBarButtonItem alloc] initWithCustomView:
                                                   [UIView new]] animated:NO];
        [[AppDelegate currentDelegate].rootViewController showMenuAnimated:animated];
    }
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

-(void)handleSwipeLeft:(id)sender
{
    RootViewController * rootViewController = [AppDelegate currentDelegate].rootViewController;
    if (!rootViewController.isMenuHidden)
    {
        [rootViewController hideMenuAnimated:YES];
    }
}

-(void)handleSwipeRight:(id)sender
{
    RootViewController * rootViewController = [AppDelegate currentDelegate].rootViewController;
    if (rootViewController.isMenuHidden)
    {
        [rootViewController showMenuAnimated:YES];
    }
}


-(void)addFramedView:(UIView *)view
{
    int contentHeight = contentView.frame.size.height;
    float frameOffset = (self.view.frame.size.width - view.frame.size.width) / 2;
    view.frame = CGRectIntegral(CGRectMake(frameOffset, contentHeight + VERTICAL_SPACE / 2, view.frame.size.width, view.frame.size.height));
    
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
    contentView.frame = CGRectIntegral(CGRectMake(0, 0, self.view.frame.size.width, contentHeight));
    scrollView.contentSize = contentView.frame.size;
}

-(void)addSimpleView:(UIView *)view
{
    int contentHeight = contentView.frame.size.height;
    int frameOffset = (self.view.frame.size.width - view.frame.size.width) / 2;
    view.frame = CGRectIntegral(CGRectMake(frameOffset, contentHeight + VERTICAL_SPACE / 2, view.frame.size.width, view.frame.size.height));
    
    [contentView addSubview:view];
    contentHeight += view.frame.size.height + VERTICAL_SPACE;
    contentView.frame = CGRectIntegral(CGRectMake(0, 0, self.view.frame.size.width, contentHeight));
    scrollView.contentSize = contentView.frame.size;
}

-(void)removeFramedView:(UIView *)view
{
    int yOffset = 0;
    
    for (UIView * subview in contentView.subviews)
    {
        if (subview == view)
        {
            yOffset = -subview.frame.size.height - VERTICAL_SPACE;
            for (UIView * innerView in subview.subviews)
            {
                if ([innerView isKindOfClass:[UIImageView class]])
                {
                    UIImageView * imageView = (UIImageView *)innerView;
                    if (imageView.frame.size.height > subview.frame.size.height)
                    {
                        [imageView removeFromSuperview];
                        break;
                    }
                }
            }
        }
        else if (yOffset != 0)
        {
            subview.frame = CGRectIntegral(CGRectMake(subview.frame.origin.x, subview.frame.origin.y + yOffset, subview.frame.size.width, subview.frame.size.height));
        }
    }
    
    if (yOffset != 0)
    {
        contentView.frame = CGRectIntegral(CGRectMake(contentView.frame.origin.x, contentView.frame.origin.y, contentView.frame.size.width, contentView.frame.size.height + yOffset));
        scrollView.contentSize = contentView.frame.size;
        [view removeFromSuperview];
    }
}

-(void)removeSimpleView:(UIView *)view
{
    int yOffset = 0;

    for (UIView * subview in contentView.subviews)
    {
        if (subview == view)
        {
            yOffset = -subview.frame.size.height - VERTICAL_SPACE;
        }
        else if (yOffset != 0)
        {
            subview.frame = CGRectIntegral(CGRectMake(subview.frame.origin.x, subview.frame.origin.y + yOffset, subview.frame.size.width, subview.frame.size.height));
        }
    }
    
    if (yOffset != 0)
    {
        contentView.frame = CGRectIntegral(CGRectMake(contentView.frame.origin.x, contentView.frame.origin.y, contentView.frame.size.width, contentView.frame.size.height + yOffset));
        scrollView.contentSize = contentView.frame.size;
        [view removeFromSuperview];
    }
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
            UIImageView * borderView = nil;
            if ([[view.subviews objectAtIndex:view.subviews.count - 1] isKindOfClass:[UIImageView class]])
            {
                borderView = [view.subviews objectAtIndex:view.subviews.count - 1];
            }
            if (animated)
            {
                [UIView animateWithDuration:0.3 animations:^{
                    if (borderView != nil)
                    {
                        borderView.frame = CGRectIntegral(CGRectMake(borderView.frame.origin.x, borderView.frame.origin.y, borderView.frame.size.width, borderView.frame.size.height + delta));
                    }
                    subview.frame = CGRectIntegral(CGRectMake(subview.frame.origin.x, subview.frame.origin.y, subview.frame.size.width, height));
                }];
            }
            else
            {
                if (borderView != nil)
                {
                    borderView.frame = CGRectIntegral(CGRectMake(borderView.frame.origin.x, borderView.frame.origin.y, borderView.frame.size.width, borderView.frame.size.height + delta));
                }
                subview.frame = CGRectIntegral(CGRectMake(subview.frame.origin.x, subview.frame.origin.y, subview.frame.size.width, height));
            }
        }
        else if (delta != 0)
        {
            if (animated)
            {
                [UIView animateWithDuration:0.3 animations:^{
                    subview.frame = CGRectIntegral(CGRectMake(subview.frame.origin.x, subview.frame.origin.y + delta, subview.frame.size.width, subview.frame.size.height));
                }];
            }
            else
            {
                subview.frame = CGRectIntegral(CGRectMake(subview.frame.origin.x, subview.frame.origin.y + delta, subview.frame.size.width, subview.frame.size.height));
            }
        }
    }
    if (animated)
    {
        [UIView animateWithDuration:0.3 animations:^{
            contentView.frame = CGRectIntegral(CGRectMake(contentView.frame.origin.x, contentView.frame.origin.y, contentView.frame.size.width, contentView.frame.size.height + delta));
        }];
    }
    else
    {
        contentView.frame = CGRectIntegral(CGRectMake(contentView.frame.origin.x, contentView.frame.origin.y, contentView.frame.size.width, contentView.frame.size.height + delta));
    }
    scrollView.contentSize = CGSizeMake(scrollView.contentSize.width, scrollView.contentSize.height + delta);
}

@end
