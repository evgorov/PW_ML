//
//  HintsCell.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/9/13.
//
//

#import "FramedBlockCell.h"
#import "EventListenerDelegate.h"

@interface HintsCell : FramedBlockCell<EventListenerDelegate>

@property (weak, nonatomic) IBOutlet PrizeWordButton *btnBuyHint1;
@property (weak, nonatomic) IBOutlet PrizeWordButton *btnBuyHint2;
@property (weak, nonatomic) IBOutlet PrizeWordButton *btnBuyHint3;
@property (weak, nonatomic) IBOutlet UILabel *lblHintsLeft;

+ (float)height;

- (void)setupForIndexPath:(NSIndexPath *)indexPath inTableView:(UITableView *)tableView;

@end
