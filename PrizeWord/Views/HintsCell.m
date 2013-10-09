//
//  HintsCell.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/9/13.
//
//

#import "HintsCell.h"
#import "DataManager.h"
#import <StoreKit/SKProduct.h>
#import "GlobalData.h"
#import "UserData.h"
#import "Event.h"
#import "EventManager.h"
#import "AppDelegate.h"
#import "StoreManager.h"

@interface HintsCell ()
{
    BOOL isInitialized;
}
- (IBAction)handleBuyHintsClick:(id)sender;

@end

@implementation HintsCell

+ (float)height
{
    return [AppDelegate currentDelegate].isIPad ? 244 : 187;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self != nil)
    {
        isInitialized = NO;
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_ME_UPDATED];
    }
    return self;
}

- (void)dealloc
{
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_ME_UPDATED];
}

- (void)setupForIndexPath:(NSIndexPath *)indexPath inTableView:(UITableView *)tableView
{
    [super setupBackgroundForIndexPath:indexPath inTableView:tableView];
    self.lblHintsLeft.text = [NSString stringWithFormat:@"Осталось: %d", [GlobalData globalData].loggedInUser.hints];
    
    if (isInitialized)
    {
        return;
    }
    
    self.btnBuyHint1.titleLabel.font = [UIFont fontWithName:@"DINPro-Bold" size:15];
    self.btnBuyHint2.titleLabel.font = self.btnBuyHint1.titleLabel.font;
    self.btnBuyHint3.titleLabel.font = self.btnBuyHint1.titleLabel.font;
    __block NSArray * buttons = @[self.btnBuyHint1, self.btnBuyHint2, self.btnBuyHint3];
    [[StoreManager sharedManager] fetchPricesForHintsWithCompletion:^(NSArray *data, NSError *error) {
        if (data != nil && data.count == 3)
        {
            isInitialized = YES;
        }
        for (int idx = 0; idx < 3; ++idx)
        {
            if (data != nil && [data objectAtIndex:idx] != nil)
            {
                dispatch_async(dispatch_get_main_queue(), ^{
                    UIButton * button = [buttons objectAtIndex:idx];
                    [button setTitle:[data objectAtIndex:idx] forState:UIControlStateNormal];
                });
            }
        }
    }];
}

- (IBAction)handleBuyHintsClick:(id)sender
{
    UIButton * button = sender;
    [[StoreManager sharedManager] purchaseHints:(10 + (button.tag * 10))];
}

#pragma mark EventListenerDelegate
- (void)handleEvent:(Event *)event
{
    if (event.type == EVENT_ME_UPDATED)
    {
        UserData * user = event.data;
        if (user != nil)
        {
            self.lblHintsLeft.text = [NSString stringWithFormat:@"Осталось: %d", user.hints];
        }
    }
}

@end
