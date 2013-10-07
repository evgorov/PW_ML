//
//  CurrentPuzzlesCell.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/8/13.
//
//

#import "FramedBlockCell.h"

@interface CurrentPuzzlesCell : FramedBlockCell

+ (float)height;

- (void)setupWithLoadingAndIndexPath:(NSIndexPath *)indexPath tableView:(UITableView *)tableView;
- (void)setupWithMonth:(int)month daysLeft:(int)daysLeft indexPath:(NSIndexPath *)indexPath tableView:(UITableView *)tableView;

@end
