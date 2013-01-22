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
#import "PuzzleSetData.h"
#import "PuzzleData.h"
#import "GlobalData.h"
#import "UserData.h"
#import "APIRequest.h"
#import "SBJson.h"
#import "AppDelegate.h"

NSString * MONTHS2[] = {@"январь", @"февраль", @"март", @"апрель", @"май", @"июнь", @"июль", @"август", @"сентябрь", @"октябрь", @"ноябрь", @"декабрь"};


@interface PuzzlesViewController ()

-(void)handleBadgeClick:(id)sender;
-(void)handleBuyClick:(id)sender;
-(void)handleShowMoreClick:(id)sender;
-(void)activateBadges:(PuzzleSetView *)puzzleSetView;

-(void)handleNewsPrev:(id)sender;
-(void)handleNewsNext:(id)sender;
-(void)handleNewsTap:(id)sender;

-(void)resizeBlockView:(UIView *)blockView withInnerView:(UIView *)innerView fromSize:(CGSize)oldSize toSize:(CGSize)newSize;

@end

@implementation PuzzlesViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.title = @"Сканворды";
    puzzlesViewCaption.text = @"Сканворды за ...";   
    
    UISwipeGestureRecognizer * newsRightGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleNewsPrev:)];
    newsRightGestureRecognizer.direction = UISwipeGestureRecognizerDirectionRight;
    UISwipeGestureRecognizer * newsLeftGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleNewsNext:)];
    newsLeftGestureRecognizer.direction = UISwipeGestureRecognizerDirectionLeft;
    UITapGestureRecognizer * newsTapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleNewsTap:)];
    [newsScrollView setGestureRecognizers:[NSArray arrayWithObjects:newsLeftGestureRecognizer, newsRightGestureRecognizer, newsTapGestureRecognizer, nil]];


    
    PuzzleSetView * archiveGoldSet = [PuzzleSetView puzzleSetViewWithType:PUZZLESET_GOLD month:5  puzzlesCount:12 puzzlesSolved:7 score:27000 ids:[NSArray arrayWithObjects:[NSNumber numberWithInt:1], [NSNumber numberWithInt:2], [NSNumber numberWithInt:4], [NSNumber numberWithInt:5], [NSNumber numberWithInt:6], [NSNumber numberWithInt:12], nil] percents:[NSArray arrayWithObjects:[NSNumber numberWithFloat:1], [NSNumber numberWithFloat:0.5], [NSNumber numberWithFloat:0.43], [NSNumber numberWithFloat:0.25], [NSNumber numberWithFloat:0.1], [NSNumber numberWithFloat:0], nil] scores:[NSArray arrayWithObjects:[NSNumber numberWithInt:100], [NSNumber numberWithInt:200], [NSNumber numberWithInt:400], [NSNumber numberWithInt:500], [NSNumber numberWithInt:600], [NSNumber numberWithInt:12], nil]];
    PuzzleSetView * archiveSilverSet = [PuzzleSetView puzzleSetViewWithType:PUZZLESET_SILVER2 month:0  puzzlesCount:12 puzzlesSolved:7 score:27000 ids:[NSArray arrayWithObjects:[NSNumber numberWithInt:1], [NSNumber numberWithInt:2], [NSNumber numberWithInt:4], [NSNumber numberWithInt:5], [NSNumber numberWithInt:6], [NSNumber numberWithInt:12], nil] percents:[NSArray arrayWithObjects:[NSNumber numberWithFloat:1], [NSNumber numberWithFloat:0.5], [NSNumber numberWithFloat:0.43], [NSNumber numberWithFloat:0.25], [NSNumber numberWithFloat:0.1], [NSNumber numberWithFloat:0], nil] scores:[NSArray arrayWithObjects:[NSNumber numberWithInt:100], [NSNumber numberWithInt:200], [NSNumber numberWithInt:400], [NSNumber numberWithInt:500], [NSNumber numberWithInt:600], [NSNumber numberWithInt:12], nil]];
    PuzzleSetView * archiveGoldSet2 = [PuzzleSetView puzzleSetViewWithType:PUZZLESET_GOLD month:9  puzzlesCount:12 puzzlesSolved:7 score:27000 ids:[NSArray arrayWithObjects:[NSNumber numberWithInt:1], [NSNumber numberWithInt:2], [NSNumber numberWithInt:4], [NSNumber numberWithInt:5], [NSNumber numberWithInt:6], [NSNumber numberWithInt:12], nil] percents:[NSArray arrayWithObjects:[NSNumber numberWithFloat:1], [NSNumber numberWithFloat:0.5], [NSNumber numberWithFloat:0.43], [NSNumber numberWithFloat:0.25], [NSNumber numberWithFloat:0.1], [NSNumber numberWithFloat:0], nil] scores:[NSArray arrayWithObjects:[NSNumber numberWithInt:100], [NSNumber numberWithInt:200], [NSNumber numberWithInt:400], [NSNumber numberWithInt:500], [NSNumber numberWithInt:600], [NSNumber numberWithInt:12], nil]];
    [self activateBadges:archiveGoldSet];
    [self activateBadges:archiveSilverSet];
    [self activateBadges:archiveGoldSet2];
    
    archiveGoldSet.frame = CGRectMake(0, archiveView.frame.size.height, archiveGoldSet.frame.size.width, archiveGoldSet.frame.size.height);
    archiveSilverSet.frame = CGRectMake(0, archiveGoldSet.frame.origin.y + archiveGoldSet.frame.size.height, archiveSilverSet.frame.size.width, archiveSilverSet.frame.size.height);
    archiveGoldSet2.frame = CGRectMake(0, archiveSilverSet.frame.origin.y + archiveSilverSet.frame.size.height, archiveGoldSet2.frame.size.width, archiveGoldSet2.frame.size.height);

    [archiveView addSubview:archiveGoldSet];
    [archiveView addSubview:archiveSilverSet];
    [archiveView addSubview:archiveGoldSet2];
    archiveView.frame = CGRectMake(0, 0, archiveView.frame.size.width, archiveGoldSet2.frame.origin.y + archiveGoldSet2.frame.size.height);
    [archiveGoldSet.btnShowMore addTarget:self action:@selector(handleShowMoreClick:) forControlEvents:UIControlEventTouchUpInside];
    [archiveSilverSet.btnShowMore addTarget:self action:@selector(handleShowMoreClick:) forControlEvents:UIControlEventTouchUpInside];
    [archiveGoldSet2.btnShowMore addTarget:self action:@selector(handleShowMoreClick:) forControlEvents:UIControlEventTouchUpInside];
    
    currentPuzzlesView.frame = CGRectMake(0, 0, currentPuzzlesView.frame.size.width, currentPuzzlesView.frame.size.height);
    
    [self addSimpleView:newsView];
    [self addFramedView:currentPuzzlesView];
    [self addFramedView:hintsView];
    [self addFramedView:archiveView];
    
    btnBuyHint1.titleLabel.font = [UIFont fontWithName:@"DINPro-Bold" size:15];
    btnBuyHint2.titleLabel.font = btnBuyHint1.titleLabel.font;
    btnBuyHint3.titleLabel.font = btnBuyHint1.titleLabel.font;
}

