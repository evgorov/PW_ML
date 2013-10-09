//
//  PuzzleSetCell.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/8/13.
//
//

#import "PuzzleSetCell.h"
#import "PuzzleSetView.h"
#import "AppDelegate.h"
#import "FISoundEngine.h"
#import "BadgeView.h"
#import "EventManager.h"
#import "PuzzleSetData.h"
#import "DataManager.h"
#import "StoreManager.h"
#import "APIRequest.h"
#import "SBJsonParser.h"
#import "GlobalData.h"
#import "PrizewordStoreObserver.h"
#import <StoreKit/SKPaymentTransaction.h>
#import "UserData.h"
#import "NSString+Utils.h"

@interface PuzzleSetCell()
{
    NSBlockOperation * operation;
    __weak PuzzleSetState * state;
    __weak UITableView * tableView;
    __weak IBOutlet UIActivityIndicatorView *activityIndicator;
}

- (void)activateBadges;

- (void)handleBuyClick:(id)sender;
- (void)handleShowMoreClick:(id)sender;
- (void)handleBadgeClick:(id)sender;
@end

static FISound * buySetSound = nil;
static FISound * openSetSound = nil;
static FISound * closeSetSound = nil;

@implementation PuzzleSetState

@synthesize isShownFull;
@synthesize height;

- (id)init
{
    self = [super init];
    if (self != nil)
    {
        isShownFull = YES;
        height = 0;
    }
    return self;
}

@end

@implementation PuzzleSetCell

@synthesize puzzleSetView;

static NSOperationQueue * backgroundOperationQueue = nil;

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self != nil)
    {
        puzzleSetView = nil;
        operation = nil;
        tableView = nil;
        state = nil;
        if (backgroundOperationQueue == nil)
        {
            backgroundOperationQueue = [NSOperationQueue new];
        }
        if (buySetSound == nil)
            buySetSound = [[FISoundEngine sharedEngine] soundNamed:@"buy_set.caf" error:nil];
        if (openSetSound == nil)
            openSetSound = [[FISoundEngine sharedEngine] soundNamed:@"open_set.caf" error:nil];
        if (closeSetSound == nil)
            closeSetSound = [[FISoundEngine sharedEngine] soundNamed:@"close_set.caf" error:nil];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_SET_BOUGHT];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_PUZZLE_SYNCHRONIZED];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_COEFFICIENTS_UPDATED];
    }
    return self;
}

- (void)dealloc
{
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_SET_BOUGHT];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_PUZZLE_SYNCHRONIZED];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_COEFFICIENTS_UPDATED];
}

+ (float)minHeight
{
    return [[AppDelegate currentDelegate] isIPad] ? 150 : 140;
}

- (float)actualHeight
{
    return puzzleSetView.frame.size.height;
}

- (void)setupWithData:(PuzzleSetData *)puzzleSetData state:(PuzzleSetState *)state_ month:(int)month showSolved:(BOOL)showSolved showUnsolved:(BOOL)showUnsolved indexPath:(NSIndexPath *)indexPath inTableView:(UITableView *)tableView_
{
    if (puzzleSetData == nil)
    {
        NSLog(@"ERROR: puzzle set data is nil");
        return;
    }
    tableView = tableView_;
    state = state_;
    [self setupBackgroundForIndexPath:indexPath inTableView:tableView];
    if (puzzleSetView != nil)
    {
        [puzzleSetView removeFromSuperview];
    }
    if (operation != nil && ![operation isFinished])
    {
        [operation cancel];
        operation = nil;
    }
    
    puzzleSetView = [PuzzleSetView puzzleSetViewWithData:puzzleSetData month:month showSolved:showSolved showUnsolved:showUnsolved];
    [self addSubview:puzzleSetView];
    [puzzleSetView.btnShowMore addTarget:self action:@selector(handleShowMoreClick:) forControlEvents:UIControlEventTouchUpInside];
    [self activateBadges];
    
    if (!puzzleSetView.puzzleSetData.bought.boolValue)
    {
        [puzzleSetView.btnBuy addTarget:self action:@selector(handleBuyClick:) forControlEvents:UIControlEventTouchUpInside];
        if (puzzleSetView.puzzleSetData.type.intValue == PUZZLESET_FREE)
        {
            [puzzleSetView.btnBuy setTitle:@"Скачать" forState:UIControlStateNormal];
        }
        else
        {
            [puzzleSetView.btnBuy setTitle:@"" forState:UIControlStateNormal];
            __block NSString * setId = puzzleSetView.puzzleSetData.set_id;
            [[StoreManager sharedManager] fetchPriceForSet:setId completion:^(NSString *data, NSError *error) {
                if (data != nil && puzzleSetView != nil && [puzzleSetView.puzzleSetData.set_id compare:setId] == NSOrderedSame)
                {
                    NSString * price = data;
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [puzzleSetView.btnBuy setTitle:price forState:UIControlStateNormal];
                    });
                }
            }];
        }
    }

    
    /*
    [activityIndicator startAnimating];
    operation = [NSBlockOperation new];
    __block NSOperation * internalOperation = operation;
    __block UIActivityIndicatorView * internalActivityIndicator = activityIndicator;
    __block PuzzleSetCell * internalPuzzleSetCell = self;
    [operation addExecutionBlock:^{
        __block PuzzleSetView * internalView =
        if (![internalOperation isCancelled])
        {
            dispatch_async(dispatch_get_main_queue(), ^{
                [internalActivityIndicator stopAnimating];
                internalPuzzleSetCell.puzzleSetView = internalView;
                [internalPuzzleSetCell addSubview:internalPuzzleSetCell.puzzleSetView];
                [[NSNotificationCenter defaultCenter] postNotificationName:@"" object:internalView];
            });
        }
    }];
    [backgroundOperationQueue addOperation:operation];
    */
}

