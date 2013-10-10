//
//  PuzzlesViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/15/12.
//
//

#import "PuzzlesViewController.h"
#import "EventManager.h"
#import "TileData.h"
#import "PrizeWordNavigationBar.h"
#import "RootViewController.h"
#import "PuzzleSetView.h"
#import "PuzzleSetPackData.h"
#import "PuzzleSetData.h"
#import "PuzzleData.h"
#import "GlobalData.h"
#import "UserData.h"
#import "APIRequest.h"
#import "SBJson.h"
#import "AppDelegate.h"
#import <StoreKit/StoreKit.h>
#import "NSString+Utils.h"
#import "FISoundEngine.h"
#import "PrizewordStoreObserver.h"
#import "UserDataManager.h"
#import "DataManager.h"
#import "NewsCell.h"
#import "CurrentPuzzlesCell.h"
#import "PuzzleSetCell.h"
#import "LoadingCell.h"
#import "DataContext.h"
#import "HintsCell.h"
#import "StoreManager.h"

NSString * PRODUCTID_PREFIX2 = @"ru.aipmedia.prizeword.";

// default in IB
const int TAG_STATIC_VIEWS = 0;
const int TAG_DYNAMIC_VIEWS = 101;

@interface PuzzlesViewController ()
{
    __weak IBOutlet UITableView *tableView;
    NSMutableArray * currentPuzzleSetStates;
    NSMutableArray * archivePuzzleSetStates;
    NSMutableArray * archivePuzzleSets;
    
    BOOL showNews;
    
    int archiveLastMonth;
    int archiveLastYear;
    BOOL archiveNeedLoading;
    BOOL archiveLoading;
    
}

-(void)loadArchive;

@end

@implementation PuzzlesViewController

#pragma mark UIViewController lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if ([[UIDevice currentDevice].systemVersion compare:@"7.0" options:NSNumericSearch] != NSOrderedAscending)
    {
        tableView.contentInset = UIEdgeInsetsMake([AppDelegate currentDelegate].isIPad ? 72 : 60, 0, 0, 0);
        tableView.scrollIndicatorInsets = tableView.contentInset;
    }
    
    currentPuzzleSetStates = [NSMutableArray new];
    archivePuzzleSets = [NSMutableArray new];
    archivePuzzleSetStates = [NSMutableArray new];
    [scrollView removeFromSuperview];
    tableView.backgroundView = nil;
    UIImage * bgImage = [UIImage imageNamed:@"bg_dark_tile.jpg"];
    tableView.backgroundColor = [UIColor colorWithPatternImage:bgImage];
    [tableView registerNib:[UINib nibWithNibName:@"NewsCell" bundle:[NSBundle mainBundle]] forCellReuseIdentifier:@"newsCell"];
    [tableView registerNib:[UINib nibWithNibName:@"CurrentPuzzlesCell" bundle:[NSBundle mainBundle]] forCellReuseIdentifier:@"currentPuzzlesCell"];
    [tableView registerNib:[UINib nibWithNibName:@"PuzzleSetCell" bundle:[NSBundle mainBundle]] forCellReuseIdentifier:@"puzzleSetCell"];
    [tableView registerNib:[UINib nibWithNibName:@"HintsCell" bundle:[NSBundle mainBundle]] forCellReuseIdentifier:@"hintsCell"];
    [tableView registerNib:[UINib nibWithNibName:@"ArchiveCell" bundle:[NSBundle mainBundle]] forCellReuseIdentifier:@"archiveCell"];
    [tableView registerNib:[UINib nibWithNibName:@"LoadingCell" bundle:[NSBundle mainBundle]] forCellReuseIdentifier:@"loadingCell"];
    
    showNews = YES;
    
    self.title = NSLocalizedString(@"TITLE_PUZZLES", @"Title of screen with puzzles");
    
    archiveLastMonth = [GlobalData globalData].currentMonth;
    archiveLastYear = [GlobalData globalData].currentYear;
    archiveLoading = NO;
    archiveNeedLoading = YES;
}

- (void)viewDidUnload
{
    currentPuzzleSetStates = nil;
    archivePuzzleSets = nil;
    archivePuzzleSetStates = nil;
    
    [super viewDidUnload];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_MONTH_SETS_UPDATED];
    
//    [self showActivityIndicator];
    [[GlobalData globalData] loadMe];
    [[GlobalData globalData] loadCoefficients];
    [[GlobalData globalData] loadMonthSets];
    
    [self loadArchive];
    
    NSLog(@"puzzles view controller: %f %f", self.view.bounds.size.width, self.view.bounds.size.height);
    
