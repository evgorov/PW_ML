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
#import "PuzzleSetPackData.h"
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
#import "PrizewordStoreObserver.h"
#import "UserDataManager.h"
#import "DataManager.h"
#import "NewsCell.h"
#import "CurrentPuzzlesCell.h"
#import "PuzzleSetCell.h"
#import "DataContext.h"

NSString * PRODUCTID_PREFIX = @"ru.aipmedia.prizeword.";
NSString * PRODUCTID_HINTS10 = @"ru.aipmedia.prizeword.hints10";
NSString * PRODUCTID_HINTS20 = @"ru.aipmedia.prizeword.hints20";
NSString * PRODUCTID_HINTS30 = @"ru.aipmedia.prizeword.hints30";

// default in IB
const int TAG_STATIC_VIEWS = 0;
const int TAG_DYNAMIC_VIEWS = 101;

@interface PuzzleSetState : NSObject

@property () BOOL isShownFull;
@property () float height;

@end

@implementation PuzzleSetState

@synthesize isShownFull;
@synthesize height;

- (id)init
{
    self = [super init];
    if (self != nil)
    {
        isShownFull = YES;
        height = 0;
    }
    return self;
}

@end

@interface PuzzlesViewController ()
{
    __weak IBOutlet UITableView *tableView;
    NSMutableArray * currentPuzzleSetStates;
    
    IBOutlet UIView *hintsView;
    IBOutlet UIView *archiveView;
    IBOutlet UIView *setToBuyView;
    
    IBOutlet PrizeWordButton *btnBuyHint1;
    IBOutlet PrizeWordButton *btnBuyHint2;
    IBOutlet PrizeWordButton *btnBuyHint3;
    IBOutlet UILabel *lblHintsLeft;
    NSMutableArray * hintsProducts;
    SKProductsRequest * productsRequest;
    
    BOOL showNews;
    
    int archiveLastMonth;
    int archiveLastYear;
    BOOL archiveNeedLoading;
    BOOL archiveLoading;
    
    FISound * buySetSound;
    FISound * openSetSound;
    FISound * closeSetSound;
}

-(void)handleBadgeClick:(id)sender;
-(void)handleBuyClick:(id)sender;
-(void)handleShowMoreClick:(id)sender;
-(void)activateBadges:(PuzzleSetView *)puzzleSetView;

-(void)resizeBlockView:(UIView *)blockView withInnerView:(UIView *)innerView fromSize:(CGSize)oldSize toSize:(CGSize)newSize;
-(void)switchSetViewToBought:(PuzzleSetView *)puzzleSetView;

-(void)updateArchive:(NSArray *)sets;
//-(void)updateMonthSets:(NSArray *)monthSets;
-(void)updateBaseScores;
-(void)updateHintButton:(PrizeWordButton*)button withProduct:(SKProduct*)product;
-(void)handleSetBoughtWithView:(PuzzleSetView *)puzzleSetView withTransaction:(SKPaymentTransaction *)transaction;
-(void)handleHintsBought:(int)count withTransaction:(SKPaymentTransaction *)transaction;

-(void)setupKnownPrices;

-(void)loadArchive;

@end

@implementation PuzzlesViewController

