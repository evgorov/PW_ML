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

@interface PuzzleSetCell()
{
    NSBlockOperation * operation;
    __weak IBOutlet UIActivityIndicatorView *activityIndicator;
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
    if (operation != nil && ![operation isFinished])
    {
        [operation cancel];
        operation = nil;
    }
    
    puzzleSetView = [PuzzleSetView puzzleSetViewWithData:puzzleSetData month:month showSolved:showSolved showUnsolved:showUnsolved];
    [self addSubview:puzzleSetView];
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

- (void)switchToBought
{
    [puzzleSetView switchToBought];
}

@end
