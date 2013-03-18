//
//  PuzzlesViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/15/12.
//
//

#import "PuzzlesViewController.h"
#import "BadgeView.h"
#import "EventManager.h"
#import "TileData.h"
#import "PrizeWordNavigationBar.h"
#import "RootViewController.h"
#import "PuzzleSetView.h"
#import "PuzzleSetData.h"
#import "PuzzleData.h"
#import "GlobalData.h"
#import "UserData.h"
#import "APIRequest.h"
#import "SBJson.h"
#import "AppDelegate.h"
#import <StoreKit/StoreKit.h>
#import "NSString+Utils.h"
#import "FISoundEngine.h"

NSString * MONTHS2[] = {@"январь", @"февраль", @"март", @"апрель", @"май", @"июнь", @"июль", @"август", @"сентябрь", @"октябрь", @"ноябрь", @"декабрь"};

NSString * PRODUCTID_PREFIX = @"ru.aipmedia.ios.prizeword.";
NSString * PRODUCTID_HINTS10 = @"ru.aipmedia.ios.prizeword.hints10";
NSString * PRODUCTID_HINTS20 = @"ru.aipmedia.ios.prizeword.hints20";
NSString * PRODUCTID_HINTS30 = @"ru.aipmedia.ios.prizeword.hints30";


@interface PuzzlesViewController ()

-(void)updateNews;

-(void)handleBadgeClick:(id)sender;
-(void)handleBuyClick:(id)sender;
-(void)handleShowMoreClick:(id)sender;
-(void)activateBadges:(PuzzleSetView *)puzzleSetView;

-(void)handleNewsPrev:(id)sender;
-(void)handleNewsNext:(id)sender;
-(void)handleNewsTap:(id)sender;

-(void)resizeBlockView:(UIView *)blockView withInnerView:(UIView *)innerView fromSize:(CGSize)oldSize toSize:(CGSize)newSize;
-(void)switchSetViewToBought:(PuzzleSetView *)puzzleSetView;

-(void)updateArchive:(NSData *)receivedData;
-(void)updateMonthSets:(NSArray*)monthSets;
-(void)updateBaseScores;
-(void)updateHintButton:(PrizeWordButton*)button withProduct:(SKProduct*)product;
-(void)handleSetBoughtWithView:(PuzzleSetView *)puzzleSetView withTransaction:(SKPaymentTransaction *)transaction;
-(void)handleHintsBought:(int)count withTransaction:(SKPaymentTransaction *)transaction;

@end

@implementation PuzzlesViewController

#pragma mark UIViewController lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.title = @"Сканворды";
    puzzlesViewCaption.text = @"Сканворды за ...";   
    
    UISwipeGestureRecognizer * newsRightGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleNewsPrev:)];
    newsRightGestureRecognizer.direction = UISwipeGestureRecognizerDirectionRight;
    UISwipeGestureRecognizer * newsLeftGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleNewsNext:)];
    newsLeftGestureRecognizer.direction = UISwipeGestureRecognizerDirectionLeft;
    UITapGestureRecognizer * newsTapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleNewsTap:)];
    [newsScrollView setGestureRecognizers:[NSArray arrayWithObjects:newsLeftGestureRecognizer, newsRightGestureRecognizer, newsTapGestureRecognizer, nil]];

    [self addSimpleView:newsView];
    [self addFramedView:currentPuzzlesView];
    [self addFramedView:hintsView];
    [self addFramedView:archiveView];
    
    btnBuyHint1.titleLabel.font = [UIFont fontWithName:@"DINPro-Bold" size:15];
    btnBuyHint2.titleLabel.font = btnBuyHint1.titleLabel.font;
    btnBuyHint3.titleLabel.font = btnBuyHint1.titleLabel.font;
    hintsProducts = [NSMutableArray arrayWithObjects:[NSNull null], [NSNull null], [NSNull null], nil];
    
    productsRequest = nil;
    
    buySetSound = [[FISoundEngine sharedEngine] soundNamed:@"buy_set.caf" error:nil];
    toggleSetSound = [[FISoundEngine sharedEngine] soundNamed:@"toggle_set.caf" error:nil];
    
    [self updateNews];
}