#pragma mark UIViewController lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    currentPuzzleSetStates = [NSMutableArray new];
    [scrollView removeFromSuperview];
    tableView.backgroundView = nil;
    UIImage * bgImage = [UIImage imageNamed:@"bg_dark_tile.jpg"];
    tableView.backgroundColor = [UIColor colorWithPatternImage:bgImage];
    [tableView registerNib:[UINib nibWithNibName:@"NewsCell" bundle:[NSBundle mainBundle]] forCellReuseIdentifier:@"newsCell"];
    [tableView registerNib:[UINib nibWithNibName:@"CurrentPuzzlesCell" bundle:[NSBundle mainBundle]] forCellReuseIdentifier:@"currentPuzzlesCell"];
    [tableView registerNib:[UINib nibWithNibName:@"PuzzleSetCell" bundle:[NSBundle mainBundle]] forCellReuseIdentifier:@"puzzleSetCell"];
    
    showNews = YES;
    
    self.title = NSLocalizedString(@"TITLE_PUZZLES", @"Title of screen with puzzles");
    
    btnBuyHint1.titleLabel.font = [UIFont fontWithName:@"DINPro-Bold" size:15];
    btnBuyHint2.titleLabel.font = btnBuyHint1.titleLabel.font;
    btnBuyHint3.titleLabel.font = btnBuyHint1.titleLabel.font;
    hintsProducts = [NSMutableArray arrayWithObjects:[NSNull null], [NSNull null], [NSNull null], nil];
    
    productsRequest = nil;
    
    buySetSound = [[FISoundEngine sharedEngine] soundNamed:@"buy_set.caf" error:nil];
    openSetSound = [[FISoundEngine sharedEngine] soundNamed:@"open_set.caf" error:nil];
    closeSetSound = [[FISoundEngine sharedEngine] soundNamed:@"close_set.caf" error:nil];
    
    archiveLastMonth = [GlobalData globalData].currentMonth;
    archiveLastYear = [GlobalData globalData].currentYear;
    archiveLoading = NO;
    archiveNeedLoading = YES;
}

- (void)viewDidUnload
{
    currentPuzzleSetStates = nil;
    hintsView = nil;
    archiveView = nil;
    btnBuyHint1 = nil;
    btnBuyHint2 = nil;
    btnBuyHint3 = nil;
    setToBuyView = nil;
    lblHintsLeft = nil;
    
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
    [[EventManager sharedManager] registerListener:self forEventType:EVENT_ME_UPDATED];
    
//    [self showActivityIndicator];
    [[GlobalData globalData] loadMe];
    [[GlobalData globalData] loadCoefficients];
    [[GlobalData globalData] loadMonthSets];
    
    [self loadArchive];
    
    lblHintsLeft.text = [NSString stringWithFormat:@"Осталось: %d", [GlobalData globalData].loggedInUser.hints];
    
    NSLog(@"puzzles view controller: %f %f", self.view.bounds.size.width, self.view.bounds.size.height);
    
//    scrollView.delegate = self;
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
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_ME_UPDATED];

    if (productsRequest != nil)
    {
        productsRequest.delegate = nil;
        [productsRequest cancel];
        productsRequest = nil;
    }
//    scrollView.delegate = nil;
}

-(void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    
//    scrollView.contentOffset = CGPointZero;
}

