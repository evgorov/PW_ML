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

NSString * PRODUCTID_HINTS10 = @"ru.aipmedia.prizeword.hints10";
NSString * PRODUCTID_HINTS20 = @"ru.aipmedia.prizeword.hints20";
NSString * PRODUCTID_HINTS30 = @"ru.aipmedia.prizeword.hints30";

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
    __block NSArray * productIDs = @[PRODUCTID_HINTS10, PRODUCTID_HINTS20, PRODUCTID_HINTS30];
    __block NSArray * buttons = @[self.btnBuyHint1, self.btnBuyHint2, self.btnBuyHint3];
    [[DataManager sharedManager] fetchPricesForProductIDs:productIDs completion:^(NSDictionary *data, NSError *error) {
        if (data != nil && data.count == 3)
        {
            isInitialized = YES;
        }
        for (int idx = 0; idx < 3; ++idx) {
            if (data != nil && [data objectForKey:[productIDs objectAtIndex:idx]] != nil)
            {
                dispatch_async(dispatch_get_main_queue(), ^{
                    UIButton * button = [buttons objectAtIndex:idx];
                    [button setTitle:[data objectForKey:[productIDs objectAtIndex:idx]] forState:UIControlStateNormal];
                });
            }
        }
    }];
}

- (IBAction)handleBuyHintsClick:(id)sender
{
    UIButton * button = sender;
    NSArray * productIDs = @[PRODUCTID_HINTS10, PRODUCTID_HINTS20, PRODUCTID_HINTS30];
    NSMutableDictionary * products = [GlobalData globalData].products;
    SKProduct * product = [products objectForKey:[productIDs objectAtIndex:button.tag]];
    if (product != nil)
    {
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_REQUEST_PRODUCT andData:product]];
    }

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
