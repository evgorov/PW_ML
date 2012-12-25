//
//  ScoreViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import "ScoreViewController.h"
#import "BadgeView.h"
#import "PrizeWordNavigationBar.h"
#import "AppDelegate.h"
#import "RootViewController.h"
#import "InviteViewController.h"
#import "PuzzleSetView.h"

@interface ScoreViewController (private)

-(void)handleMenuClick:(id)sender;

@end

@implementation ScoreViewController

-(void)viewDidLoad
{
    [super viewDidLoad];
    self.title = @"3456 в декабре";
    
    PuzzleSetView * brilliantSet = [PuzzleSetView puzzleSetCompleteViewWithType:PUZZLESET_BRILLIANT puzzlesCount:12 puzzlesSolved:7 score:27000 ids:[NSArray arrayWithObjects:[NSNumber numberWithInt:1], [NSNumber numberWithInt:2], [NSNumber numberWithInt:4], [NSNumber numberWithInt:5], [NSNumber numberWithInt:6], [NSNumber numberWithInt:12], nil] scores:[NSArray arrayWithObjects:[NSNumber numberWithInt:100], [NSNumber numberWithInt:1000], [NSNumber numberWithInt:10000], [NSNumber numberWithInt:3425], [NSNumber numberWithInt:777], [NSNumber numberWithInt:104], nil]];
    brilliantSet.frame = CGRectMake(0, -2, brilliantSet.frame.size.width, brilliantSet.frame.size.height);
    [puzzlesView addSubview:brilliantSet];

    PuzzleSetView * goldSet = [PuzzleSetView puzzleSetCompleteViewWithType:PUZZLESET_GOLD puzzlesCount:12 puzzlesSolved:7 score:27000 ids:[NSArray arrayWithObjects:[NSNumber numberWithInt:1], [NSNumber numberWithInt:2], [NSNumber numberWithInt:4], [NSNumber numberWithInt:5], [NSNumber numberWithInt:6], [NSNumber numberWithInt:12], nil] scores:[NSArray arrayWithObjects:[NSNumber numberWithInt:100], [NSNumber numberWithInt:1000], [NSNumber numberWithInt:10000], [NSNumber numberWithInt:3425], [NSNumber numberWithInt:777], [NSNumber numberWithInt:104], nil]];
    goldSet.frame = CGRectMake(0, brilliantSet.frame.origin.y + brilliantSet.frame.size.height, goldSet.frame.size.width, goldSet.frame.size.height);
    [puzzlesView addSubview:goldSet];
    
    PuzzleSetView * silver2Set = [PuzzleSetView puzzleSetCompleteViewWithType:PUZZLESET_SILVER2 puzzlesCount:12 puzzlesSolved:7 score:27000 ids:[NSArray arrayWithObjects:[NSNumber numberWithInt:1], [NSNumber numberWithInt:2], [NSNumber numberWithInt:4], [NSNumber numberWithInt:5], [NSNumber numberWithInt:6], [NSNumber numberWithInt:12], nil] scores:[NSArray arrayWithObjects:[NSNumber numberWithInt:100], [NSNumber numberWithInt:1000], [NSNumber numberWithInt:10000], [NSNumber numberWithInt:3425], [NSNumber numberWithInt:777], [NSNumber numberWithInt:104], nil]];
    silver2Set.frame = CGRectMake(0, goldSet.frame.origin.y + goldSet.frame.size.height, silver2Set.frame.size.width, silver2Set.frame.size.height);
    [puzzlesView addSubview:silver2Set];
    puzzlesView.frame = CGRectMake(puzzlesView.frame.origin.x, puzzlesView.frame.origin.y, puzzlesView.frame.size.width, silver2Set.frame.origin.y + silver2Set.frame.size.height);
    
    [self addFramedView:puzzlesView];
    [self addFramedView:invitesView];

}

- (void)viewDidUnload
{
    puzzlesView = nil;
    invitesView = nil;
    btnInvite = nil;
    [super viewDidUnload];
}

- (IBAction)handleInviteClick:(id)sender
{
    UINavigationController * navController = self.navigationController;
    [navController popViewControllerAnimated:NO];
    [navController pushViewController:[InviteViewController new] animated:YES];
}

@end