- (void)viewDidUnload
{
    newsView = nil;
    currentPuzzlesView = nil;
    hintsView = nil;
    archiveView = nil;
    btnBuyHint1 = nil;
    btnBuyHint2 = nil;
    btnBuyHint3 = nil;
    setToBuyView = nil;
    newsPaginator = nil;
    newsScrollView = nil;
    lblHintsLeft = nil;
    puzzlesViewCaption = nil;
    
    newsLbl1 = nil;
    newsLbl2 = nil;
    newsLbl3 = nil;
    puzzlesTimeLeftBg = nil;
    puzzlesTimeLeftCaption = nil;
    [super viewDidUnload];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_PRODUCT_BOUGHT];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_PRODUCT_FAILED];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_PRODUCT_ERROR];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_PUZZLE_SYNCHRONIZED];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_MONTH_SETS_UPDATED];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_COEFFICIENTS_UPDATED];
    
    [self showActivityIndicator];
    [[GlobalData globalData] loadMe];
    [[GlobalData globalData] loadCoefficients];
    [[GlobalData globalData] loadMonthSets];
    
    APIRequest * request = [APIRequest getRequest:@"puzzles" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        [self updateArchive:receivedData];
    } failCallback:^(NSError *error) {
        NSLog(@"archive error: %@", error.description);
    }];
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [request.params setObject:@"0" forKey:@"from"];
    [request.params setObject:@"100" forKey:@"limit"];
    [request runSilent];

    lblHintsLeft.text = [NSString stringWithFormat:@"Осталось: %d", [GlobalData globalData].loggedInUser.hints];
    
    [self updateNews];
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    NSLog(@"puzzles viewWillDisappear");

    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_PRODUCT_BOUGHT];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_PRODUCT_FAILED];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_PRODUCT_ERROR];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_PUZZLE_SYNCHRONIZED];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_MONTH_SETS_UPDATED];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_COEFFICIENTS_UPDATED];

    if (productsRequest != nil)
    {
        productsRequest.delegate = nil;
        [productsRequest cancel];
        productsRequest = nil;
    }
}

