//
//  PuzzleSetCell.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/8/13.
//
//

#import "FramedBlockCell.h"
#import "EventListenerDelegate.h"

@class PuzzleSetView;
@class PuzzleSetProxy;

@interface PuzzleSetState : NSObject

@property () BOOL isShownFull;

@end

@interface PuzzleSetCell : FramedBlockCell<EventListenerDelegate>

@property (nonatomic, retain) PuzzleSetView * puzzleSetView;

+ (float)fullHeightForPuzzleSet:(PuzzleSetProxy *)puzzleSet;
+ (float)shortHeightForPuzzleSet:(PuzzleSetProxy *)puzzleSet;

- (float)actualHeight;
- (void)setupWithData:(PuzzleSetProxy *)puzzleSetData state:(PuzzleSetState *)state month:(int)month showSolved:(BOOL)showSolved showUnsolved:(BOOL)showUnsolved indexPath:(NSIndexPath *)indexPath inTableView:(UITableView *)tableView;

@end
