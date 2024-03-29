//
//  AppDelegate.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <StoreKit/SKPaymentQueue.h>

@class GameLogic;
@class PrizeWordNavigationController;
@class RootViewController;

@interface AppDelegate : UIResponder <UIApplicationDelegate, AVAudioSessionDelegate>

@property (strong, nonatomic) UIWindow *window;
@property (readonly, strong, nonatomic) PrizeWordNavigationController *navController;
@property (readonly, strong, nonatomic) RootViewController *rootViewController;

//@property (readonly, strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (readonly, strong, nonatomic) NSManagedObjectModel *managedObjectModel;
@property (readonly, strong, nonatomic) NSPersistentStoreCoordinator *persistentStoreCoordinator;

@property (readonly, nonatomic) AVAudioPlayer * backgroundMusicPlayer;

@property (readonly) BOOL isIPad;
@property (readonly) UIDeviceOrientation viewOrientation;
@property (readonly) UIDeviceOrientation deviceOrientation;

+ (AppDelegate *)currentDelegate;
+ (GameLogic *)gameLogic;
+ (id<SKPaymentTransactionObserver>)storeObserver;
//- (void)saveContext;
- (NSURL *)applicationDocumentsDirectory;
- (void)orientationChanged:(NSNotification *)note;
- (void)setOrientation:(UIDeviceOrientation)targetOrientation;


@end