#pragma mark EventListenerDelegate
-(void)handleEvent:(Event *)event
{
    if (event.type == EVENT_PRODUCT_BOUGHT)
    {
//        [self hideActivityIndicator];
        
        SKPaymentTransaction * paymentTransaction = event.data;
        NSLog(@"EVENT_PRODUCT_BOUGHT: %@", paymentTransaction.payment.productIdentifier);

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
            /*
            for (UIView * subview in currentPuzzlesView.subviews)
            {
                if ([subview isKindOfClass:[PuzzleSetView class]])
                {
                    PuzzleSetView * puzzleSetView = (PuzzleSetView *)subview;
//                    NSLog(@"check productIdentifier: %@ %@ %@", puzzleSetView.puzzleSetData.set_id, puzzleSetView.product.productIdentifier, paymentTransaction.payment.productIdentifier);
                    if (puzzleSetView.product != nil && puzzleSetView.product.productIdentifier != nil && [puzzleSetView.product.productIdentifier compare:paymentTransaction.payment.productIdentifier] == NSOrderedSame)
                    {
                        [self handleSetBoughtWithView:puzzleSetView withTransaction:paymentTransaction];
                        break;
                    }
                }
            }
            */
        }
    }
    else if (event.type == EVENT_PRODUCT_ERROR)
    {
        NSLog(@"EVENT_PRODUCT_ERROR");
//        [self hideActivityIndicator];
        NSError * error = event.data;
        UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:error.localizedDescription delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alertView show];
    }
    else if (event.type == EVENT_PRODUCT_FAILED)
    {
        NSLog(@"EVENT_PRODUCT_FAILED");
//        [self hideActivityIndicator];
        SKPaymentTransaction * paymentTransaction = event.data;
        if (paymentTransaction.error != nil)
        {
            NSLog(@"error: %@", paymentTransaction.error.description);
        }
    }
    else if (event.type == EVENT_PUZZLE_SYNCHRONIZED)
    {
        PuzzleData * puzzleData = event.data;
        NSLog(@"handle puzzle %@ synchronization", puzzleData.name);
        
        PuzzleSetView * oldView = nil;
        /*
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
        if (oldView != nil)
        {
            // current puzzles
            PuzzleSetView * puzzleSetView = [PuzzleSetView puzzleSetViewWithData:puzzleData.puzzleSet month:0 showSolved:YES showUnsolved:YES];
            puzzleSetView.tag = TAG_DYNAMIC_VIEWS;
            CGSize newSize = puzzleSetView.frame.size;
            CGSize oldSize = oldView.frame.size;
            puzzleSetView.frame = CGRectIntegral(CGRectMake(0, oldView.frame.origin.y, puzzleSetView.frame.size.width, oldSize.height));
            [currentPuzzlesView insertSubview:puzzleSetView aboveSubview:oldView];
            [puzzleSetView.btnShowMore addTarget:self action:@selector(handleShowMoreClick:) forControlEvents:UIControlEventTouchUpInside];
            [self activateBadges:puzzleSetView];
            
            [oldView removeFromSuperview];
            [self resizeBlockView:currentPuzzlesView withInnerView:puzzleSetView fromSize:oldSize toSize:newSize];
            return;
        }
        else
         */
        {
            // archive puzzles
            for (UIView * view in archiveView.subviews)
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
            PuzzleSetView * puzzleSetView = [PuzzleSetView puzzleSetViewWithData:puzzleData.puzzleSet month:oldView.month showSolved:YES showUnsolved:YES];
            puzzleSetView.tag = TAG_DYNAMIC_VIEWS;
            CGSize newSize = puzzleSetView.frame.size;
            CGSize oldSize = oldView.frame.size;
            puzzleSetView.frame = CGRectIntegral(CGRectMake(0, oldView.frame.origin.y, puzzleSetView.frame.size.width, oldSize.height));
            [archiveView insertSubview:puzzleSetView aboveSubview:oldView];
            [puzzleSetView.btnShowMore addTarget:self action:@selector(handleShowMoreClick:) forControlEvents:UIControlEventTouchUpInside];
            puzzleSetView.btnShowMore.selected = NO;
            [self activateBadges:puzzleSetView];
            
            [oldView removeFromSuperview];
            if (fabs(oldSize.height - newSize.height) > 0.01)
            {
//                [self resizeBlockView:currentPuzzlesView withInnerView:puzzleSetView fromSize:oldSize toSize:newSize];
            }
            return;
            
        }
        
    }
    else if (event.type == EVENT_MONTH_SETS_UPDATED)
    {
//        [self hideActivityIndicator];
        [tableView reloadData];
//        [self updateMonthSets:[GlobalData globalData].monthSets];
    }
    else if (event.type == EVENT_COEFFICIENTS_UPDATED)
    {
        [self updateBaseScores];
    }
    else if (event.type == EVENT_ME_UPDATED)
    {
        UserData * me = event.data;
        lblHintsLeft.text = [NSString stringWithFormat:@"Осталось: %d", me.hints];
    }
}

