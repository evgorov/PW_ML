//
//  ScoreViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import "ScoreViewController.h"
#import "BadgeView.h"
#import "PrizeWordNavigationBar.h"
#import "AppDelegate.h"
#import "RootViewController.h"

@interface ScoreViewController (private)

-(void)handleMenuClick:(id)sender;

@end

@implementation ScoreViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

-(void)viewDidLoad
{
    UIImage * border = [UIImage imageNamed:@"frame_border"];
    if ([border respondsToSelector:@selector(resizableImageWithCapInsets:)])
    {
        border = [border resizableImageWithCapInsets:UIEdgeInsetsMake(border.size.height / 2 - 1, border.size.width / 2 - 1, border.size.height / 2, border.size.width / 2)];
    }
    else
    {
        border = [border stretchableImageWithLeftCapWidth:(border.size.width / 2 - 1) topCapHeight:(border.size.height / 2 - 1)];
    }
    
    contentView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_dark_tile.jpg"]];
    puzzlesView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_sand_tile.jpg"]];
    invitesView.backgroundColor = puzzlesView.backgroundColor;
    puzzlesBorder.image = border;
    invitesBorder.image = border;
    scrollView.contentSize = contentView.frame.size;
    
    badges = [NSMutableArray new];
    for (int i = 0; i != 8; ++i)
    {
        BadgeView * badgeView = [BadgeView badgeWithType:BADGE_GOLD andNumber:(i + 1) andScore:(1000 + (rand() % 10000))];
        badgeView.frame = CGRectMake(18 + (i % 4) * 70, 73 + (i / 4) * 105, badgeView.frame.size.width, badgeView.frame.size.height);
        [puzzlesView addSubview:badgeView];
        [badges addObject:badgeView];
    }
    
    for (int i = 0; i != 4; ++i)
    {
        BadgeView * badgeView = [BadgeView badgeWithType:BADGE_SILVER andNumber:(i + 1) andScore:(1000 + (rand() % 10000))];
        badgeView.frame = CGRectMake(18 + (i % 4) * 70, 373 + (i / 4) * 105, badgeView.frame.size.width, badgeView.frame.size.height);
        [puzzlesView addSubview:badgeView];
        [badges addObject:badgeView];
    }
    
    for (int i = 0; i != 2; ++i)
    {
        BadgeView * badgeView = [BadgeView badgeWithType:BADGE_FREE andNumber:(i + 1) andScore:(1000 + (rand() % 10000))];
        badgeView.frame = CGRectMake(18 + (i % 4) * 70, 573 + (i / 4) * 105, badgeView.frame.size.width, badgeView.frame.size.height);
        [puzzlesView addSubview:badgeView];
        [badges addObject:badgeView];
    }
}

- (void)viewDidUnload
{
    scrollView = nil;
    contentView = nil;
    puzzlesView = nil;
    puzzlesBorder = nil;
    invitesView = nil;
    invitesBorder = nil;
    btnInvite = nil;
    for (UIImageView * badge in badges)
    {
        [badge removeFromSuperview];
    }
    [badges removeAllObjects];
    badges = nil;
    [super viewDidUnload];
}

-(void)viewWillAppear:(BOOL)animated
{
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

- (IBAction)handleInviteClick:(id)sender
{
}

@end
