//
//  AppDelegate.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "AppDelegate.h"
#import "GameLogic.h"
#import "TileImageHelper.h"
#import "PrizeWordNavigationController.h"
#import "LoginMainViewController.h"
#import "RootViewController.h"
#import "GlobalData.h"
#import "Facebook.h"
#import "PrizewordStoreObserver.h"
#import "ExternalImage.h"
#import "GameViewController.h"
#import "FISoundEngine.h"
#import <AVFoundation/AVFoundation.h>
#import "APIRequest.h"
#import "NSData+Utils.h"

@implementation AppDelegate

@synthesize window = _window;
@synthesize navController = _navController;
@synthesize rootViewController = _rootViewController;
@synthesize managedObjectContext = __managedObjectContext;
@synthesize managedObjectModel = __managedObjectModel;
@synthesize persistentStoreCoordinator = __persistentStoreCoordinator;
@synthesize isIPad = _isIPad;
@synthesize viewOrientation;
@synthesize deviceOrientation;

static AppDelegate * currentInstance = nil;
static GameLogic * sharedGameLogic = nil;
static PrizewordStoreObserver * storeObserver = nil;

+ (AppDelegate *)currentDelegate
{
    return currentInstance;
}

+ (GameLogic *)gameLogic
{
    return sharedGameLogic;
}

+ (id<SKPaymentTransactionObserver>)storeObserver
{
    return storeObserver;
}

#pragma mark UIApplicationDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    _isIPad = ([UIDevice currentDevice].userInterfaceIdiom == UIUserInterfaceIdiomPad);
    
    [application setStatusBarHidden:YES];
    _window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    _window.autoresizesSubviews = YES;
    _navController = [[[NSBundle mainBundle] loadNibNamed:@"PrizeWordNavigationController" owner:self options:nil] objectAtIndex:0];
    _navController.viewControllers = [NSArray arrayWithObject:[LoginMainViewController new]];
    _navController.view.autoresizesSubviews = YES;
    _rootViewController = [[RootViewController alloc] initWithNavigationController:_navController];
    _rootViewController.view.clipsToBounds = YES;
    _rootViewController.view.autoresizesSubviews = YES;
    
    _rootViewController.view.transform = CGAffineTransformIdentity;

    self.window.rootViewController = _rootViewController;
    self.window.backgroundColor = [UIColor blackColor];
    [self.window makeKeyAndVisible];

    currentInstance = self;
    sharedGameLogic = [GameLogic sharedLogic];
    [TileImageHelper initHelper];
    
    storeObserver = [PrizewordStoreObserver new];
    [[SKPaymentQueue defaultQueue] addTransactionObserver:storeObserver];
    
    BOOL remoteNotificationDisabled = [[NSUserDefaults standardUserDefaults] boolForKey:@"remote-notifications-disabled"];
    if (!remoteNotificationDisabled)
    {
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:UIRemoteNotificationTypeAlert|UIRemoteNotificationTypeBadge|UIRemoteNotificationTypeSound];
    }
    
    //----- SETUP DEVICE ORIENTATION CHANGE NOTIFICATION -----
	UIDevice *device = [UIDevice currentDevice];					//Get the device object
	[device beginGeneratingDeviceOrientationNotifications];			//Tell it to start monitoring the accelerometer for orientation
    deviceOrientation = device.orientation;
    viewOrientation = -1;
	NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];	//Get the notification centre for the app
	[nc addObserver:self selector:@selector(orientationChanged:) name:UIDeviceOrientationDidChangeNotification object:device];
    
    // initialize sound engine
    [FISoundEngine sharedEngine];
    if ([[UIDevice currentDevice].systemVersion compare:@"6.0" options:NSNumericSearch] != NSOrderedAscending)
    {
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(handleAudioSessionInterruption:)
                                                     name:AVAudioSessionInterruptionNotification
                                                   object:[AVAudioSession sharedInstance]];
    }
    else
    {
        [(AVAudioSession *)[AVAudioSession sharedInstance] setDelegate:self];
    }
    
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    [FBSession.activeSession handleDidBecomeActive];
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Saves changes in the application's managed object context before the application terminates.
    [self saveContext];
    [TileImageHelper uninitHelper];
    [[GlobalData globalData].fbSession close];
}

- (void)applicationDidReceiveMemoryWarning:(UIApplication *)application
{
    [ExternalImage clearCache];
}

-(void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    [GlobalData globalData].deviceToken = [deviceToken hexadecimalString];
}

