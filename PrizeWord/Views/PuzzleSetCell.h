//
//  PuzzleSetCell.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/8/13.
//
//

#import "FramedBlockCell.h"

@class PuzzleSetView;
@class PuzzleSetData;

@interface PuzzleSetCell : FramedBlockCell

@property (nonatomic, retain) PuzzleSetView * puzzleSetView;

+ (float)minHeight;

- (float)actualHeight;
- (void)setupWithData:(PuzzleSetData *)puzzleSetData month:(int)month showSolved:(BOOL)showSolved showUnsolved:(BOOL)showUnsolved indexPath:(NSIndexPath *)indexPath inTableView:(UITableView *)tableView;

-(void)switchToBought;

@end
