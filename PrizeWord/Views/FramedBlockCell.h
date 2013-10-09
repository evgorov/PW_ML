//
//  FramedBlockCell.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/7/13.
//
//

#import <UIKit/UIKit.h>

@interface FramedBlockCell : UITableViewCell

+ (float)height;

- (void)setupBackgroundForIndexPath:(NSIndexPath *)indexPath inTableView:(UITableView *)tableView;

@end
