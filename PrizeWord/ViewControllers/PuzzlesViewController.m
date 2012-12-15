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

@interface PuzzlesViewController ()

-(void)handleBadgeClick:(id)sender;

@end

@implementation PuzzlesViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
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
    currentPuzzlesView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_sand_tile.jpg"]];
    hintsView.backgroundColor = currentPuzzlesView.backgroundColor;
    archiveView.backgroundColor = currentPuzzlesView.backgroundColor;
    currentPuzzlesBorder.image = border;
    hintsBorder.image = border;
    archiveBorder.image = border;
    scrollView.contentSize = contentView.frame.size;
    
    currentGoldBadges = [NSMutableArray new];
    for (int i = 0; i != 4; ++i)
    {
        BadgeView * badgeView = [BadgeView badgeWithType:BADGE_GOLD andNumber:(i + 1) andPercent:((i + 1) * 25 / 100.0f)];
        badgeView.frame = CGRectMake(18 + (i % 4) * 70, 273 + (i / 4) * 105, badgeView.frame.size.width, badgeView.frame.size.height);
        [currentPuzzlesView addSubview:badgeView];
        [currentGoldBadges addObject:badgeView];
        [badgeView addTarget:self action:@selector(handleBadgeClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    
    currentSilverBadges = [NSMutableArray new];
    for (int i = 0; i != 4; ++i)
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

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewDidUnload {
    scrollView = nil;
    contentView = nil;
    newsView = nil;
    currentPuzzlesView = nil;
    currentPuzzlesBorder = nil;
    for (UIImageView * badge in currentGoldBadges) {
        [badge removeFromSuperview];
    }
    for (UIImageView * badge in currentSilverBadges) {
        [badge removeFromSuperview];
    }
    for (UIImageView * badge in archiveBadges) {
        [badge removeFromSuperview];
    }
    [currentGoldBadges removeAllObjects];
    [currentSilverBadges removeAllObjects];
    [archiveBadges removeAllObjects];
    currentGoldBadges = nil;
    currentSilverBadges = nil;
    archiveBadges = nil;
    hintsView = nil;
    hintsBorder = nil;
    archiveView = nil;
    archiveBorder = nil;
    [super viewDidUnload];
}

- (IBAction)handleNewsCloseClick:(id)sender
{
}

-(void)handleBadgeClick:(id)sender
{
    BadgeView * badge = (BadgeView *)sender;
    
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_START andData:[NSNumber numberWithInt:(LetterType)badge.badgeType]]];
    
}
@end