//    scrollView.delegate = self;
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    NSLog(@"puzzles viewWillDisappear");

    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_MONTH_SETS_UPDATED];
//    scrollView.delegate = nil;
}

-(void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    
//    scrollView.contentOffset = CGPointZero;
}

#pragma mark EventListenerDelegate
-(void)handleEvent:(Event *)event
{
    if (event.type == EVENT_MONTH_SETS_UPDATED)
    {
        for (int i = 0; i < [GlobalData globalData].monthSets.count; ++i)
        {
            PuzzleSetState * state = nil;
            if (i < currentPuzzleSetStates.count)
            {
                state = [currentPuzzleSetStates objectAtIndex:i];
            }
            else
            {
                state = [PuzzleSetState new];
                [currentPuzzleSetStates addObject:state];
            }
        }
        [tableView reloadData];
    }
}

-(void)loadArchive
{
    if (!archiveLoading && archiveNeedLoading)
    {
        NSCalendar * calendar = [NSCalendar currentCalendar];
        NSDateComponents * components = [calendar components:NSYearCalendarUnit|NSMonthCalendarUnit fromDate:[GlobalData globalData].loggedInUser.createdAt];
        if ([components year] > archiveLastYear || ([components year] == archiveLastYear && [components month] >= archiveLastMonth))
        {
            archiveNeedLoading = NO;
            [tableView reloadData];
            return;
        }

        if (--archiveLastMonth < 1)
        {
            archiveLastMonth = 12;
            --archiveLastYear;
        }
        
        NSLog(@"loading archive for %02d.%d", archiveLastMonth, archiveLastYear);
        archiveLoading = YES;

        [[DataManager sharedManager] fetchArchiveSetsForMonth:archiveLastMonth year:archiveLastYear completion:^(NSArray *data, NSError *error) {
            NSLog(@"fetch result");
            if (data != nil && data.count > 0)
            {
                PuzzleSetData * puzzleSet = [data lastObject];
                NSAssert(puzzleSet.managedObjectContext != nil, @"managed object context of managed object in nil");
                [puzzleSet.managedObjectContext save:nil];
            }
            archiveLoading = NO;
            if (data != nil) {
                __block NSMutableArray * objectIDs = [NSMutableArray arrayWithCapacity:data.count];
                for (NSManagedObject * object in data) {
                    [objectIDs addObject:object.objectID];
                }
                dispatch_async(dispatch_get_main_queue(), ^{
                    for (NSManagedObjectID * objectID in objectIDs)
                    {
                        PuzzleSetData * puzzleSet = (PuzzleSetData *)[[DataContext currentContext] objectWithID:objectID];
                        if (puzzleSet == nil)
                        {
                            NSLog(@"WARNING: cannot transfer object between threads");
                            continue;
                        }
                        if (!puzzleSet.bought.boolValue)
                        {
                            continue;
                        }
                        [archivePuzzleSets addObject:puzzleSet];
                        PuzzleSetState * state = [PuzzleSetState new];
                        state.isShownFull = NO;
                        [archivePuzzleSetStates addObject:state];
                    }
                    [tableView reloadData];
                });
            }
            else
            {
                [tableView reloadData];
            }
        }];
    }
}

#pragma mark user interaction

- (IBAction)handleNewsCloseClick:(id)sender
{
    showNews = NO;
    [tableView beginUpdates];
    [tableView endUpdates];
}