#pragma mark update and bought puzzles
/*
-(void)updateMonthSets:(NSArray *)monthSets
{
    BOOL hasUnbought = NO;
    NSMutableSet * productsIds = [NSMutableSet new];

    float yOffset = currentPuzzlesView.frame.size.height;
    NSMutableArray * subviewsToDelete = [NSMutableArray arrayWithCapacity:currentPuzzlesView.subviews.count];
    for (UIView * subview in currentPuzzlesView.subviews)
    {
        if (subview.tag == TAG_DYNAMIC_VIEWS)
        {
            [subviewsToDelete addObject:subview];
        }
    }
    for (UIView * subview in subviewsToDelete)
    {
        yOffset -= subview.frame.size.height;
        [subview removeFromSuperview];
    }
    for (PuzzleSetData * puzzleSet in monthSets)
    {
        PuzzleSetView * puzzleSetView = [PuzzleSetView puzzleSetViewWithData:puzzleSet month:0 showSolved:YES showUnsolved:YES];
        puzzleSetView.tag = TAG_DYNAMIC_VIEWS;
        puzzleSetView.frame = CGRectIntegral(CGRectMake(0, yOffset, puzzleSetView.frame.size.width, puzzleSetView.frame.size.height));
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

    
    UIView * frame = [currentPuzzlesView.subviews objectAtIndex:1];
    [UIView animateWithDuration:0.3 animations:^{
        frame.frame = CGRectIntegral(CGRectMake(frame.frame.origin.x, frame.frame.origin.y, frame.frame.size.width, yOffset - frame.frame.origin.y * 2));
    }];
//    [self resizeView:currentPuzzlesView newHeight:yOffset animated:YES];
    
    
    [self setupKnownPrices];
    // request information about products
    [productsIds addObject:PRODUCTID_HINTS10];
    [productsIds addObject:PRODUCTID_HINTS20];
    [productsIds addObject:PRODUCTID_HINTS30];
    productsRequest = [[SKProductsRequest alloc] initWithProductIdentifiers:productsIds];
    productsRequest.delegate = self;
    [productsRequest start];
}
 */

-(void)updateArchive:(NSArray *)sets
{
    NSLog(@"update archive. sets count: %d", sets.count);
    
    float yOffset = archiveView.frame.size.height;
    BOOL added = NO;
    for (PuzzleSetData * puzzleSet in sets)
    {
        if (![puzzleSet.bought boolValue])
        {
            continue;
        }
        if (puzzleSet.puzzleSetPack.year.intValue == [GlobalData globalData].currentYear && puzzleSet.puzzleSetPack.month.intValue == [GlobalData globalData].currentMonth)
        {
            continue;
        }
        int month = puzzleSet.puzzleSetPack.month.intValue;
        if (added)
        {
            month = 0;
        }
        added = YES;

        PuzzleSetView * puzzleSetView = [PuzzleSetView puzzleSetViewWithData:puzzleSet month:month showSolved:YES showUnsolved:YES];
        puzzleSetView.tag = TAG_DYNAMIC_VIEWS;
        [self activateBadges:puzzleSetView];
        [puzzleSetView.btnShowMore addTarget:self action:@selector(handleShowMoreClick:) forControlEvents:UIControlEventTouchUpInside];
        puzzleSetView.btnShowMore.selected = NO;
        puzzleSetView.frame = CGRectIntegral(CGRectMake(0, yOffset, puzzleSetView.frame.size.width, puzzleSetView.shortSize.height));
        yOffset += puzzleSetView.shortSize.height;
        
        [archiveView addSubview:puzzleSetView];
    }
    
    if (!added) {
        archiveLoading = NO;
        [self loadArchive];
    }
    else
    {
        UIView * frame = [archiveView.subviews objectAtIndex:1];
        [UIView animateWithDuration:0.3 animations:^{
            frame.frame = CGRectIntegral(CGRectMake(frame.frame.origin.x, frame.frame.origin.y, frame.frame.size.width, yOffset - frame.frame.origin.y * 2));
        }];
        
//        [self resizeView:archiveView newHeight:yOffset animated:YES];
    }
    NSLog(@"update archive finished");
}

-(void)updateBaseScores
{
    /*
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
     */
}

-(void)productsRequest:(SKProductsRequest *)request didReceiveResponse:(SKProductsResponse *)response
{
    for (SKProduct * product in response.products)
    {
        [[GlobalData globalData].products setObject:product forKey:product.productIdentifier];
    }
    [self setupKnownPrices];
}

