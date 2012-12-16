//
//  RootViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "RootViewController.h"
#import "GameViewController.h"
#import "PuzzlesViewController.h"
#import "ScoreViewController.h"
#import "InviteViewController.h"

@interface RootViewController ()

@end

@implementation RootViewController

@synthesize isMenuHidden = _isMenuHidden;

-(id)initWithNavigationController:(UINavigationController *)navigationController
{
    self = [super init];
    if (self)
    {
        navController = navigationController;
        _isMenuHidden = YES;
    }
    return self;
}

-(void)viewDidLoad
{
    mainMenuView.contentSize = mainMenuBg.frame.size;
    
    UIFont * font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:13];
    NSString * score = @"9 001 ";
    CGSize scoreSize = [score sizeWithFont:font];
    UILabel * lblScoreNumber = [[UILabel alloc] initWithFrame:CGRectMake(40, 15, scoreSize.width, scoreSize.height)];
    lblScoreNumber.font = font;
    lblScoreNumber.textAlignment = UITextAlignmentLeft;
    lblScoreNumber.textColor = [UIColor colorWithRed:228/255.f green:179/255.f blue:55/255.f alpha:1];
    lblScoreNumber.highlightedTextColor = [UIColor colorWithRed:228/400.f green:179/400.f blue:55/400.f alpha:1];
    lblScoreNumber.shadowColor = [UIColor colorWithRed:36/255.f green:31/255.f blue:26/255.f alpha:1];
    lblScoreNumber.backgroundColor = [UIColor clearColor];
    lblScoreNumber.shadowOffset = CGSizeMake(0, -1);
    lblScoreNumber.text = score;
    [btnScore addSubview:lblScoreNumber];
    
    UILabel * lblScoreSuffix = [[UILabel alloc] initWithFrame:CGRectMake(40 + scoreSize.width, 15, 100, scoreSize.height)];
    lblScoreSuffix.font = font;
    lblScoreSuffix.textAlignment = UITextAlignmentLeft;
    lblScoreSuffix.textColor = [UIColor colorWithRed:158/255.f green:146/255.f blue:135/255.f alpha:1];
    lblScoreSuffix.highlightedTextColor = [UIColor colorWithRed:158/400.f green:146/400.f blue:135/400.f alpha:1];
    lblScoreSuffix.shadowColor = [UIColor colorWithRed:36/255.f green:31/255.f blue:26/255.f alpha:1];
    lblScoreSuffix.backgroundColor = [UIColor clearColor];
    lblScoreSuffix.shadowOffset = CGSizeMake(0, -1);
    lblScoreSuffix.text = @"очков";
    [btnScore addSubview:lblScoreSuffix];

    NSString * rating = @"123-й ";
    CGSize ratingSize = [rating sizeWithFont:font];
    UILabel * lblRatingNumber = [[UILabel alloc] initWithFrame:CGRectMake(40, 15, ratingSize.width, ratingSize.height)];
    lblRatingNumber.font = font;
    lblRatingNumber.textAlignment = UITextAlignmentLeft;
    lblRatingNumber.textColor = [UIColor colorWithRed:115/255.f green:189/255.f blue:69/255.f alpha:1];
    lblRatingNumber.highlightedTextColor = [UIColor colorWithRed:115/400.f green:189/400.f blue:69/400.f alpha:1];
    lblRatingNumber.shadowColor = [UIColor colorWithRed:36/255.f green:31/255.f blue:26/255.f alpha:1];
    lblRatingNumber.backgroundColor = [UIColor clearColor];
    lblRatingNumber.shadowOffset = CGSizeMake(0, -1);
    lblRatingNumber.text = rating;
    [btnRating addSubview:lblRatingNumber];
    
    UILabel * lblRatingSuffix = [[UILabel alloc] initWithFrame:CGRectMake(40 + ratingSize.width, 15, 100, ratingSize.height)];
    lblRatingSuffix.font = font;
    lblRatingSuffix.textAlignment = UITextAlignmentLeft;
    lblRatingSuffix.textColor = [UIColor colorWithRed:158/255.f green:146/255.f blue:135/255.f alpha:1];
    lblRatingSuffix.highlightedTextColor = [UIColor colorWithRed:158/400.f green:146/400.f blue:135/400.f alpha:1];
    lblRatingSuffix.shadowColor = [UIColor colorWithRed:36/255.f green:31/255.f blue:26/255.f alpha:1];
    lblRatingSuffix.backgroundColor = [UIColor clearColor];
    lblRatingSuffix.shadowOffset = CGSizeMake(0, -1);
    lblRatingSuffix.text = @"в рейтинге";
    [btnRating addSubview:lblRatingSuffix];
    
    [self.view addSubview:navController.view];
    if (_isMenuHidden)
    {
        [self hideMenuAnimated:NO];
    }
    else
    {
        [self showMenuAnimated:NO];
    }
}

