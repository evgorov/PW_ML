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

NSString * MONTHS2[] = {@"январь", @"февраль", @"март", @"апрель", @"май", @"июнь", @"июль", @"август", @"сентябрь", @"октябрь", @"ноябрь", @"декабрь"};


@interface PuzzlesViewController ()

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
-(void)handleSetBoughtWithView:(PuzzleSetView *)puzzleSetView;

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
    [super viewDidUnload];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self showActivityIndicator];
    [[GlobalData globalData] loadMe];
    [[GlobalData globalData] loadMonthSets:^{
        [self hideActivityIndicator];
        [self updateMonthSets:[GlobalData globalData].monthSets];
    }];
    
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
    
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_PRODUCT_BOUGHT];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_PRODUCT_FAILED];
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_PRODUCT_ERROR];
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];

    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_PRODUCT_BOUGHT];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_PRODUCT_FAILED];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_PRODUCT_ERROR];
}

#pragma mark EventListenerDelegate
-(void)handleEvent:(Event *)event
{
    if (event.type == EVENT_PRODUCT_BOUGHT)
    {
        NSLog(@"EVENT_PRODUCT_BOUGHT");
        [self hideActivityIndicator];
        
        SKPaymentTransaction * paymentTransaction = event.data;
        
        for (UIView * subview in currentPuzzlesView.subviews)
        {
            if ([subview isKindOfClass:[PuzzleSetView class]])
            {
                PuzzleSetView * puzzleSetView = (PuzzleSetView *)subview;
                if ([puzzleSetView.product.productIdentifier compare:paymentTransaction.payment.productIdentifier] == NSOrderedSame)
                {
                    [self handleSetBoughtWithView:puzzleSetView];
                    break;
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
}

#pragma mark update and bought puzzles

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
    for (PuzzleSetData * puzzleSet in monthSets) {
        PuzzleSetView * puzzleSetView = [PuzzleSetView puzzleSetViewWithData:puzzleSet month:0 showSolved:NO showUnsolved:YES];
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
                [puzzleSetView.btnBuy setTitle:@"Обновление" forState:UIControlStateNormal];
                hasUnbought = YES;
//                [productsIds addObject:puzzleSet.set_id];
                [productsIds addObject:@"ru.aipmedia.ios.prizeword.set0"];
            }
        }
    }
    puzzlesViewCaption.text = [NSString stringWithFormat:@"Сканворды за %@", MONTHS2[[GlobalData globalData].currentMonth]];
    
    UIView * frame = [currentPuzzlesView.subviews objectAtIndex:1];
    [UIView animateWithDuration:0.3 animations:^{
        frame.frame = CGRectMake(frame.frame.origin.x, frame.frame.origin.y, frame.frame.size.width, yOffset - frame.frame.origin.y * 2);
    }];
    [self resizeView:currentPuzzlesView newHeight:yOffset animated:YES];
    
    if (hasUnbought)
    {
        [self showActivityIndicator];
        
        SKProductsRequest * productsRequest = [[SKProductsRequest alloc] initWithProductIdentifiers:productsIds];
        productsRequest.delegate = self;
        [productsRequest start];
    }
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
        PuzzleSetData * puzzleSet = [PuzzleSetData puzzleSetWithDictionary:setData andUserId:[GlobalData globalData].loggedInUser.provider_id];
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
        puzzleSetView.frame = CGRectMake(0, yOffset, puzzleSetView.frame.size.width, puzzleSetView.frame.size.height);
        yOffset += puzzleSetView.frame.size.height;
        
        [archiveView addSubview:puzzleSetView];
    }
    
    UIView * frame = [archiveView.subviews objectAtIndex:1];
    [UIView animateWithDuration:0.3 animations:^{
        frame.frame = CGRectMake(frame.frame.origin.x, frame.frame.origin.y, frame.frame.size.width, yOffset - frame.frame.origin.y * 2);
    }];
    [self resizeView:archiveView newHeight:yOffset animated:YES];
}

-(void)productsRequest:(SKProductsRequest *)request didReceiveResponse:(SKProductsResponse *)response
{
    for (UIView * subview in currentPuzzlesView.subviews)
    {
        if (![subview isKindOfClass:[PuzzleSetView class]])
        {
            continue;
        }
        PuzzleSetView * puzzleSetView = (PuzzleSetView *)subview;
        
        if (puzzleSetView.puzzleSetData.type.intValue == PUZZLESET_FREE)
        {
            [puzzleSetView.btnBuy setTitle:@"Скачать" forState:UIControlStateNormal];
        }
        else
        {
            for (SKProduct * product in response.products)
            {
                //            if ([puzzleSetView.puzzleSetData.set_id compare:product.productIdentifier] == NSOrderedSame)
                {
                    NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
                    [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
                    [formatter setLocale:product.priceLocale];
                    NSString *localizedMoneyString = [formatter stringFromNumber:product.price];
                    NSLog(@"product %@: %@", product.localizedTitle, localizedMoneyString);
                    
                    [puzzleSetView.btnBuy setTitle:localizedMoneyString forState:UIControlStateNormal];
                    puzzleSetView.product = product;
                    break;
                }
            }
        }
    }

    [self hideActivityIndicator];
}

-(void)handleSetBoughtWithView:(PuzzleSetView *)puzzleSetView
{
    [self showActivityIndicator];
    APIRequest * request = [APIRequest postRequest:[NSString stringWithFormat:@"sets/%@/buy", puzzleSetView.puzzleSetData.set_id] successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        NSLog(@"set bought! %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
        [self hideActivityIndicator];
        [self switchSetViewToBought:puzzleSetView];
        [puzzleSetView.puzzleSetData setBought:[NSNumber numberWithBool:YES]];
        NSError * error;
        [[AppDelegate currentDelegate].managedObjectContext save:&error];
        if (error != nil) {
            NSLog(@"error: %@", error.description);
        }
    } failCallback:^(NSError *error) {
        NSLog(@"set error: %@", error.description);
        [self hideActivityIndicator];
    }];
    [request.params setObject:puzzleSetView.puzzleSetData.set_id forKey:@"id"];
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
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
    [self showActivityIndicator];
    
    if (setView.puzzleSetData.type.intValue == PUZZLESET_FREE)
    {
        [self handleSetBoughtWithView:setView];
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
    [request.params setObject:[NSString stringWithFormat:@"%d", button.tag] forKey:@"hints_change"];
    [request runSilent];
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
        [badgeView addTarget:self action:@selector(handleBadgeClick:) forControlEvents:UIControlEventTouchUpInside];
    }
}

@end