-(void)setupKnownPrices
{
    /*
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
        SKProduct * product = [[GlobalData globalData].products objectForKey:[NSString stringWithFormat:@"%@%@", PRODUCTID_PREFIX, puzzleSetView.puzzleSetData.set_id]];
        if (product != nil)
        {
            NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
            [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
            [formatter setLocale:product.priceLocale];
            NSString *localizedMoneyString = [formatter stringFromNumber:product.price];
            
            [puzzleSetView.btnBuy setTitle:localizedMoneyString forState:UIControlStateNormal];
            puzzleSetView.product = product;
        }
    }
    
    // update hints' prices
    SKProduct * product = [[GlobalData globalData].products objectForKey:PRODUCTID_HINTS10];
    [self updateHintButton:btnBuyHint1 withProduct:product];
    product = [[GlobalData globalData].products objectForKey:PRODUCTID_HINTS20];
    [self updateHintButton:btnBuyHint2 withProduct:product];
    product = [[GlobalData globalData].products objectForKey:PRODUCTID_HINTS30];
    [self updateHintButton:btnBuyHint3 withProduct:product];
    */
}

-(void)loadArchive
{
    if (!archiveLoading && archiveNeedLoading)
    {
        archiveNeedLoading = NO;
        
        NSCalendar * calendar = [NSCalendar currentCalendar];
        NSDateComponents * components = [calendar components:NSYearCalendarUnit|NSMonthCalendarUnit fromDate:[GlobalData globalData].loggedInUser.createdAt];
        if ([components year] > archiveLastYear || ([components year] == archiveLastYear && [components month] >= archiveLastMonth))
        {
            return;
        }

        if (--archiveLastMonth < 1)
        {
            archiveLastMonth = 12;
            --archiveLastYear;
        }
        
        NSLog(@"loading archive for %02d.%d", archiveLastMonth, archiveLastYear);
        archiveLoading = YES;

        [[DataManager sharedManager] fetchArchiveSetsForMonth:archiveLastMonth year:archiveLastYear completion:^(NSArray *data, NSError *error) {
            NSLog(@"fetch result");
            if (data != nil && data.count > 0)
            {
                PuzzleSetData * puzzleSet = [data lastObject];
                NSAssert(puzzleSet.managedObjectContext != nil, @"managed object context of managed object in nil");
                [puzzleSet.managedObjectContext save:nil];
            }
            double delayInSeconds = 0.3;
            dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
            dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
                archiveLoading = NO;
                [self loadArchive];
            });
            if (data != nil) {
                __block NSMutableArray * objectIDs = [NSMutableArray arrayWithCapacity:data.count];
                for (NSManagedObject * object in data) {
                    [objectIDs addObject:object.objectID];
                }
                dispatch_async(dispatch_get_main_queue(), ^{
                    NSMutableArray * objects = [NSMutableArray arrayWithCapacity:objectIDs.count];
                    for (NSManagedObjectID * objectID in objectIDs)
                    {
                        [objects addObject:[[DataContext currentContext] objectWithID:objectID]];
                    }
                    [self updateArchive:objects];
                });
            }
        }];
    }
}

