//
//  PuzzlesViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/15/12.
//
//

#import "PuzzlesViewController.h"
#import "BadgeView.h"
#import "EventManager.h"
#import "TileData.h"
#import "PrizeWordNavigationBar.h"
#import "RootViewController.h"
#import "AppDelegate.h"
#import "PuzzleSetView.h"

@interface PuzzlesViewController ()

-(void)handleBadgeClick:(id)sender;

@end

@implementation PuzzlesViewController

- (void)viewDidLoad
{
    [super viewDidLoad];

    PuzzleSetView * brilliantSet = [PuzzleSetView puzzleSetViewWithType:PUZZLESET_BRILLIANT puzzlesCount:17 minScore:10000000 price:3.99f];
    PuzzleSetView * goldSet = [PuzzleSetView puzzleSetViewWithType:PUZZLESET_GOLD puzzlesCount:12 minScore:10999 price:2.99f];
    PuzzleSetView * silverSet = [PuzzleSetView puzzleSetViewWithType:PUZZLESET_SILVER puzzlesCount:15 minScore:10000 price:1.99f];
    PuzzleSetView * silver2Set = [PuzzleSetView puzzleSetViewWithType:PUZZLESET_SILVER2 puzzlesCount:10 minScore:10000 price:1.99f];
    PuzzleSetView * freeSet = [PuzzleSetView puzzleSetViewWithType:PUZZLESET_FREE puzzlesCount:7 minScore:0 price:0];
    brilliantSet.frame = CGRectMake(0, currentPuzzlesView.frame.size.height, brilliantSet.frame.size.width, brilliantSet.frame.size.height);
    goldSet.frame = CGRectMake(0, brilliantSet.frame.origin.y + brilliantSet.frame.size.height, goldSet.frame.size.width, goldSet.frame.size.height);
    silverSet.frame = CGRectMake(0, goldSet.frame.origin.y + goldSet.frame.size.height, silverSet.frame.size.width, silverSet.frame.size.height);
    silver2Set.frame = CGRectMake(0, silverSet.frame.origin.y + silverSet.frame.size.height, silver2Set.frame.size.width, silver2Set.frame.size.height);
    freeSet.frame = CGRectMake(0, silver2Set.frame.origin.y + silver2Set.frame.size.height, freeSet.frame.size.width, freeSet.frame.size.height);
    [currentPuzzlesView addSubview:brilliantSet];
    [currentPuzzlesView addSubview:goldSet];
    [currentPuzzlesView addSubview:silverSet];
    [currentPuzzlesView addSubview:silver2Set];
    [currentPuzzlesView addSubview:freeSet];
    currentPuzzlesView.frame = CGRectMake(0, 0, currentPuzzlesView.frame.size.width, freeSet.frame.origin.y + freeSet.frame.size.height);
    
    [self addSimpleView:newsView];
    [self addFramedView:currentPuzzlesView];
    [self addFramedView:hintsView];
    [self addFramedView:archiveView];

    btnBuyHint1.titleLabel.font = [UIFont fontWithName:@"DINPro-Bold" size:15];
    btnBuyHint2.titleLabel.font = btnBuyHint1.titleLabel.font;
    btnBuyHint3.titleLabel.font = btnBuyHint1.titleLabel.font;
    
    currentGoldBadges = [NSMutableArray new];
    for (int i = 0; i != 0; ++i)
    {
        BadgeView * badgeView = [BadgeView badgeWithType:BADGE_GOLD andNumber:(i + 1) andPercent:((i + 1) * 25 / 100.0f)];
        badgeView.frame = CGRectMake(18 + (i % 4) * 70, 273 + (i / 4) * 105, badgeView.frame.size.width, badgeView.frame.size.height);
        [currentPuzzlesView addSubview:badgeView];
        [currentGoldBadges addObject:badgeView];
        [badgeView addTarget:self action:@selector(handleBadgeClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    
    currentSilverBadges = [NSMutableArray new];
    for (int i = 0; i != 0; ++i)
    {
        BadgeView * badgeView = [BadgeView badgeWithType:BADGE_SILVER andNumber:(i + 5) andPercent:(i * 25 / 100.0f)];
        badgeView.frame = CGRectMake(18 + (i % 4) * 70, 479 + (i / 4) * 105, badgeView.frame.size.width, badgeView.frame.size.height);
        [currentPuzzlesView addSubview:badgeView];
        [currentSilverBadges addObject:badgeView];
        [badgeView addTarget:self action:@selector(handleBadgeClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    
    archiveBadges = [NSMutableArray new];
    for (int i = 0; i != 12; ++i)
    {
        BadgeView * badgeView;
        if (i < 5)
        {
            badgeView = [BadgeView badgeWithType:BADGE_GOLD andNumber:(i + 1) andScore:(1000 + (rand() % 10000))];
        }
        else
        {
            badgeView = [BadgeView badgeWithType:BADGE_GOLD andNumber:(i + 1) andPercent:((rand() % 10000) / 10000.0f)];
        }
        badgeView.frame = CGRectMake(18 + (i % 4) * 70, 145 + (i / 4) * 105, badgeView.frame.size.width, badgeView.frame.size.height);
        [archiveView addSubview:badgeView];
        [archiveBadges addObject:badgeView];
        if (i >= 5)
        {
            [badgeView addTarget:self action:@selector(handleBadgeClick:) forControlEvents:UIControlEventTouchUpInside];
        }
    }
}

- (void)viewDidUnload
{
    newsView = nil;
    currentPuzzlesView = nil;
    for (UIImageView * badge in currentGoldBadges)
    {
        [badge removeFromSuperview];
    }
    for (UIImageView * badge in currentSilverBadges)
    {
        [badge removeFromSuperview];
    }
    for (UIImageView * badge in archiveBadges)
    {
        [badge removeFromSuperview];
    }
    [currentGoldBadges removeAllObjects];
    [currentSilverBadges removeAllObjects];
    [archiveBadges removeAllObjects];
    currentGoldBadges = nil;
    currentSilverBadges = nil;
    archiveBadges = nil;
    hintsView = nil;
    archiveView = nil;
    btnBuyHint1 = nil;
    btnBuyHint2 = nil;
    btnBuyHint3 = nil;
    setToBuyView = nil;
    [super viewDidUnload];
}

- (IBAction)handleNewsCloseClick:(id)sender
{
    newsView.autoresizesSubviews = NO;
    newsView.clipsToBounds = YES;
    [self resizeView:newsView newHeight:0 animated:YES];
}

-(void)handleBadgeClick:(id)sender
{
    BadgeView * badge = (BadgeView *)sender;
    
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_START andData:[NSNumber numberWithInt:(LetterType)badge.badgeType]]];
}

@end