#pragma mark EventListenerDelegate
-(void)handleEvent:(Event *)event
{
    if (event.type == EVENT_PRODUCT_BOUGHT)
    {
        [self hideActivityIndicator];
        
        SKPaymentTransaction * paymentTransaction = event.data;
        NSLog(@"EVENT_PRODUCT_BOUGHT: %@", paymentTransaction.payment.productIdentifier);
        [buySetSound play];

        if ([paymentTransaction.payment.productIdentifier compare:PRODUCTID_HINTS10] == NSOrderedSame)
        {
            [self handleHintsBought:10 withTransaction:paymentTransaction];
        }
        else if ([paymentTransaction.payment.productIdentifier compare:PRODUCTID_HINTS20] == NSOrderedSame)
        {
            [self handleHintsBought:20 withTransaction:paymentTransaction];
        }
        else if ([paymentTransaction.payment.productIdentifier compare:PRODUCTID_HINTS30] == NSOrderedSame)
        {
            [self handleHintsBought:30 withTransaction:paymentTransaction];
        }
        else
        {
            for (UIView * subview in currentPuzzlesView.subviews)
            {
                if ([subview isKindOfClass:[PuzzleSetView class]])
                {
                    PuzzleSetView * puzzleSetView = (PuzzleSetView *)subview;
                    NSLog(@"check productIdentifier: %@ %@ %@", puzzleSetView.puzzleSetData.set_id, puzzleSetView.product.productIdentifier, paymentTransaction.payment.productIdentifier);
                    if (puzzleSetView.product != nil && puzzleSetView.product.productIdentifier != nil && [puzzleSetView.product.productIdentifier compare:paymentTransaction.payment.productIdentifier] == NSOrderedSame)
                    {
                        [self handleSetBoughtWithView:puzzleSetView withTransaction:paymentTransaction];
                        break;
                    }
                }
            }
        }
    }
    else if (event.type == EVENT_PRODUCT_ERROR)
    {
        NSLog(@"EVENT_PRODUCT_ERROR");
        [self hideActivityIndicator];
        NSError * error = event.data;
        UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:@"Ошибка" message:error.description delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alertView show];
    }
    else if (event.type == EVENT_PRODUCT_FAILED)
    {
        NSLog(@"EVENT_PRODUCT_FAILED");
        [self hideActivityIndicator];
        SKPaymentTransaction * paymentTransaction = event.data;
        if (paymentTransaction.error != nil)
        {
            NSLog(@"error: %@", paymentTransaction.error.description);
        }
    }
    else if (event.type == EVENT_PUZZLE_SYNCHRONIZED)
    {
        PuzzleData * puzzleData = event.data;
        if (puzzleData.progress < 1)
        {
            for (UIView * view in currentPuzzlesView.subviews)
            {
                if (![view isKindOfClass:[PuzzleSetView class]])
                {
                    continue;
                }
                PuzzleSetView * puzzleSetView = (PuzzleSetView *)view;
                if ([puzzleSetView.puzzleSetData.set_id compare:puzzleData.puzzleSet.set_id] == NSOrderedSame)
                {
                    for (BadgeView * badge in puzzleSetView.badges)
                    {
                        if ([badge.puzzle.puzzle_id compare:puzzleData.puzzle_id] == NSOrderedSame)
                        {
                            [badge updateWithPuzzle:puzzleData];
                        }
                    }
                    break;
                }
            }
            
            return;
        }
        
        PuzzleSetView * oldView = nil;
        for (UIView * view in currentPuzzlesView.subviews)
        {
            if (![view isKindOfClass:[PuzzleSetView class]])
            {
                continue;
            }
            PuzzleSetView * puzzleSetView = (PuzzleSetView *)view;
            if ([puzzleSetView.puzzleSetData.set_id compare:puzzleData.puzzleSet.set_id] == NSOrderedSame)
            {
                oldView = puzzleSetView;
                break;
            }
        }
        if (oldView == nil)
        {
            return;
        }
        
        PuzzleSetView * puzzleSetView = [PuzzleSetView puzzleSetViewWithData:puzzleData.puzzleSet month:0 showSolved:YES showUnsolved:YES];
        CGSize newSize = puzzleSetView.frame.size;
        CGSize oldSize = oldView.frame.size;
        puzzleSetView.frame = CGRectMake(0, oldView.frame.origin.y, puzzleSetView.frame.size.width, oldSize.height);
        [currentPuzzlesView insertSubview:puzzleSetView aboveSubview:oldView];
        [puzzleSetView.btnShowMore addTarget:self action:@selector(handleShowMoreClick:) forControlEvents:UIControlEventTouchUpInside];
        [self activateBadges:puzzleSetView];
        
        [oldView removeFromSuperview];
        [self resizeBlockView:currentPuzzlesView withInnerView:puzzleSetView fromSize:oldSize toSize:newSize];
    }
    else if (event.type == EVENT_MONTH_SETS_UPDATED)
    {
        [self hideActivityIndicator];
        [self updateMonthSets:[GlobalData globalData].monthSets];
    }
    else if (event.type == EVENT_COEFFICIENTS_UPDATED)
    {
        [self updateBaseScores];
    }
}