- (void)viewDidUnload
{
    newsView = nil;
    currentPuzzlesView = nil;
    hintsView = nil;
    archiveView = nil;
    btnBuyHint1 = nil;
    btnBuyHint2 = nil;
    btnBuyHint3 = nil;
    setToBuyView = nil;
    newsPaginator = nil;
    newsScrollView = nil;
    lblHintsLeft = nil;
    puzzlesViewCaption = nil;
    [super viewDidUnload];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self showActivityIndicator];
    [[GlobalData globalData] loadMonthSets:^{
        [self hideActivityIndicator];
        [self updateMonthSets:[GlobalData globalData].monthSets];
    }];

    lblHintsLeft.text = [NSString stringWithFormat:@"Осталось: %d", [GlobalData globalData].loggedInUser.hints];
}

-(void)updateMonthSets:(NSArray *)monthSets
{
    float yOffset = currentPuzzlesView.frame.size.height;
    while (currentPuzzlesView.subviews.count > 2) {
        UIView * subview = [currentPuzzlesView.subviews objectAtIndex:currentPuzzlesView.subviews.count-1];
        yOffset -= subview.frame.size.height;
        [subview removeFromSuperview];
    }
    for (PuzzleSetData * puzzleSet in monthSets) {
        if ([puzzleSet.bought boolValue]) {
            NSMutableArray * unsolvedIds = [NSMutableArray new];
            NSMutableArray * unsolvedPercent = [NSMutableArray new];
            NSMutableArray * unsolvedScores = [NSMutableArray new];
            int idx = 1;
            for (PuzzleData * puzzle in puzzleSet.puzzles) {
                if (puzzle.solved != puzzle.questions.count)
                {
                    [unsolvedIds addObject:[NSNumber numberWithInt:idx]];
                    [unsolvedPercent addObject:[NSNumber numberWithFloat:puzzle.progress]];
                    [unsolvedScores addObject:puzzle.base_score];
                }
                ++idx;
            }
            PuzzleSetView * puzzleSetView = [PuzzleSetView puzzleSetViewWithType:[puzzleSet.type intValue] month:0 puzzlesCount:puzzleSet.puzzles.count puzzlesSolved:[PuzzleSetData solved:puzzleSet] score:[PuzzleSetData score:puzzleSet] ids:unsolvedIds percents:unsolvedPercent scores:unsolvedScores];
            puzzleSetView.puzzleSetData = puzzleSet;
            puzzleSetView.frame = CGRectMake(0, yOffset, puzzleSetView.frame.size.width, puzzleSetView.frame.size.height);
            yOffset += puzzleSetView.frame.size.height;
            [currentPuzzlesView addSubview:puzzleSetView];
            [self activateBadges:puzzleSetView];
        } else {
            PuzzleSetView * puzzleSetView = [PuzzleSetView puzzleSetViewWithType:[puzzleSet.type intValue] puzzlesCount:puzzleSet.puzzles.count minScore:[PuzzleSetData minScore:puzzleSet] price:3.99f];
            puzzleSetView.puzzleSetData = puzzleSet;
            puzzleSetView.frame = CGRectMake(0, yOffset, puzzleSetView.frame.size.width, puzzleSetView.frame.size.height);
            yOffset += puzzleSetView.frame.size.height;
            [currentPuzzlesView addSubview:puzzleSetView];
            [puzzleSetView.btnBuy addTarget:self action:@selector(handleBuyClick:) forControlEvents:UIControlEventTouchUpInside];
        }
    }
    puzzlesViewCaption.text = [NSString stringWithFormat:@"Сканворды за %@", MONTHS2[[GlobalData globalData].currentMonth]];
    
    UIView * frame = [currentPuzzlesView.subviews objectAtIndex:1];
    [UIView animateWithDuration:0.3 animations:^{
        frame.frame = CGRectMake(frame.frame.origin.x, frame.frame.origin.y, frame.frame.size.width, yOffset - frame.frame.origin.y * 2);
    }];
    [self resizeView:currentPuzzlesView newHeight:yOffset animated:YES];
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
    PuzzleSetView * puzzleSetView = (PuzzleSetView *)badge.superview;
    PuzzleSetData * puzzleSet = puzzleSetView.puzzleSetData;
    NSEnumerator * puzzleIt = [puzzleSet.puzzles objectEnumerator];
    for (int i = 0; i < badge.tag; ++i)
    {
        [puzzleIt nextObject];
    }
    PuzzleData * puzzle = [puzzleIt nextObject];
    
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_START andData:puzzle]];
}

