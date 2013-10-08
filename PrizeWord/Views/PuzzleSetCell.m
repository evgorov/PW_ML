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

@implementation PuzzleSetCell

@synthesize puzzleSetView;

static NSOperationQueue * backgroundOperationQueue = nil;

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self != nil)
    {
        puzzleSetView = nil;
        if (backgroundOperationQueue == nil)
        {
            backgroundOperationQueue = [NSOperationQueue new];
        }
    }
    return self;
}

+ (float)minHeight
{
    return [[AppDelegate currentDelegate] isIPad] ? 150 : 140;
}

- (float)actualHeight
{
    return puzzleSetView.frame.size.height;
}

- (void)setupWithData:(PuzzleSetData *)puzzleSetData month:(int)month showSolved:(BOOL)showSolved showUnsolved:(BOOL)showUnsolved indexPath:(NSIndexPath *)indexPath inTableView:(UITableView *)tableView
{
    if (puzzleSetData == nil)
    {
        NSLog(@"ERROR: puzzle set data is nil");
    }
    [self setupBackgroundForIndexPath:indexPath inTableView:tableView];
    if (puzzleSetView != nil)
    {
        [puzzleSetView removeFromSuperview];
    }
    
    puzzleSetView = [PuzzleSetView puzzleSetViewWithData:puzzleSetData month:month showSolved:showSolved showUnsolved:showUnsolved];
    [self addSubview:puzzleSetView];
}

- (void)switchToBought
{
    [puzzleSetView switchToBought];
}

@end
