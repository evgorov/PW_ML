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
#import "PuzzleSetView.h"

@interface PuzzlesViewController ()

-(void)handleBadgeClick:(id)sender;
-(void)handleBuyClick:(id)sender;
-(void)activateBadges:(NSArray *)badges;

-(void)handleNewsPrev:(id)sender;
-(void)handleNewsNext:(id)sender;
-(void)handleNewsTap:(id)sender;

@end

@implementation PuzzlesViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.title = @"Сканворды";
    
    UISwipeGestureRecognizer * newsRightGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleNewsPrev:)];
    newsRightGestureRecognizer.direction = UISwipeGestureRecognizerDirectionRight;
    UISwipeGestureRecognizer * newsLeftGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleNewsNext:)];
    newsLeftGestureRecognizer.direction = UISwipeGestureRecognizerDirectionLeft;
    UITapGestureRecognizer * newsTapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleNewsTap:)];
    [newsScrollView setGestureRecognizers:[NSArray arrayWithObjects:newsLeftGestureRecognizer, newsRightGestureRecognizer, newsTapGestureRecognizer, nil]];

    PuzzleSetView * brilliantSet = [PuzzleSetView puzzleSetViewWithType:PUZZLESET_BRILLIANT puzzlesCount:17 minScore:10000000 price:3.99f];
    PuzzleSetView * goldSet = [PuzzleSetView puzzleSetViewWithType:PUZZLESET_GOLD puzzlesCount:12 puzzlesSolved:7 score:27000 ids:[NSArray arrayWithObjects:[NSNumber numberWithInt:1], [NSNumber numberWithInt:4], [NSNumber numberWithInt:5], [NSNumber numberWithInt:6], [NSNumber numberWithInt:12], nil] percents:[NSArray arrayWithObjects:[NSNumber numberWithFloat:0.5], [NSNumber numberWithFloat:0.43], [NSNumber numberWithFloat:0.25], [NSNumber numberWithFloat:0.1], [NSNumber numberWithFloat:0], nil]];
    [self activateBadges:goldSet.badges];
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
    [brilliantSet.btnBuy addTarget:self action:@selector(handleBuyClick:) forControlEvents:UIControlEventTouchUpInside];
    [silverSet.btnBuy addTarget:self action:@selector(handleBuyClick:) forControlEvents:UIControlEventTouchUpInside];
    [silver2Set.btnBuy addTarget:self action:@selector(handleBuyClick:) forControlEvents:UIControlEventTouchUpInside];
    [freeSet.btnBuy addTarget:self action:@selector(handleBuyClick:) forControlEvents:UIControlEventTouchUpInside];
    currentPuzzlesView.frame = CGRectMake(0, 0, currentPuzzlesView.frame.size.width, freeSet.frame.origin.y + freeSet.frame.size.height);
    
    [self addSimpleView:newsView];
    [self addFramedView:currentPuzzlesView];
    [self addFramedView:hintsView];
    [self addFramedView:archiveView];

    btnBuyHint1.titleLabel.font = [UIFont fontWithName:@"DINPro-Bold" size:15];
    btnBuyHint2.titleLabel.font = btnBuyHint1.titleLabel.font;
    btnBuyHint3.titleLabel.font = btnBuyHint1.titleLabel.font;
    

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
    for (UIImageView * badge in archiveBadges)
    {
        [badge removeFromSuperview];
    }
    [archiveBadges removeAllObjects];
    archiveBadges = nil;
    hintsView = nil;
    archiveView = nil;
    btnBuyHint1 = nil;
    btnBuyHint2 = nil;
    btnBuyHint3 = nil;
    setToBuyView = nil;
    newsPaginator = nil;
    newsScrollView = nil;
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

-(void)handleBuyClick:(id)sender
{
    [self showActivityIndicator];
    [NSTimer scheduledTimerWithTimeInterval:2 target:self selector:@selector(buySet:) userInfo:sender repeats:NO];
}

- (IBAction)handleNewsPaginatorChange:(id)sender
{
    NSLog(@"paginator: %d", newsPaginator.currentPage);
    [newsScrollView setContentOffset:CGPointMake(newsPaginator.currentPage * newsScrollView.frame.size.width, 0) animated:YES];
}


-(void)buySet:(id)sender
{
    [self hideActivityIndicator];
    
    NSTimer * timer = sender;
    UIButton * btnBuy = timer.userInfo;
    PuzzleSetView * setView = (PuzzleSetView *)btnBuy.superview;
    UIView * blockView = setView.superview;
    CGSize oldSize = setView.frame.size;
    
    [setView switchToBought];
    CGSize newSize = setView.frame.size;
    
    float delta = 0;
    for (UIView * subview in blockView.subviews)
    {
        if ([subview isKindOfClass:[UIImageView class]])
        {
            UIImageView * imageView = (UIImageView *)subview;
            if (imageView.frame.size.height <= blockView.frame.size.height)
            {
                continue;
            }
            [UIView animateWithDuration:0.3 animations:^{
                subview.frame = CGRectMake(subview.frame.origin.x, subview.frame.origin.y, subview.frame.size.width, subview.frame.size.height + (newSize.height - oldSize.height));
            }];
            continue;
        }
        if (subview == setView)
        {
            delta = subview.frame.size.height - oldSize.height;
            subview.frame = CGRectMake(subview.frame.origin.x, subview.frame.origin.y, subview.frame.size.width, oldSize.height);
            [UIView animateWithDuration:0.3 animations:^{
                subview.frame = CGRectMake(subview.frame.origin.x, subview.frame.origin.y, subview.frame.size.width, oldSize.height + delta);
            }];
        }
        else if (delta != 0)
        {
            [UIView animateWithDuration:0.3 animations:^{
                subview.frame = CGRectMake(subview.frame.origin.x, subview.frame.origin.y + delta, subview.frame.size.width, subview.frame.size.height);
            }];
        }
    }
    [self resizeView:blockView newHeight:(blockView.frame.size.height + delta) animated:YES];
    [self activateBadges:setView.badges];
}

-(void)activateBadges:(NSArray *)badges
{
    for (BadgeView * badgeView in badges)
    {
        [badgeView addTarget:self action:@selector(handleBadgeClick:) forControlEvents:UIControlEventTouchUpInside];
    }
}

-(void)handleNewsPrev:(id)sender
{
    if (newsPaginator.currentPage == 0)
    {
        return;
    }
    newsPaginator.currentPage = newsPaginator.currentPage - 1;
    [self handleNewsPaginatorChange:newsPaginator];
}

-(void)handleNewsNext:(id)sender
{
    if (newsPaginator.currentPage == newsPaginator.numberOfPages - 1)
    {
        return;
    }
    newsPaginator.currentPage = newsPaginator.currentPage + 1;
    [self handleNewsPaginatorChange:newsPaginator];
}

-(void)handleNewsTap:(id)sender
{
    if (newsPaginator.currentPage == newsPaginator.numberOfPages - 1)
    {
        newsPaginator.currentPage = 0;
        [self handleNewsPaginatorChange:newsPaginator];
        return;
    }
    newsPaginator.currentPage = newsPaginator.currentPage + 1;
    [self handleNewsPaginatorChange:newsPaginator];
}



@end