#pragma mark update news, update and bought puzzles
-(void)updateNews
{
    APIRequest * newsUpdateRequest = [APIRequest getRequest:@"service_messages" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        if (response.statusCode == 200)
        {
            NSLog(@"news update success: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            
            NSDictionary * messages = [[SBJsonParser new] objectWithData:receivedData];
            NSMutableArray * messagesArray = [NSMutableArray new];
            if ([messages objectForKey:@"message1"] != (id)[NSNull null])
            {
                [messagesArray addObject:[messages objectForKey:@"message1"]];
            }
            if ([messages objectForKey:@"message2"] != (id)[NSNull null])
            {
                [messagesArray addObject:[messages objectForKey:@"message2"]];
            }
            if ([messages objectForKey:@"message3"] != (id)[NSNull null])
            {
                [messagesArray addObject:[messages objectForKey:@"message3"]];
            }
            newsPaginator.numberOfPages = messagesArray.count;
            if (messagesArray.count == 0)
            {
                [self resizeView:newsView newHeight:0 animated:YES];
            }
            if (messagesArray.count >= 1)
            {
                newsLbl1.text = [messagesArray objectAtIndex:0];
            }
            if (messagesArray.count >= 2)
            {
                newsLbl2.text = [messagesArray objectAtIndex:1];
            }
            if (messagesArray.count >= 3)
            {
                newsLbl3.text = [messagesArray objectAtIndex:2];
            }
            [newsScrollView setContentSize:CGSizeMake(newsPaginator.numberOfPages * newsScrollView.frame.size.width, newsScrollView.frame.size.height)];
        }
        else
        {
            NSLog(@"news update failed: %d %@", response.statusCode, [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
        }
    } failCallback:^(NSError *error) {
        NSLog(@"news update error: %@", error.description);
    }];
    [newsUpdateRequest.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [newsUpdateRequest runSilent];
}

-(void)updateMonthSets:(NSArray *)monthSets
{
    BOOL hasUnbought = NO;
    NSMutableSet * productsIds = [NSMutableSet new];

    float yOffset = currentPuzzlesView.frame.size.height;
    while (currentPuzzlesView.subviews.count > 2) {
        UIView * subview = [currentPuzzlesView.subviews objectAtIndex:currentPuzzlesView.subviews.count-1];
        yOffset -= subview.frame.size.height;
        [subview removeFromSuperview];
    }
    for (PuzzleSetData * puzzleSet in monthSets)
    {
        PuzzleSetView * puzzleSetView = [PuzzleSetView puzzleSetViewWithData:puzzleSet month:0 showSolved:YES showUnsolved:YES];
        puzzleSetView.frame = CGRectMake(0, yOffset, puzzleSetView.frame.size.width, puzzleSetView.frame.size.height);
        yOffset += puzzleSetView.frame.size.height;
        [currentPuzzlesView addSubview:puzzleSetView];
        [puzzleSetView.btnShowMore addTarget:self action:@selector(handleShowMoreClick:) forControlEvents:UIControlEventTouchUpInside];
        [self activateBadges:puzzleSetView];
        if (!puzzleSet.bought.boolValue)
        {
            [puzzleSetView.btnBuy addTarget:self action:@selector(handleBuyClick:) forControlEvents:UIControlEventTouchUpInside];
            if (puzzleSetView.puzzleSetData.type.intValue == PUZZLESET_FREE)
            {
                [puzzleSetView.btnBuy setTitle:@"Скачать" forState:UIControlStateNormal];
            }
            else
            {
                [puzzleSetView.btnBuy setTitle:@"" forState:UIControlStateNormal];
                hasUnbought = YES;
                [productsIds addObject:[NSString stringWithFormat:@"%@%@", PRODUCTID_PREFIX, puzzleSet.set_id]];
                NSLog(@"requested: %@%@", PRODUCTID_PREFIX, puzzleSet.set_id);
            }
        }
    }
    puzzlesViewCaption.text = [NSString stringWithFormat:@"Сканворды за %@", MONTHS2[[GlobalData globalData].currentMonth]];
    
    UIView * frame = [currentPuzzlesView.subviews objectAtIndex:1];
    [UIView animateWithDuration:0.3 animations:^{
        frame.frame = CGRectMake(frame.frame.origin.x, frame.frame.origin.y, frame.frame.size.width, yOffset - frame.frame.origin.y * 2);
    }];
    [self resizeView:currentPuzzlesView newHeight:yOffset animated:YES];
    
    // days left set-up
    NSCalendar * calendar = [NSCalendar currentCalendar];
    NSDate * currentDate = [NSDate new];
    NSDateComponents * currentComponents = [calendar components:NSDayCalendarUnit fromDate:currentDate];
    NSRange daysRange = [calendar rangeOfUnit:NSDayCalendarUnit inUnit:NSMonthCalendarUnit forDate:currentDate];
    int daysLeft = daysRange.location + daysRange.length - currentComponents.day;
    puzzlesTimeLeftCaption.text = [NSString stringWithFormat:@"Ост. %d %@", daysLeft, [NSString declesion:daysLeft oneString:@"день" twoString:@"дня" fiveString:@"дней"]];
    puzzlesTimeLeftCaption.hidden = NO;
    puzzlesTimeLeftBg.hidden = NO;
    
    // request information about products
    [self showActivityIndicator];
    [productsIds addObject:PRODUCTID_HINTS10];
    [productsIds addObject:PRODUCTID_HINTS20];
    [productsIds addObject:PRODUCTID_HINTS30];
    productsRequest = [[SKProductsRequest alloc] initWithProductIdentifiers:productsIds];
    productsRequest.delegate = self;
    [productsRequest start];
}

-(void)updateArchive:(NSData *)receivedData
{
    NSLog(@"archive: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
    SBJsonParser * parser = [SBJsonParser new];
    NSDictionary * data = [parser objectWithData:receivedData];
    NSArray * setsData = [data objectForKey:@"sets"];
    
    int lastMonth = 0;
    
    float yOffset = archiveView.frame.size.height;
    while (archiveView.subviews.count > 2)
    {
        UIView * subview = [archiveView.subviews objectAtIndex:archiveView.subviews.count-1];
        yOffset -= subview.frame.size.height;
        [subview removeFromSuperview];
    }
    
    for (NSDictionary * setData in setsData)
    {
        PuzzleSetData * puzzleSet = [PuzzleSetData puzzleSetWithDictionary:setData andUserId:[GlobalData globalData].loggedInUser.user_id];
        puzzleSet.bought = [NSNumber numberWithBool:YES];
        int month = [(NSNumber *)[setData objectForKey:@"month"] intValue];
        int year = [(NSNumber *)[setData objectForKey:@"year"] intValue];
        if (year == [GlobalData globalData].currentYear && month == ([GlobalData globalData].currentMonth + 1))
        {
            continue;
        }
        if (lastMonth != month)
        {
            lastMonth = month;
        }
        else
        {
            month = 0;
        }

        PuzzleSetView * puzzleSetView = [PuzzleSetView puzzleSetViewWithData:puzzleSet month:month showSolved:YES showUnsolved:YES];
        [self activateBadges:puzzleSetView];
        [puzzleSetView.btnShowMore addTarget:self action:@selector(handleShowMoreClick:) forControlEvents:UIControlEventTouchUpInside];
        puzzleSetView.btnShowMore.selected = NO;
        puzzleSetView.frame = CGRectMake(0, yOffset, puzzleSetView.frame.size.width, puzzleSetView.shortSize.height);
        yOffset += puzzleSetView.shortSize.height;
        
        [archiveView addSubview:puzzleSetView];
    }
    
    UIView * frame = [archiveView.subviews objectAtIndex:1];
    [UIView animateWithDuration:0.3 animations:^{
        frame.frame = CGRectMake(frame.frame.origin.x, frame.frame.origin.y, frame.frame.size.width, yOffset - frame.frame.origin.y * 2);
    }];
    [self resizeView:archiveView newHeight:yOffset animated:YES];
}

-(void)updateBaseScores
{
    for (id subview in currentPuzzlesView.subviews)
    {
        if (![subview isKindOfClass:[PuzzleSetView class]])
        {
            continue;
        }
        PuzzleSetView * puzzleSetView = subview;
        if (!puzzleSetView.puzzleSetData.bought.boolValue)
        {
            int minScore = puzzleSetView.puzzleSetData.minScore;
            puzzleSetView.lblScore.text = [NSString stringWithFormat:@" %@", [NSString digitString:minScore]];
        }
    }
}

-(void)productsRequest:(SKProductsRequest *)request didReceiveResponse:(SKProductsResponse *)response
{
    // update sets' prices
    for (UIView * subview in currentPuzzlesView.subviews)
    {
        if (![subview isKindOfClass:[PuzzleSetView class]])
        {
            continue;
        }
        PuzzleSetView * puzzleSetView = (PuzzleSetView *)subview;
        if (puzzleSetView.puzzleSetData.bought.boolValue)
        {
            continue;
        }
        
        if (puzzleSetView.puzzleSetData.type.intValue == PUZZLESET_FREE)
        {
            [puzzleSetView.btnBuy setTitle:@"Скачать" forState:UIControlStateNormal];
            continue;
        }
        for (SKProduct * product in response.products)
        {
            if ([product.productIdentifier rangeOfString:puzzleSetView.puzzleSetData.set_id options:NSCaseInsensitiveSearch].location != NSNotFound)
            {
                NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
                [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
                [formatter setLocale:product.priceLocale];
                NSString *localizedMoneyString = [formatter stringFromNumber:product.price];
                NSLog(@"product %@ %@ %@: %@", puzzleSetView.puzzleSetData.set_id, product.productIdentifier, product.localizedTitle, localizedMoneyString);
                
                [puzzleSetView.btnBuy setTitle:localizedMoneyString forState:UIControlStateNormal];
                puzzleSetView.product = product;
                break;
            }
        }
    }
    
    // update hints' prices
    for (SKProduct * product in response.products)
    {
        if ([product.productIdentifier compare:PRODUCTID_HINTS10] == NSOrderedSame)
        {
            [self updateHintButton:btnBuyHint1 withProduct:product];
        }
        else if ([product.productIdentifier compare:PRODUCTID_HINTS20] == NSOrderedSame)
        {
            [self updateHintButton:btnBuyHint2 withProduct:product];
        }
        else if ([product.productIdentifier compare:PRODUCTID_HINTS30] == NSOrderedSame)
        {
            [self updateHintButton:btnBuyHint3 withProduct:product];
        }
    }

    [self hideActivityIndicator];
}

-(void)updateHintButton:(PrizeWordButton*)button withProduct:(SKProduct*)product
{
    [hintsProducts replaceObjectAtIndex:button.tag withObject:product];
    NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
    [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
    [formatter setLocale:product.priceLocale];
    NSString *localizedMoneyString = [formatter stringFromNumber:product.price];
    NSLog(@"product %@: %@", product.localizedTitle, localizedMoneyString);
    
    [button setTitle:localizedMoneyString forState:UIControlStateNormal];
}

-(void)handleSetBoughtWithView:(PuzzleSetView *)puzzleSetView withTransaction:(SKPaymentTransaction *)transaction
{
    [self showActivityIndicator];
    NSLog(@"buy set: %@ %@", puzzleSetView.puzzleSetData.set_id, transaction.payment.productIdentifier);
    APIRequest * request = [APIRequest postRequest:[NSString stringWithFormat:@"sets/%@/buy", puzzleSetView.puzzleSetData.set_id] successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        [self hideActivityIndicator];
        if (response.statusCode == 200)
        {
            NSLog(@"set bought! %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            [self switchSetViewToBought:puzzleSetView];
            [puzzleSetView.puzzleSetData setBought:[NSNumber numberWithBool:YES]];
            NSError * error;
            [[AppDelegate currentDelegate].managedObjectContext save:&error];
            if (error != nil) {
                NSLog(@"error: %@", error.description);
            }
        }
        else
        {
            NSString * message = [[[SBJsonParser new] objectWithData:receivedData] objectForKey:@"message"];
            if (message == nil)
            {
                message = [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding];
            }
            UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:@"Ошибка" message:message delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
            [alertView show];
        }
    } failCallback:^(NSError *error) {
        NSLog(@"set error: %@", error.description);
        [self hideActivityIndicator];
    }];
    [request.params setObject:puzzleSetView.puzzleSetData.set_id forKey:@"id"];
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    // transaction == nil for free sets
    if (transaction != nil)
    {
        [request.params setObject:transaction.transactionReceipt forKey:@"receipt-data"];
    }
    [request runSilent];
}

-(void)handleHintsBought:(int)count withTransaction:(SKPaymentTransaction *)transaction
{
    APIRequest * request = [APIRequest postRequest:@"hints" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        NSLog(@"hints: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * dict = [parser objectWithData:receivedData];
        [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:[dict objectForKey:@"me"]];
        lblHintsLeft.text = [NSString stringWithFormat:@"Осталось: %d", [GlobalData globalData].loggedInUser.hints];
        [self hideActivityIndicator];
    } failCallback:^(NSError *error) {
        [self hideActivityIndicator];
        NSLog(@"hints error: %@", error.description);
    }];
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [request.params setObject:[NSString stringWithFormat:@"%d", count] forKey:@"hints_change"];
    [request runSilent];
}

#pragma mark user interaction

- (IBAction)handleNewsCloseClick:(id)sender
{
    newsView.autoresizesSubviews = NO;
    newsView.clipsToBounds = YES;
    [self resizeView:newsView newHeight:0 animated:YES];
}

-(void)handleBadgeClick:(id)sender
{
    BadgeView * badge = (BadgeView *)sender;
    PuzzleData * puzzle = badge.puzzle;
    
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_START andData:puzzle]];
}

-(void)handleBuyClick:(id)sender
{
    PuzzleSetView * setView = (PuzzleSetView *)((UIButton *)sender).superview;
    NSLog(@"buy click: %@", setView.puzzleSetData.set_id);
    [self showActivityIndicator];
    
    if (setView.puzzleSetData.type.intValue == PUZZLESET_FREE)
    {
        [self handleSetBoughtWithView:setView withTransaction:nil];
    }
    else
    {
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_REQUEST_PRODUCT andData:setView.product]];
    }
}

-(void)handleShowMoreClick:(id)sender
{
    UIButton * btnShowMore = (UIButton *)sender;

    PuzzleSetView * setView = (PuzzleSetView *)btnShowMore.superview;
    UIView * blockView = setView.superview;
    CGSize oldSize = setView.frame.size;

    btnShowMore.selected = !btnShowMore.selected;
    CGSize newSize = btnShowMore.selected ? setView.fullSize : setView.shortSize;
    [toggleSetSound play];
    
    [self resizeBlockView:blockView withInnerView:setView fromSize:oldSize toSize:newSize];
}

- (IBAction)handleNewsPaginatorChange:(id)sender
{
    NSLog(@"paginator: %d", newsPaginator.currentPage);
    [newsScrollView setContentOffset:CGPointMake(newsPaginator.currentPage * newsScrollView.frame.size.width, 0) animated:YES];
}

-(void)handleNewsPrev:(id)sender
{
    if (newsPaginator.currentPage == 0)
    {
        return;
    }
    newsPaginator.currentPage = newsPaginator.currentPage - 1;
    [self handleNewsPaginatorChange:newsPaginator];
}

-(void)handleNewsNext:(id)sender
{
    if (newsPaginator.currentPage == newsPaginator.numberOfPages - 1)
    {
        return;
    }
    newsPaginator.currentPage = newsPaginator.currentPage + 1;
    [self handleNewsPaginatorChange:newsPaginator];
}

-(void)handleNewsTap:(id)sender
{
    if (newsPaginator.currentPage == newsPaginator.numberOfPages - 1)
    {
        newsPaginator.currentPage = 0;
        [self handleNewsPaginatorChange:newsPaginator];
        return;
    }
    newsPaginator.currentPage = newsPaginator.currentPage + 1;
    [self handleNewsPaginatorChange:newsPaginator];
}

- (IBAction)handleBuyHints:(id)sender
{
    UIButton * button = sender;
    [self showActivityIndicator];
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_REQUEST_PRODUCT andData:[hintsProducts objectAtIndex:button.tag]]];
}

