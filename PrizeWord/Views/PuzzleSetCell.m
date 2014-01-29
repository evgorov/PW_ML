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
#import "PuzzleSetProxy.h"
#import "DataManager.h"
#import "StoreManager.h"
#import "SBJsonParser.h"
#import "GlobalData.h"
#import "PrizewordStoreObserver.h"
#import <StoreKit/SKPaymentTransaction.h>
#import "UserData.h"
#import "NSString+Utils.h"

@interface PuzzleSetCell()
{
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

const int TAG_PUZZLESET_VIEW = 2;

@implementation PuzzleSetState

@synthesize isShownFull;

- (id)init
{
    self = [super init];
    if (self != nil)
    {
        isShownFull = YES;
    }
    return self;
}

@end

@implementation PuzzleSetCell

@synthesize puzzleSetView;

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self != nil)
    {
        puzzleSetView = nil;
        tableView = nil;
        state = nil;
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

+ (float)fullHeightForPuzzleSet:(PuzzleSetProxy *)puzzleSet
{
    return [PuzzleSetView fullHeightForPuzzleSet:puzzleSet];
}

+ (float)shortHeightForPuzzleSet:(PuzzleSetProxy *)puzzleSet
{
    return [PuzzleSetView shortHeightForPuzzleSet:puzzleSet];
}

- (float)actualHeight
{
    return puzzleSetView.frame.size.height;
}

- (void)setupWithData:(PuzzleSetProxy *)puzzleSetData state:(PuzzleSetState *)state_ month:(int)month showSolved:(BOOL)showSolved showUnsolved:(BOOL)showUnsolved indexPath:(NSIndexPath *)indexPath inTableView:(UITableView *)tableView_
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

    [activityIndicator startAnimating];

    dispatch_async(dispatch_get_main_queue(), ^{
        self.puzzleSetView = [PuzzleSetView puzzleSetViewWithData:puzzleSetData month:month showSolved:showSolved showUnsolved:showUnsolved];
        self.puzzleSetView.btnShowMore.selected = state.isShownFull;
        self.puzzleSetView.tag = TAG_PUZZLESET_VIEW;
        if (!state.isShownFull)
        {
            CGRect frame = self.puzzleSetView.frame;
            frame.size = self.puzzleSetView.shortSize;
            self.puzzleSetView.frame = frame;
        }
        
        if (!self.puzzleSetView.puzzleSetData.bought.boolValue)
        {
            [self.puzzleSetView.btnBuy addTarget:self action:@selector(handleBuyClick:) forControlEvents:UIControlEventTouchUpInside];
            if (self.puzzleSetView.puzzleSetData.type.intValue == PUZZLESET_FREE)
            {
                [self.puzzleSetView.btnBuy setTitle:@"Скачать" forState:UIControlStateNormal];
            }
            else
            {
                [self.puzzleSetView.btnBuy setTitle:@"" forState:UIControlStateNormal];
                __block NSString * setId = self.puzzleSetView.puzzleSetData.set_id;
                [[StoreManager sharedManager] fetchPriceForSet:setId completion:^(NSString *data, NSError *error) {
                    if (data != nil && self.puzzleSetView != nil && [self.puzzleSetView.puzzleSetData.set_id compare:setId] == NSOrderedSame)
                    {
                        NSString * price = data;
                        dispatch_async(dispatch_get_main_queue(), ^{
                            [self.puzzleSetView.btnBuy setTitle:price forState:UIControlStateNormal];
                        });
                    }
                }];
            }
        }
        [self.puzzleSetView.btnShowMore addTarget:self action:@selector(handleShowMoreClick:) forControlEvents:UIControlEventTouchUpInside];
        [self activateBadges];
        [activityIndicator stopAnimating];

        UIView * subview = nil;
        while ((subview = [self viewWithTag:TAG_PUZZLESET_VIEW]) != nil)
        {
            [subview removeFromSuperview];
        }

        [self addSubview:self.puzzleSetView];
    });
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
    state.isShownFull = btnShowMore.selected;
    [tableView beginUpdates];
    [tableView endUpdates];
}

-(void)handleBadgeClick:(id)sender
{
    BadgeView * badge = (BadgeView *)sender;
    PuzzleProxy * puzzle = badge.puzzle;
    
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
                [badge.puzzle updateObject];
                NSLog(@"handle puzzle %@ synchronization", puzzleId);
//                PuzzleProxy * puzzle = [PuzzleProxy puzzleWithId:puzzleId andUserId:[GlobalData globalData].loggedInUser.user_id];
                [badge updateWithPuzzle:badge.puzzle];
                break;
            }
        }
    }
}


@end
