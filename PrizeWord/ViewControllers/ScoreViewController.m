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
#import "PuzzleSetData.h"
#import "GlobalData.h"
#import "UserData.h"

NSString * MONTHS_IN[] = {@"январе", @"феврале", @"марте", @"апреле", @"мае", @"июне", @"июле", @"августе", @"сентябре", @"октябре", @"ноябрь", @"декабрь"};

@interface ScoreViewController (private)

-(void)handleMenuClick:(id)sender;

@end

@implementation ScoreViewController

-(void)viewDidLoad
{
    [super viewDidLoad];
    self.title = [NSString stringWithFormat:@"%d в %@", [GlobalData globalData].loggedInUser.month_score, MONTHS_IN[[GlobalData globalData].currentMonth]];

    int yOffset = 0;
    for (PuzzleSetData * puzzleSet in [GlobalData globalData].monthSets) {
        NSMutableArray * solvedIds = [NSMutableArray new];
        NSMutableArray * solvedScores = [NSMutableArray new];
        int idx = 1;
        for (PuzzleData * puzzle in puzzleSet.puzzles) {
            if (puzzle.solved == puzzle.questions.count)
            {
                [solvedIds addObject:[NSNumber numberWithInt:idx]];
                [solvedScores addObject:puzzle.score];
            }
            ++idx;
        }
        PuzzleSetView * puzzleSetView = [PuzzleSetView puzzleSetCompleteViewWithData:puzzleSet];
        puzzleSetView.frame = CGRectMake(0, yOffset, puzzleSetView.frame.size.width, puzzleSetView.frame.size.height);
        yOffset += puzzleSetView.frame.size.height;
        [puzzlesView addSubview:puzzleSetView];
    }
    puzzlesView.frame = CGRectMake(0, 0, puzzlesView.frame.size.width, yOffset);
    
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
