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
#import "APIRequest.h"
#import "SBJsonParser.h"
#import "ScoreInviteCellView.h"

NSString * MONTHS_IN[] = {@"январе", @"феврале", @"марте", @"апреле", @"мае", @"июне", @"июле", @"августе", @"сентябре", @"октябре", @"ноябрь", @"декабрь"};

@interface ScoreViewController (private)

-(void)updateInvited:(NSString *)providerName;

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
    
    [self updateInvited:@"facebook"];
    [self updateInvited:@"vkontakte"];
}

- (void)viewDidUnload
{
    puzzlesView = nil;
    invitesView = nil;
    btnInvite = nil;
    lblInvitesScore = nil;
    lblInvitesFriendsCount = nil;
    lblInvitesFriendsLabel = nil;
    [super viewDidUnload];
}

- (IBAction)handleInviteClick:(id)sender
{
    UINavigationController * navController = self.navigationController;
    [navController popViewControllerAnimated:NO];
    [navController pushViewController:[InviteViewController new] animated:YES];
}

-(void)updateInvited:(NSString *)providerName
{
    APIRequest * request = [APIRequest getRequest:[NSString stringWithFormat:@"%@/friends", providerName] successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        [self hideActivityIndicator];
        if (response.statusCode == 200)
        {
            float yOffset = invitesView.frame.size.height - ([AppDelegate currentDelegate].isIPad ? 110 : 75);
            SBJsonParser * parser = [SBJsonParser new];
            NSArray * friendsData = [parser objectWithData:receivedData];
            for (NSDictionary * friendData in friendsData)
            {
                UserData * user = [UserData userDataWithDictionary:friendData];
                ScoreInviteCellView * userView = [[[NSBundle mainBundle] loadNibNamed:@"ScoreInviteCellView" owner:self options:nil] objectAtIndex:0];
                userView.lblName.text = [NSString stringWithFormat:@"%@ %@", user.first_name, user.last_name];
                userView.frame = CGRectMake(0, yOffset, userView.frame.size.width, userView.frame.size.height);
                [invitesView addSubview:userView];
                yOffset += userView.frame.size.height;
            }
            [self resizeView:invitesView newHeight:(yOffset + [AppDelegate currentDelegate].isIPad ? 110 : 75) animated:YES];
            [UIView animateWithDuration:0.3 animations:^{
                btnInvite.frame = CGRectMake(btnInvite.frame.origin.x, yOffset, btnInvite.frame.size.width, btnInvite.frame.size.height);
            }];
        }
        else
        {
            SBJsonParser * parser = [SBJsonParser new];
            NSDictionary * data = [parser objectWithData:receivedData];
            NSString * message = data == nil ? ([[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]) : [data objectForKey:@"message"];
            NSLog(@"error: %@", message);
        }
    } failCallback:^(NSError *error) {
        [self hideActivityIndicator];
        NSLog(@"error: %@", error.description);
    }];
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [request.params setObject:providerName forKey:@"provider_name"];
    [request runSilent];
}

@end
