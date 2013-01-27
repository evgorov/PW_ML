//
//  RatingViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import "BlockedViewController.h"

@interface RatingViewController : BlockedViewController<UITableViewDelegate, UITableViewDataSource>
{
    IBOutlet UITableView * ratingView;
    NSMutableArray * users;
}

@end
