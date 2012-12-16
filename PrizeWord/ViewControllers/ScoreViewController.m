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
#import "InviteViewController.h"

@interface ScoreViewController (private)

-(void)handleMenuClick:(id)sender;

@end

@implementation ScoreViewController

-(void)viewDidLoad
{
    [super viewDidLoad];
    [self addFramedView:puzzlesView];
    [self addFramedView:invitesView];
    
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
    puzzlesView = nil;
    invitesView = nil;
    btnInvite = nil;
    badges = nil;
    [super viewDidUnload];
}

- (IBAction)handleInviteClick:(id)sender
{
    UINavigationController * navController = self.navigationController;
    [navController popViewControllerAnimated:NO];
    [navController pushViewController:[InviteViewController new] animated:YES];
}

@end
