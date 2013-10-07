//
//  CurrentPuzzlesCell.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/8/13.
//
//

#import "CurrentPuzzlesCell.h"
#import "AppDelegate.h"
#import "NSString+Utils.h"

NSString * CURRENT_PUZZLES_MONTHS[] = {@"январь", @"февраль", @"март", @"апрель", @"май", @"июнь", @"июль", @"август", @"сентябрь", @"октябрь", @"ноябрь", @"декабрь"};

@interface CurrentPuzzlesCell ()
{
    __weak IBOutlet UILabel *puzzlesViewCaption;
    __weak IBOutlet UIImageView *puzzlesTimeLeftBg;
    __weak IBOutlet UILabel *puzzlesTimeLeftCaption;
    __weak IBOutlet UIActivityIndicatorView *activityIndicator;
}

@end

@implementation CurrentPuzzlesCell

+ (float)height
{
    return [AppDelegate currentDelegate].isIPad ? 116 : 84;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self != nil)
    {
        puzzlesViewCaption.text = @"Сканворды за ...";
    }
    return self;
}

- (void)setupWithLoadingAndIndexPath:(NSIndexPath *)indexPath tableView:(UITableView *)tableView
{
    [super setupBackgroundForIndexPath:indexPath inTableView:tableView];

    puzzlesViewCaption.text = @"Сканворды за ...";
    puzzlesTimeLeftBg.hidden = YES;
    puzzlesTimeLeftCaption.hidden = YES;
    [activityIndicator startAnimating];
}

- (void)setupWithMonth:(int)month daysLeft:(int)daysLeft indexPath:(NSIndexPath *)indexPath tableView:(UITableView *)tableView
{
    [super setupBackgroundForIndexPath:indexPath inTableView:tableView];
    
    puzzlesViewCaption.text = [NSString stringWithFormat:@"Сканворды за %@", CURRENT_PUZZLES_MONTHS[month - 1]];
    puzzlesTimeLeftCaption.text = [NSString stringWithFormat:@"Ост. %d %@", daysLeft, [NSString declesion:daysLeft oneString:@"день" twoString:@"дня" fiveString:@"дней"]];
    puzzlesTimeLeftCaption.hidden = daysLeft > 5;
    puzzlesTimeLeftBg.hidden = daysLeft > 5;
}

@end
