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
@class PuzzleSetData;

@interface PuzzleSetState : NSObject

@property () BOOL isShownFull;
@property () float height;

@end

@interface PuzzleSetCell : FramedBlockCell<EventListenerDelegate>

@property (nonatomic, retain) PuzzleSetView * puzzleSetView;

+ (float)minHeight;

- (float)actualHeight;
- (void)setupWithData:(PuzzleSetData *)puzzleSetData state:(PuzzleSetState *)state month:(int)month showSolved:(BOOL)showSolved showUnsolved:(BOOL)showUnsolved indexPath:(NSIndexPath *)indexPath inTableView:(UITableView *)tableView;

@end