-(void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
    NSLog(@"didFailToRegisterForRemoteNotificationsWithError: %@", error.description);
}

#pragma mark orientation handling

-(void)orientationChanged:(NSNotification *)note
{
    if (note != nil)
    {
        deviceOrientation = [[note object] orientation];
    }
    
    UIDeviceOrientation targetOrientation = deviceOrientation;
    
    if (![self isIPad] || ![[_navController topViewController] isKindOfClass:[GameViewController class]])
    {
        if (UIDeviceOrientationIsLandscape(targetOrientation))
        {
            if (UIDeviceOrientationIsLandscape(viewOrientation))
            {
                targetOrientation = UIDeviceOrientationPortrait;
            }
            else
            {
                targetOrientation = viewOrientation;
            }
        }
    }
    
    [self setOrientation:targetOrientation];
}

- (void)setOrientation:(UIDeviceOrientation)targetOrientation
{
    if (viewOrientation != targetOrientation)
    {
        NSLog(@"change orientation from %d to %d", viewOrientation, targetOrientation);
        viewOrientation = targetOrientation;
        NSLog(@"window frame: %f %f %f %f", _window.frame.origin.x, _window.frame.origin.y, _window.frame.size.width, _window.frame.size.height);
        CGSize frameSize = _isIPad ? CGSizeMake(768, 1024) : CGSizeMake(320, 480);
        switch (targetOrientation)
        {
            case UIDeviceOrientationPortrait:
            {
                [UIApplication sharedApplication].statusBarOrientation = UIInterfaceOrientationPortrait;
                                    [UIView animateWithDuration:0.5f animations:^{
                _window.transform = CGAffineTransformIdentity;
                _window.frame = CGRectMake(0, 0, frameSize.width, frameSize.height);
                _rootViewController.view.frame = _window.frame;
                                    }];
            }
                break;
                
            case UIDeviceOrientationPortraitUpsideDown:
            {
                [UIApplication sharedApplication].statusBarOrientation = UIInterfaceOrientationPortraitUpsideDown;
                                    [UIView animateWithDuration:0.5f animations:^{
                _window.transform = CGAffineTransformMakeRotation(M_PI);
                                        _window.frame = CGRectMake(0, 0, frameSize.width, frameSize.height);
                _rootViewController.view.frame = _window.frame;
                                    }];
            }
                break;
                
            case UIDeviceOrientationLandscapeLeft:
            {
                [UIApplication sharedApplication].statusBarOrientation = UIInterfaceOrientationLandscapeRight;
                                    [UIView animateWithDuration:0.5f animations:^{
                _window.transform = CGAffineTransformMakeRotation(M_PI_2);
                _window.frame = CGRectMake(0, 0, 1024, 1024);
                _rootViewController.view.frame = CGRectMake(0, 256, 1024, 768);
                                    }];
            }
                break;
                
            case UIDeviceOrientationLandscapeRight:
            {
                [UIApplication sharedApplication].statusBarOrientation = UIInterfaceOrientationLandscapeLeft;
                [UIView animateWithDuration:0.5f animations:^{
                    _window.transform = CGAffineTransformMakeRotation(-M_PI_2);
                    _window.frame = CGRectMake(0, 0, 1024, 1024);
                    _rootViewController.view.frame = CGRectMake(0, 0, 1024, 768);
                                    }];
            }
                break;
                
                
            default:
                break;
        }
        
        [_rootViewController orientationChanged:viewOrientation];
        if ([_navController.topViewController isKindOfClass:[PrizeWordViewController class]])
        {
            [(PrizeWordViewController *)_navController.topViewController orientationChanged:viewOrientation];
        }
    }
}


- (void)saveContext
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    if (managedObjectContext != nil) {
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error]) {
             // Replace this implementation with code to handle the error appropriately.
             // abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. 
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
            abort();
        } 
    }
}

// FBSample logic
// The native facebook application transitions back to an authenticating application when the user
// chooses to either log in, or cancel. The url passed to this method contains the token in the
// case of a successful login. By passing the url to the handleOpenURL method of a session object
// the session object can parse the URL, and capture the token for use by the rest of the authenticating
// application; the return value of handleOpenURL indicates whether or not the URL was handled by the
// session object, and does not reflect whether or not the login was successful; the session object's
// state, as well as its arguments passed to the state completion handler indicate whether the login
// was successful; note that if the session is nil or closed when handleOpenURL is called, the expression
// will be boolean NO, meaning the URL was not handled by the authenticating application
- (BOOL)application:(UIApplication *)application
            openURL:(NSURL *)url
  sourceApplication:(NSString *)sourceApplication
         annotation:(id)annotation {
    // attempt to extract a token from the url
    return [FBSession.activeSession handleOpenURL:url];
}