-(void)handleBuyClick:(id)sender
{
    PuzzleSetView * setView = (PuzzleSetView *)((UIButton *)sender).superview;
    [self showActivityIndicator];
    APIRequest * request = [APIRequest postRequest:[NSString stringWithFormat:@"sets/%@/buy", setView.puzzleSetData.set_id] successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        NSLog(@"set bought! %@", [NSString stringWithUTF8String:receivedData.bytes]);
        [self hideActivityIndicator];
        [self buySet:sender];
        [setView.puzzleSetData setBought:[NSNumber numberWithBool:YES]];
        NSError * error;
        [[AppDelegate currentDelegate].managedObjectContext save:&error];
        if (error != nil) {
            NSLog(@"error: %@", error.description);
        }
    } failCallback:^(NSError *error) {
        NSLog(@"set error: %@", error.description);
        [self hideActivityIndicator];
    }];
    [request.params setObject:setView.puzzleSetData.set_id forKey:@"id"];
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [request runSilent];
}

-(void)handleShowMoreClick:(id)sender
{
    UIButton * btnShowMore = (UIButton *)sender;

    PuzzleSetView * setView = (PuzzleSetView *)btnShowMore.superview;
    UIView * blockView = setView.superview;
    CGSize oldSize = setView.frame.size;

    btnShowMore.selected = !btnShowMore.selected;
    CGSize newSize = btnShowMore.selected ? setView.fullSize : setView.shortSize;
    
    [self resizeBlockView:blockView withInnerView:setView fromSize:oldSize toSize:newSize];
    
}

- (IBAction)handleNewsPaginatorChange:(id)sender
{
    NSLog(@"paginator: %d", newsPaginator.currentPage);
    [newsScrollView setContentOffset:CGPointMake(newsPaginator.currentPage * newsScrollView.frame.size.width, 0) animated:YES];
}

- (IBAction)handleBuyHints:(id)sender
{
    UIButton * button = sender;
    [self showActivityIndicator];
    APIRequest * request = [APIRequest postRequest:@"hints" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        NSLog(@"hints: %@", [NSString stringWithUTF8String:receivedData.bytes]);
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * dict = [parser objectWithData:receivedData];
        [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:[dict objectForKey:@"me"]];
        lblHintsLeft.text = [NSString stringWithFormat:@"Осталось: %d", [GlobalData globalData].loggedInUser.hints];
        [self hideActivityIndicator];
    } failCallback:^(NSError *error) {
        [self hideActivityIndicator];
        NSLog(@"hints error: %@", error.description);
    }];
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [request.params setObject:[NSString stringWithFormat:@"%d", button.tag] forKey:@"hints_change"];
    [request runSilent];
}

-(void)buySet:(id)sender
{
    [self hideActivityIndicator];
    
    UIButton * btnBuy = sender;
    PuzzleSetView * setView = (PuzzleSetView *)btnBuy.superview;
    UIView * blockView = setView.superview;
    CGSize oldSize = setView.frame.size;
    
    [setView switchToBought];
    CGSize newSize = setView.frame.size;
    
    [self resizeBlockView:blockView withInnerView:setView fromSize:oldSize toSize:newSize];
    [self activateBadges:setView];
}

-(void)resizeBlockView:(UIView *)blockView withInnerView:(UIView *)innerView fromSize:(CGSize)oldSize toSize:(CGSize)newSize
{
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
        if (subview == innerView)
        {
            delta = newSize.height - oldSize.height;
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
}

-(void)activateBadges:(PuzzleSetView *)puzzleSetView
{
    for (BadgeView * badgeView in puzzleSetView.badges)
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
