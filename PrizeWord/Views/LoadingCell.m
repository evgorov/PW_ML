//
//  LoadingCell.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/9/13.
//
//

#import "LoadingCell.h"

@interface LoadingCell ()
{
    __weak IBOutlet UIActivityIndicatorView *activityIndicator;
}

@end

@implementation LoadingCell

- (void)setupBackgroundForIndexPath:(NSIndexPath *)indexPath inTableView:(UITableView *)tableView
{
    [super setupBackgroundForIndexPath:indexPath inTableView:tableView];
    [activityIndicator startAnimating];
}

@end