- (void)activateBadges
{
    if (!puzzleSetView.puzzleSetData.bought.boolValue)
    {
        return;
    }
    for (BadgeView * badgeView in puzzleSetView.badges)
    {
        if (badgeView.puzzle.progress < 1)
        {
            badgeView.userInteractionEnabled = YES;
            [badgeView addTarget:self action:@selector(handleBadgeClick:) forControlEvents:UIControlEventTouchUpInside];
        }
        else
        {
            badgeView.userInteractionEnabled = NO;
        }
    }
}

-(void)handleBuyClick:(id)sender
{
    NSLog(@"buy click: %@", puzzleSetView.puzzleSetData.set_id);
    //    [self showActivityIndicator];
    
    [[StoreManager sharedManager] purchaseSet:puzzleSetView.puzzleSetData.set_id];
}

-(void)handleShowMoreClick:(id)sender
{
    UIButton * btnShowMore = (UIButton *)sender;
    
    btnShowMore.selected = !btnShowMore.selected;
    CGSize newSize = btnShowMore.selected ? puzzleSetView.fullSize : puzzleSetView.shortSize;
    CGRect frame = puzzleSetView.frame;
    frame.size = newSize;
    [UIView animateWithDuration:0.2 animations:^{
        puzzleSetView.frame = frame;
    }];
    if (btnShowMore.selected)
    {
        [openSetSound play];
    }
    else
    {
        [closeSetSound play];
    }
    state.height = newSize.height;
    state.isShownFull = btnShowMore.selected;
    [tableView beginUpdates];
    [tableView endUpdates];
}

-(void)handleBadgeClick:(id)sender
{
    BadgeView * badge = (BadgeView *)sender;
    PuzzleData * puzzle = badge.puzzle;
    
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_START andData:puzzle]];
}

#pragma mark
- (void)handleEvent:(Event *)event
{
    if (event.type == EVENT_SET_BOUGHT)
    {
        NSString * setId = event.data;
        if (puzzleSetView != nil && [puzzleSetView.puzzleSetData.set_id compare:setId] == NSOrderedSame)
        {
            [buySetSound play];
            [closeSetSound play];
            [puzzleSetView switchToBought];
            [self activateBadges];
            state.isShownFull = YES;
            state.height = puzzleSetView.fullSize.height;
            [tableView beginUpdates];
            [tableView endUpdates];
            
        }
    }
    else if (event.type == EVENT_COEFFICIENTS_UPDATED)
    {
        if (puzzleSetView != nil)
        {
            int minScore = puzzleSetView.puzzleSetData.minScore;
            puzzleSetView.lblScore.text = [NSString stringWithFormat:@" %@", [NSString digitString:minScore]];
        }
    }
    else if (event.type == EVENT_PUZZLE_SYNCHRONIZED)
    {
        NSString * puzzleId = event.data;
        if (puzzleSetView == nil)
        {
            return;
        }
        for (BadgeView * badge in puzzleSetView.badges)
        {
            if ([badge.puzzle.puzzle_id compare:puzzleId] == NSOrderedSame)
            {
                NSLog(@"handle puzzle %@ synchronization", puzzleId);
                PuzzleData * puzzle = [PuzzleData puzzleWithId:puzzleId andUserId:[GlobalData globalData].loggedInUser.user_id];
                [badge updateWithPuzzle:puzzle];
                break;
            }
        }
    }
}


@end