#pragma mark helpers

-(void)switchSetViewToBought:(PuzzleSetView *)puzzleSetView
{
    [self hideActivityIndicator];
    
    UIView * blockView = puzzleSetView.superview;
    CGSize oldSize = puzzleSetView.frame.size;
    
    [puzzleSetView switchToBought];
    CGSize newSize = puzzleSetView.frame.size;
    
    [self resizeBlockView:blockView withInnerView:puzzleSetView fromSize:oldSize toSize:newSize];
    [self activateBadges:puzzleSetView];
}

-(void)resizeBlockView:(UIView *)blockView withInnerView:(UIView *)innerView fromSize:(CGSize)oldSize toSize:(CGSize)newSize
{
    float delta = 0;
    for (UIView * subview in blockView.subviews)
    {
        if ([subview isKindOfClass:[UIImageView class]])
        {
            UIImageView * imageView = (UIImageView *)subview;
            if (imageView.frame.size.height <= blockView.frame.size.height)
            {
                continue;
            }
            [UIView animateWithDuration:0.3 animations:^{
                subview.frame = CGRectMake(subview.frame.origin.x, subview.frame.origin.y, subview.frame.size.width, subview.frame.size.height + (newSize.height - oldSize.height));
            }];
            continue;
        }
        if (subview == innerView)
        {
            delta = newSize.height - oldSize.height;
            subview.frame = CGRectMake(subview.frame.origin.x, subview.frame.origin.y, subview.frame.size.width, oldSize.height);
            [UIView animateWithDuration:0.3 animations:^{
                subview.frame = CGRectMake(subview.frame.origin.x, subview.frame.origin.y, subview.frame.size.width, oldSize.height + delta);
            }];
        }
        else if (delta != 0)
        {
            [UIView animateWithDuration:0.3 animations:^{
                subview.frame = CGRectMake(subview.frame.origin.x, subview.frame.origin.y + delta, subview.frame.size.width, subview.frame.size.height);
            }];
        }
    }
    [self resizeView:blockView newHeight:(blockView.frame.size.height + delta) animated:YES];
}

-(void)activateBadges:(PuzzleSetView *)puzzleSetView
{
    if (puzzleSetView.badges == nil)
    {
        return;
    }
    for (BadgeView * badgeView in puzzleSetView.badges)
    {
        if (badgeView.puzzle.progress < 1)
        {
            badgeView.userInteractionEnabled = YES;
            [badgeView addTarget:self action:@selector(handleBadgeClick:) forControlEvents:UIControlEventTouchUpInside];
        }
        else
        {
            badgeView.userInteractionEnabled = NO;
        }
    }
}

@end
