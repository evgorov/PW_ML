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

-(void)updateArchive:(NSData *)receivedData;
-(void)updateMonthSets:(NSArray*)monthSets;


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
    
    APIRequest * request = [APIRequest getRequest:@"puzzles" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        [self updateArchive:receivedData];
    } failCallback:^(NSError *error) {
        NSLog(@"archive error: %@", error.description);
    }];
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [request.params setObject:@"0" forKey:@"from"];
    [request.params setObject:@"100" forKey:@"limit"];
    [request runSilent];

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
            [puzzleSetView.btnShowMore addTarget:self action:@selector(handleShowMoreClick:) forControlEvents:UIControlEventTouchUpInside];
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

-(void)updateArchive:(NSData *)receivedData
{
    NSLog(@"archive: %@", [NSString stringWithUTF8String:receivedData.bytes]);
    SBJsonParser * parser = [SBJsonParser new];
    NSDictionary * data = [parser objectWithData:receivedData];
    NSArray * setsData = [data objectForKey:@"sets"];
    
    int lastMonth = 0;
    
    float yOffset = archiveView.frame.size.height;
    while (archiveView.subviews.count > 2)
    {
        UIView * subview = [archiveView.subviews objectAtIndex:archiveView.subviews.count-1];
        yOffset -= subview.frame.size.height;
        [subview removeFromSuperview];
    }
    
    for (NSDictionary * setData in setsData)
    {
        PuzzleSetData * puzzleSet = [PuzzleSetData puzzleSetWithDictionary:setData andUserId:[GlobalData globalData].loggedInUser.provider_id];
        int month = [(NSNumber *)[setData objectForKey:@"month"] intValue];
        int year = [(NSNumber *)[setData objectForKey:@"year"] intValue];
        if (year == [GlobalData globalData].currentYear && month == ([GlobalData globalData].currentMonth + 1))
        {
            continue;
        }
        if (lastMonth != month)
        {
            lastMonth = month;
        }
        else
        {
            month = 0;
        }
        NSMutableArray * ids = [[NSMutableArray alloc] initWithCapacity:puzzleSet.puzzles.count];
        NSMutableArray * percents = [[NSMutableArray alloc] initWithCapacity:puzzleSet.puzzles.count];
        NSMutableArray * scores = [[NSMutableArray alloc] initWithCapacity:puzzleSet.puzzles.count];
        int idx = 1;
        for (PuzzleData * puzzle in puzzleSet.puzzles)
        {
            [ids addObject:[NSNumber numberWithInt:idx]];
            [percents addObject:[NSNumber numberWithFloat:puzzle.progress]];
            [scores addObject:puzzle.score];
            ++idx;
        }
        PuzzleSetView * puzzleSetView = [PuzzleSetView puzzleSetViewWithType:[puzzleSet.type intValue] month:month puzzlesCount:puzzleSet.puzzles.count puzzlesSolved:[PuzzleSetData solved:puzzleSet] score:[PuzzleSetData score:puzzleSet] ids:ids percents:percents scores:scores];
        puzzleSetView.puzzleSetData = puzzleSet;
        [self activateBadges:puzzleSetView];
        [puzzleSetView.btnShowMore addTarget:self action:@selector(handleShowMoreClick:) forControlEvents:UIControlEventTouchUpInside];
        puzzleSetView.frame = CGRectMake(0, yOffset, puzzleSetView.frame.size.width, puzzleSetView.frame.size.height);
        yOffset += puzzleSetView.frame.size.height;
        
        [archiveView addSubview:puzzleSetView];
    }
    
    UIView * frame = [archiveView.subviews objectAtIndex:1];
    [UIView animateWithDuration:0.3 animations:^{
        frame.frame = CGRectMake(frame.frame.origin.x, frame.frame.origin.y, frame.frame.size.width, yOffset - frame.frame.origin.y * 2);
    }];
    [self resizeView:archiveView newHeight:yOffset animated:YES];
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