-(void)updateHintButton:(PrizeWordButton*)button withProduct:(SKProduct*)product
{
    if (product != nil)
    {
        [hintsProducts replaceObjectAtIndex:button.tag withObject:product];
        NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
        [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
        [formatter setLocale:product.priceLocale];
        NSString *localizedMoneyString = [formatter stringFromNumber:product.price];
        
        [button setTitle:localizedMoneyString forState:UIControlStateNormal];
    }
    else
    {
        [button setTitle:@"" forState:UIControlStateNormal];
    }
}

-(void)handleSetBoughtWithView:(PuzzleSetView *)puzzleSetView withTransaction:(SKPaymentTransaction *)transaction
{
//    [self showActivityIndicator];
    NSLog(@"buy set: %@", puzzleSetView.puzzleSetData.set_id);
    APIRequest * request = [APIRequest postRequest:[NSString stringWithFormat:@"sets/%@/buy", puzzleSetView.puzzleSetData.set_id] successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        
        NSLog(@"set bought! %@", puzzleSetView.puzzleSetData.set_id);
//        [self hideActivityIndicator];
        if (response.statusCode == 200)
        {
//            [self showActivityIndicator];
            APIRequest * puzzlesRequest = [APIRequest getRequest:@"user_puzzles" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
                NSLog(@"puzzles loaded!");
                [buySetSound play];
//                [self hideActivityIndicator];
                
                NSArray * puzzlesData = [[SBJsonParser new] objectWithData:receivedData];
                for (NSDictionary * puzzleData in puzzlesData)
                {
                    PuzzleData * puzzle = [PuzzleData puzzleWithDictionary:puzzleData andUserId:[GlobalData globalData].loggedInUser.user_id];
                    if (puzzle != nil)
                    {
                        [puzzleSetView.puzzleSetData addPuzzlesObject:puzzle];
                    }
                }
                
                [puzzleSetView.puzzleSetData setBought:[NSNumber numberWithBool:YES]];
                NSAssert(puzzleSetView.puzzleSetData.managedObjectContext != nil, @"managed object context of managed object in nil");
                [puzzleSetView.puzzleSetData.managedObjectContext save:nil];
                [closeSetSound play];
                [self switchSetViewToBought:puzzleSetView];
                NSLog(@"view for puzzles created!");
            } failCallback:^(NSError *error) {
                [(PrizewordStoreObserver *)[AppDelegate storeObserver] setShouldIgnoreWarnings:YES];
                NSLog(@"puzzles error: %@", error.description);
//                [self hideActivityIndicator];
            }];
            
            [puzzlesRequest.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
            [puzzlesRequest.params setObject:puzzleSetView.puzzleSetData.puzzle_ids forKey:@"ids"];
            [puzzlesRequest runUsingCache:NO silentMode:NO];
        }
        else if (![(PrizewordStoreObserver *)[AppDelegate storeObserver] shouldIgnoreWarnings])
        {
            if (response.statusCode >= 400 && response.statusCode < 500)
            {
                [(PrizewordStoreObserver *)[AppDelegate storeObserver] setShouldIgnoreWarnings:YES];
                NSDictionary * data = [[SBJsonParser new] objectWithData:receivedData];
                NSString * message = [data objectForKey:@"message"];
                if (message == nil)
                {
                    message = NSLocalizedString(@"Unknown error", @"Unknown error on server");
                }
                UIAlertView * alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:message delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                [alert show];
                return;
            }
            
        }
    } failCallback:^(NSError *error) {
        NSLog(@"set error: %@", error.description);
//        [self hideActivityIndicator];
    }];
    [request.params setObject:puzzleSetView.puzzleSetData.set_id forKey:@"id"];
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    // transaction == nil for free sets
    if (transaction != nil)
    {
        [request.params setObject:transaction.transactionReceipt forKey:@"receipt-data"];
    }
    [request runUsingCache:NO silentMode:YES];
}

-(void)handleHintsBought:(int)count withTransaction:(SKPaymentTransaction *)transaction
{
    [[UserDataManager sharedManager] addHints:count];
}

#pragma mark user interaction

- (IBAction)handleNewsCloseClick:(id)sender
{
    showNews = NO;
    [tableView beginUpdates];
    [tableView endUpdates];
}

-(void)handleBadgeClick:(id)sender
{
    BadgeView * badge = (BadgeView *)sender;
    PuzzleData * puzzle = badge.puzzle;
    if (puzzle.puzzleSet.puzzleSetPack.month.intValue != [GlobalData globalData].currentMonth || puzzle.puzzleSet.puzzleSetPack.year.intValue != [GlobalData globalData].currentYear)
    {
        archiveNeedLoading = YES;
        archiveLastMonth = [GlobalData globalData].currentMonth;
        archiveLastYear = [GlobalData globalData].currentYear;

        float yOffset = archiveView.frame.size.height;
        NSMutableArray * subviewToDelete = [NSMutableArray arrayWithCapacity:archiveView.subviews.count];
        for (UIView * subview in archiveView.subviews)
        {
            if (subview.tag == TAG_DYNAMIC_VIEWS)
            {
                [subviewToDelete addObject:subview];
            }
        }
        for (UIView * subview in subviewToDelete)
        {
            yOffset -= subview.frame.size.height;
            [subview removeFromSuperview];
        }
//        [self resizeView:archiveView newHeight:yOffset animated:YES];
    }
    
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_START andData:puzzle]];
}