- (void)viewDidUnload
{
    btnScore = nil;
    btnRating = nil;
    mainMenuView = nil;
    mainMenuBg = nil;
    [super viewDidUnload];
}

-(void)showMenuAnimated:(BOOL)animated
{
    _isMenuHidden = NO;
    if (animated)
    {
        [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
        [UIView animateWithDuration:0.3 animations:^{
            mainMenuView.frame = CGRectMake(0, 0, mainMenuView.frame.size.width, mainMenuView.frame.size.height);
            navController.view.frame = CGRectMake(mainMenuView.frame.size.width, 0, self.view.frame.size.width, self.view.frame.size.height);
        }];
    }
    else
    {
        mainMenuView.frame = CGRectMake(0, 0, mainMenuView.frame.size.width, mainMenuView.frame.size.height);
        navController.view.frame = CGRectMake(mainMenuView.frame.size.width, 0, self.view.frame.size.width, self.view.frame.size.height);
    }
}

-(void)hideMenuAnimated:(BOOL)animated
{
    _isMenuHidden = YES;
    if (animated)
    {
        [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
        [UIView animateWithDuration:0.3 animations:^{
            mainMenuView.frame = CGRectMake(-mainMenuView.frame.size.width, 0, mainMenuView.frame.size.width, mainMenuView.frame.size.height);
            navController.view.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
        }];
    }
    else
    {
        mainMenuView.frame = CGRectMake(-mainMenuView.frame.size.width, 0, mainMenuView.frame.size.width, mainMenuView.frame.size.height);
        navController.view.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
    }
}

- (IBAction)handleMyPuzzlesClick:(id)sender
{
    if (![navController.topViewController isKindOfClass:[PuzzlesViewController class]])
    {
        [navController popViewControllerAnimated:NO];
        [navController pushViewController:[PuzzlesViewController new] animated:YES];
    }
    [self hideMenuAnimated:YES];
}

- (IBAction)handleSwitchUserClick:(id)sender
{
    [navController popToRootViewControllerAnimated:YES];
    [self hideMenuAnimated:YES];
}

- (IBAction)handleScoreClick:(id)sender
{
    if (![navController.topViewController isKindOfClass:[ScoreViewController class]])
    {
        [navController popViewControllerAnimated:NO];
        [navController pushViewController:[ScoreViewController new] animated:YES];
    }
    [self hideMenuAnimated:YES];
}

- (IBAction)handleRatingClick:(id)sender
{
}

- (IBAction)handleInviteClick:(id)sender
{
    if (![navController.topViewController isKindOfClass:[InviteViewController class]])
    {
        [navController popViewControllerAnimated:NO];
        [navController pushViewController:[InviteViewController new] animated:YES];
    }
    [self hideMenuAnimated:YES];
}

- (IBAction)handleRulesClick:(id)sender
{
}

- (IBAction)handleRestoreClick:(id)sender
{
}

- (IBAction)handleVKSwitchChange:(id)sender
{
}

- (IBAction)handleFBSwitchChange:(id)sender
{
}

- (IBAction)handleNotificationSwitchChange:(id)sender
{
}

@end
