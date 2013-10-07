//
//  NewsCell.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/7/13.
//
//

#import <UIKit/UIKit.h>

@interface NewsCell : UITableViewCell<UIScrollViewDelegate>

@property (weak, nonatomic) IBOutlet PrizeWordButton *btnClose;

- (void)setup;

@end