-(void)handleBuyClick:(id)sender
{
    PuzzleSetView * setView = (PuzzleSetView *)((UIButton *)sender).superview;
    NSLog(@"buy click: %@", setView.puzzleSetData.set_id);
//    [self showActivityIndicator];
    
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
    btnShowMore.selected = !btnShowMore.selected;
    CGSize newSize = btnShowMore.selected ? setView.fullSize : setView.shortSize;
    if (btnShowMore.selected)
    {
        [openSetSound play];
    }
    else
    {
        [closeSetSound play];
    }
    int idx = btnShowMore.tag;
    if (idx < 0)
    {
        idx = -idx - 1;
    }
    else
    {
        PuzzleSetState * state = [currentPuzzleSetStates objectAtIndex:idx];
        state.height = newSize.height;
    }
    [tableView beginUpdates];
    [tableView endUpdates];
}


- (IBAction)handleBuyHints:(id)sender
{
    UIButton * button = sender;
//    [self showActivityIndicator];
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_REQUEST_PRODUCT andData:[hintsProducts objectAtIndex:button.tag]]];
}

#pragma mark helpers

-(void)switchSetViewToBought:(PuzzleSetView *)puzzleSetView
{
//    [self hideActivityIndicator];
    
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
                subview.frame = CGRectIntegral(CGRectMake(subview.frame.origin.x, subview.frame.origin.y, subview.frame.size.width, subview.frame.size.height + (newSize.height - oldSize.height)));
            }];
            continue;
        }
        if (subview == innerView)
        {
            delta = newSize.height - oldSize.height;
            subview.frame = CGRectIntegral(CGRectMake(subview.frame.origin.x, subview.frame.origin.y, subview.frame.size.width, oldSize.height));
            [UIView animateWithDuration:0.3 animations:^{
                subview.frame = CGRectIntegral(CGRectMake(subview.frame.origin.x, subview.frame.origin.y, subview.frame.size.width, oldSize.height + delta));
            }];
        }
        else if (delta != 0)
        {
            [UIView animateWithDuration:0.3 animations:^{
                subview.frame = CGRectIntegral(CGRectMake(subview.frame.origin.x, subview.frame.origin.y + delta, subview.frame.size.width, subview.frame.size.height));
            }];
        }
    }
//    [self resizeView:blockView newHeight:(blockView.frame.size.height + delta) animated:YES];
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

#pragma mark UIScrollViewDelegate

-(void)scrollViewDidScroll:(UIScrollView *)scrollView_
{
    if (scrollView_.contentOffset.y + scrollView_.frame.size.height + 100 > scrollView_.contentSize.height)
    {
        archiveNeedLoading = YES;
        [self loadArchive];
    }
    else
    {
        archiveNeedLoading = NO;
    }
}