#pragma mark UITableViewDataSource and UITableViewDelegate

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 4;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (section == 0)
    {
        return 1;
    }
    else if (section == 1)
    {
        if ([GlobalData globalData].monthSets == nil)
        {
            return 1;
        }
        return [GlobalData globalData].monthSets.count + 1;
    }
    else if (section == 2)
    {
        return 1;
    }
    else if (section == 3)
    {
        return 1 + archivePuzzleSets.count + (archiveNeedLoading ? 1 : 0);
    }
    return 0;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 0)
    {
        if (!showNews)
        {
            return 0;
        }
        return [NewsCell height];
    }
    else if (indexPath.section == 1)
    {
        if (indexPath.row == 0)
        {
            if ([GlobalData globalData].monthSets != nil && [GlobalData globalData].monthSets.count > 0)
            {
                return [CurrentPuzzlesCell height] * 0.67f; // 2/3
            }
            return  [CurrentPuzzlesCell height];
        }
        while (currentPuzzleSetStates.count < indexPath.row)
        {
            [currentPuzzleSetStates addObject:[PuzzleSetState new]];
        }
        BOOL isFull = [(PuzzleSetState *)[currentPuzzleSetStates objectAtIndex:indexPath.row - 1] isShownFull];
        PuzzleSetData * puzzleSet = [[GlobalData globalData].monthSets objectAtIndex:indexPath.row - 1];
        float height = isFull ? [PuzzleSetCell fullHeightForPuzzleSet:puzzleSet] : [PuzzleSetCell shortHeightForPuzzleSet:puzzleSet];
        if (indexPath.row == [GlobalData globalData].monthSets.count)
        {
            height += ([AppDelegate currentDelegate].isIPad ? 26 : 14);
        }
        return height;
    }
    else if (indexPath.section == 2)
    {
        return [HintsCell height];
    }
    else if (indexPath.section == 3)
    {
        if (indexPath.row == 0)
        {
            if (archivePuzzleSets.count > 0 || archiveNeedLoading)
            {
                return [FramedBlockCell height] * 0.67; // 2/3
            }
            return [FramedBlockCell height];
        }
        else if (indexPath.row > archivePuzzleSets.count)
        {
            return [FramedBlockCell height] / 2 + ([AppDelegate currentDelegate].isIPad ? 22 : 10);
        }
        else
        {
            while (archivePuzzleSets.count < indexPath.row)
            {
                PuzzleSetState * state = [PuzzleSetState new];
                state.isShownFull = NO;
            }
            PuzzleSetState * state = [archivePuzzleSetStates objectAtIndex:indexPath.row - 1];
            PuzzleSetData * puzzleSet = [archivePuzzleSets objectAtIndex:indexPath.row - 1];
            float height = state.isShownFull ? [PuzzleSetCell fullHeightForPuzzleSet:puzzleSet] : [PuzzleSetCell shortHeightForPuzzleSet:puzzleSet];
            if (!archiveNeedLoading)
            {
                height += ([AppDelegate currentDelegate].isIPad ? 26 : 14);
            }
            return height;
        }
    }
    return 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView_ cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 0)
    {
        NewsCell * cell = [tableView dequeueReusableCellWithIdentifier:@"newsCell"];
        [cell.btnClose addTarget:self action:@selector(handleNewsCloseClick:) forControlEvents:UIControlEventTouchUpInside];
        [cell setup];
        return cell;
    }
    else if (indexPath.section == 1)
    {
        if (indexPath.row == 0)
        {
            CurrentPuzzlesCell * cell = [tableView dequeueReusableCellWithIdentifier:@"currentPuzzlesCell"];
            
            if ([GlobalData globalData].monthSets != nil)
            {
                NSCalendar * calendar = [NSCalendar currentCalendar];
                NSDate * currentDate = [NSDate date];
                NSDateComponents * currentComponents = [calendar components:NSDayCalendarUnit|NSMonthCalendarUnit|NSYearCalendarUnit|NSHourCalendarUnit|NSMinuteCalendarUnit|NSSecondCalendarUnit fromDate:currentDate];
                [currentComponents setMonth:[GlobalData globalData].currentMonth];
                [currentComponents setYear:[GlobalData globalData].currentYear];
                [currentComponents setDay:[GlobalData globalData].currentDay];
                currentDate = [calendar dateFromComponents:currentComponents];
                NSRange daysRange = [calendar rangeOfUnit:NSDayCalendarUnit inUnit:NSMonthCalendarUnit forDate:currentDate];
                int daysLeft = daysRange.location + daysRange.length - [GlobalData globalData].currentDay;
                int month = [GlobalData globalData].currentMonth;
                [cell setupWithMonth:month daysLeft:daysLeft indexPath:indexPath tableView:tableView];
            }
            else
            {
                [cell setupWithLoadingAndIndexPath:indexPath tableView:tableView];
            }
            return cell;
        }
        PuzzleSetCell * cell = nil;
        if ([tableView respondsToSelector:@selector(dequeueReusableCellWithIdentifier:forIndexPath:)])
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"puzzleSetCell" forIndexPath:indexPath];
        }
        else
        {
            cell = [tableView dequeueReusableCellWithIdentifier:@"puzzleSetCell"];
        }
        while (currentPuzzleSetStates.count < indexPath.row)
        {
            [currentPuzzleSetStates addObject:[PuzzleSetState new]];
        }
        PuzzleSetData * puzzleSet = [[GlobalData globalData].monthSets objectAtIndex:indexPath.row - 1];
        PuzzleSetState * state = [currentPuzzleSetStates objectAtIndex:indexPath.row - 1];
        if (cell.puzzleSetView != nil && [puzzleSet.set_id compare:cell.puzzleSetView.puzzleSetData.set_id] == NSOrderedSame && [puzzleSet.user_id compare:cell.puzzleSetView.puzzleSetData.user_id])
        {
            NSLog(@"found set: %@", puzzleSet.set_id);
            if (puzzleSet.set_id == nil)
            {
                NSLog(@"puzzle set id is nil");
            }
            if (!state.isShownFull)
            {
                [cell.puzzleSetView.btnShowMore setSelected:NO];
                CGRect frame = cell.puzzleSetView.frame;
                frame.size = cell.puzzleSetView.shortSize;
                cell.puzzleSetView.frame = frame;
            }
            /*
            if (state.height != cell.actualHeight)
            {
                state.height = cell.actualHeight;
                [tableView reloadData];
            }
            */
            return cell;
        }
        
        [cell setupWithData:puzzleSet state:state month:0 showSolved:YES showUnsolved:YES indexPath:indexPath inTableView:tableView];
        
        if (!state.isShownFull)
        {
            [cell.puzzleSetView.btnShowMore setSelected:NO];
            CGRect frame = cell.puzzleSetView.frame;
            frame.size = cell.puzzleSetView.shortSize;
            cell.puzzleSetView.frame = frame;
        }
        /*
        if (state.height != cell.actualHeight)
        {
            state.height = cell.actualHeight;
            [tableView reloadData];
        }
        */
        return cell;
    }
    else if (indexPath.section == 2)
    {
        HintsCell * cell = [tableView dequeueReusableCellWithIdentifier:@"hintsCell"];
        [cell setupForIndexPath:indexPath inTableView:tableView];
        return cell;
    }
    else if (indexPath.section == 3)
    {
        if (indexPath.row == 0)
        {
            FramedBlockCell * cell = [tableView dequeueReusableCellWithIdentifier:@"archiveCell"];
            [cell setupBackgroundForIndexPath:indexPath inTableView:tableView];
            return cell;
        }
        else if (indexPath.row > archivePuzzleSets.count)
        {
            [self loadArchive];
            LoadingCell * cell = [tableView dequeueReusableCellWithIdentifier:@"loadingCell"];
            [cell setupBackgroundForIndexPath:indexPath inTableView:tableView];
            return cell;
        }
        else
        {
            PuzzleSetCell * cell = nil;
            if ([tableView respondsToSelector:@selector(dequeueReusableCellWithIdentifier:forIndexPath:)])
            {
                cell = [tableView dequeueReusableCellWithIdentifier:@"puzzleSetCell" forIndexPath:indexPath];
            }
            else
            {
                cell = [tableView dequeueReusableCellWithIdentifier:@"puzzleSetCell"];
            }
            while (archivePuzzleSets.count < indexPath.row)
            {
                [archivePuzzleSetStates addObject:[PuzzleSetState new]];
            }
            PuzzleSetData * puzzleSet = [archivePuzzleSets objectAtIndex:indexPath.row - 1];
            PuzzleSetData * prevPuzzleSet = indexPath.row > 1 ? ([archivePuzzleSets objectAtIndex:indexPath.row - 2]) : nil;
            PuzzleSetState * state = [archivePuzzleSetStates objectAtIndex:indexPath.row - 1];
            if (cell.puzzleSetView != nil && [puzzleSet.set_id compare:cell.puzzleSetView.puzzleSetData.set_id] == NSOrderedSame && [puzzleSet.user_id compare:cell.puzzleSetView.puzzleSetData.user_id])
            {
                NSLog(@"found set: %@", puzzleSet.set_id);
                if (puzzleSet.set_id == nil)
                {
                    NSLog(@"puzzle set id is nil");
                }
                if (!state.isShownFull)
                {
                    [cell.puzzleSetView.btnShowMore setSelected:NO];
                    CGRect frame = cell.puzzleSetView.frame;
                    frame.size = cell.puzzleSetView.shortSize;
                    cell.puzzleSetView.frame = frame;
                }
                /*
                if (state.height != cell.actualHeight)
                {
                    state.height = cell.actualHeight;
                    [tableView reloadData];
                }
                */
                return cell;
            }
            
            int month = (prevPuzzleSet == nil || prevPuzzleSet.puzzleSetPack.month.intValue != puzzleSet.puzzleSetPack.month.intValue) ? puzzleSet.puzzleSetPack.month.intValue : 0;
            [cell setupWithData:puzzleSet state:state month:month showSolved:YES showUnsolved:YES indexPath:indexPath inTableView:tableView];
            
            if (!state.isShownFull)
            {
                [cell.puzzleSetView.btnShowMore setSelected:NO];
                CGRect frame = cell.puzzleSetView.frame;
                frame.size = cell.puzzleSetView.shortSize;
                cell.puzzleSetView.frame = frame;
            }
            /*
            if (state.height != cell.actualHeight)
            {
                state.height = cell.actualHeight;
                [tableView reloadData];
            }
            */
            return cell;
        }
    }
    return nil;
}

@end