#pragma mark - Core Data stack

// Returns the managed object context for the application.
// If the context doesn't already exist, it is created and bound to the persistent store coordinator for the application.
- (NSManagedObjectContext *)managedObjectContext
{
    if (__managedObjectContext != nil) {
        return __managedObjectContext;
    }
    
    NSPersistentStoreCoordinator *coordinator = [self persistentStoreCoordinator];
    if (coordinator != nil) {
        __managedObjectContext = [[NSManagedObjectContext alloc] init];
        [__managedObjectContext setPersistentStoreCoordinator:coordinator];
    }
    return __managedObjectContext;
}

// Returns the managed object model for the application.
// If the model doesn't already exist, it is created from the application's model.
- (NSManagedObjectModel *)managedObjectModel
{
    if (__managedObjectModel != nil) {
        return __managedObjectModel;
    }
    NSURL *modelURL = [[NSBundle mainBundle] URLForResource:@"PrizeWord" withExtension:@"momd"];
    __managedObjectModel = [[NSManagedObjectModel alloc] initWithContentsOfURL:modelURL];
    return __managedObjectModel;
}

// Returns the persistent store coordinator for the application.
// If the coordinator doesn't already exist, it is created and the application's store added to it.
- (NSPersistentStoreCoordinator *)persistentStoreCoordinator
{
    if (__persistentStoreCoordinator != nil) {
        return __persistentStoreCoordinator;
    }
    
    NSURL *storeURL = [[self applicationDocumentsDirectory] URLByAppendingPathComponent:@"PrizeWord.sqlite"];
    
    NSError *error = nil;
    __persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:[self managedObjectModel]];
    if (![__persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:storeURL options:nil error:&error]) {
        /*
         Replace this implementation with code to handle the error appropriately.
         
         abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. 
         
         Typical reasons for an error here include:
         * The persistent store is not accessible;
         * The schema for the persistent store is incompatible with current managed object model.
         Check the error message to determine what the actual problem was.
         
         
         If the persistent store is not accessible, there is typically something wrong with the file path. Often, a file URL is pointing into the application's resources directory instead of a writeable directory.
         
         If you encounter schema incompatibility errors during development, you can reduce their frequency by:
         * Simply deleting the existing store:
         [[NSFileManager defaultManager] removeItemAtURL:storeURL error:nil]
         
         * Performing automatic lightweight migration by passing the following dictionary as the options parameter: 
         [NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithBool:YES], NSMigratePersistentStoresAutomaticallyOption, [NSNumber numberWithBool:YES], NSInferMappingModelAutomaticallyOption, nil];
         
         Lightweight migration will only work for a limited set of schema changes; consult "Core Data Model Versioning and Data Migration Programming Guide" for details.
         
         */
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
        abort();
    }    
    
    return __persistentStoreCoordinator;
}

#pragma mark - Application's Documents directory

// Returns the URL to the application's Documents directory.
- (NSURL *)applicationDocumentsDirectory
{
    return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
}

#pragma mark Sound Engine
- (void)handleAudioSessionInterruption: (NSNotification*) event
{
    NSUInteger type = [[[event userInfo] objectForKey:AVAudioSessionInterruptionTypeKey] unsignedIntegerValue];
    switch (type) {
        case AVAudioSessionInterruptionTypeBegan:
            NSLog(@"Audio interruption began, suspending sound engine.");
            [[FISoundEngine sharedEngine] setSuspended:YES];
            break;
        case AVAudioSessionInterruptionTypeEnded:
            if ([[UIApplication sharedApplication] applicationState] == UIApplicationStateActive) {
                NSLog(@"Audio interruption ended, resuming sound engine.");
                [[FISoundEngine sharedEngine] setSuspended:NO];
            } else {
                // Have to wait for the app to become active, otherwise
                // the audio session won’t resume correctly.
            }
            break;
    }
}

-(void)beginInterruption
{
    NSLog(@"Audio interruption began, suspending sound engine.");
    [[FISoundEngine sharedEngine] setSuspended:YES];
}

-(void)endInterruption
{
    NSLog(@"Audio interruption ended, resuming sound engine.");
    [[FISoundEngine sharedEngine] setSuspended:NO];
}


@end