#pragma mark UITableViewDataSource and UITableViewDelegate

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (section == 0)
    {
        return 1;
    }
    else if (section == 1)
    {
        if ([GlobalData globalData].monthSets == nil)
        {
            return 1;
        }
        return [GlobalData globalData].monthSets.count + 1;
    }
    return 0;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 0)
    {
        if (!showNews)
        {
            return 0;
        }
        return [NewsCell height];
    }
    else if (indexPath.section == 1)
    {
        if (indexPath.row == 0)
        {
            if ([GlobalData globalData].monthSets != nil && [GlobalData globalData].monthSets.count > 0)
            {
                return [CurrentPuzzlesCell height] * 0.6f;
            }
            return  [CurrentPuzzlesCell height];
        }
        if (currentPuzzleSetStates.count >= indexPath.row)
        {
            float height = [(PuzzleSetState *)[currentPuzzleSetStates objectAtIndex:indexPath.row - 1] height];
            NSLog(@"row height: %0.0f", height);
            return height;
        }
        return [PuzzleSetCell minHeight];
    }
    return 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView_ cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 0)
    {
        NewsCell * cell = [tableView dequeueReusableCellWithIdentifier:@"newsCell"];
        [cell.btnClose addTarget:self action:@selector(handleNewsCloseClick:) forControlEvents:UIControlEventTouchUpInside];
        [cell setup];
        return cell;
    }
    else if (indexPath.section == 1)
    {
        if (indexPath.row == 0)
        {
            CurrentPuzzlesCell * cell = [tableView dequeueReusableCellWithIdentifier:@"currentPuzzlesCell"];
            
            if ([GlobalData globalData].monthSets != nil)
            {
                NSCalendar * calendar = [NSCalendar currentCalendar];
                NSDate * currentDate = [NSDate date];
                NSDateComponents * currentComponents = [calendar components:NSDayCalendarUnit|NSMonthCalendarUnit|NSYearCalendarUnit|NSHourCalendarUnit|NSMinuteCalendarUnit|NSSecondCalendarUnit fromDate:currentDate];
                [currentComponents setMonth:[GlobalData globalData].currentMonth];
                [currentComponents setYear:[GlobalData globalData].currentYear];
                [currentComponents setDay:[GlobalData globalData].currentDay];
                currentDate = [calendar dateFromComponents:currentComponents];
                NSRange daysRange = [calendar rangeOfUnit:NSDayCalendarUnit inUnit:NSMonthCalendarUnit forDate:currentDate];
                int daysLeft = daysRange.location + daysRange.length - [GlobalData globalData].currentDay;
                int month = [GlobalData globalData].currentMonth;
                [cell setupWithMonth:month daysLeft:daysLeft indexPath:indexPath tableView:tableView];
            }
            else
            {
                [cell setupWithLoadingAndIndexPath:indexPath tableView:tableView];
            }
            return cell;
        }
        PuzzleSetCell * cell = [tableView dequeueReusableCellWithIdentifier:@"puzzleSetCell" forIndexPath:indexPath];
        while (currentPuzzleSetStates.count < indexPath.row)
        {
            [currentPuzzleSetStates addObject:[PuzzleSetState new]];
        }
        PuzzleSetData * puzzleSet = [[GlobalData globalData].monthSets objectAtIndex:indexPath.row - 1];
        PuzzleSetState * state = [currentPuzzleSetStates objectAtIndex:indexPath.row - 1];
        if (cell.puzzleSetView != nil && [puzzleSet.set_id compare:cell.puzzleSetView.puzzleSetData.set_id] == NSOrderedSame)
        {
            NSLog(@"found set: %@", puzzleSet.set_id);
            if (puzzleSet.set_id == nil)
            {
                NSLog(@"puzzle set id is nil");
            }
            if (state.height != cell.actualHeight)
            {
                state.height = cell.actualHeight;
                [tableView reloadData];
            }
            return cell;
        }
        
        [cell setupWithData:puzzleSet month:0 showSolved:YES showUnsolved:YES indexPath:indexPath inTableView:tableView];
        
        cell.puzzleSetView.btnShowMore.tag = indexPath.row - 1;
        [cell.puzzleSetView.btnShowMore addTarget:self action:@selector(handleShowMoreClick:) forControlEvents:UIControlEventTouchUpInside];
        [self activateBadges:cell.puzzleSetView];
        if (!puzzleSet.bought.boolValue)
        {
            [cell.puzzleSetView.btnBuy addTarget:self action:@selector(handleBuyClick:) forControlEvents:UIControlEventTouchUpInside];
            if (puzzleSet.type.intValue == PUZZLESET_FREE)
            {
                [cell.puzzleSetView.btnBuy setTitle:@"Скачать" forState:UIControlStateNormal];
            }
            else
            {
                [cell.puzzleSetView.btnBuy setTitle:@"" forState:UIControlStateNormal];
                [[DataManager sharedManager] fetchPricesForProductIDs:@[[NSString stringWithFormat:@"%@%@", PRODUCTID_PREFIX, puzzleSet.set_id]] completion:^(NSDictionary *data, NSError *error) {
                    if (data != nil && cell != nil && puzzleSet != nil)
                    {
                        NSString * price = [data objectForKey:[NSString stringWithFormat:@"%@%@", PRODUCTID_PREFIX, puzzleSet.set_id]];
                        if (price != nil)
                        {
                            [cell.puzzleSetView.btnBuy setTitle:price forState:UIControlStateNormal];
                        }
                    }
                }];
            }
        }
        if (state.height != cell.actualHeight)
        {
            state.height = cell.actualHeight;
            [tableView reloadData];
        }
        
        return cell;
    }
    return nil;
}

@end
