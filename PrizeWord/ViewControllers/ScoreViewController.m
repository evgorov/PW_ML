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
#import "PuzzleSetProxy.h"
#import "GlobalData.h"
#import "UserData.h"
#import "SBJsonParser.h"
#import "ScoreInviteCellView.h"
#import "ScoreShareCellView.h"
#import "NSString+Utils.h"

NSString * MONTHS_IN[] = {@"январе", @"феврале", @"марте", @"апреле", @"мае", @"июне", @"июле", @"августе", @"сентябре", @"октябре", @"ноябрь", @"декабрь"};

int const TAG_SHARE_CELL = 4235;

@interface ScoreViewController (private)

-(void)updateInvited:(NSString *)providerName;
-(void)updateRate;
-(void)updateShare;

@end

@implementation ScoreViewController

-(void)viewDidLoad
{
    [super viewDidLoad];
    self.title = [NSString stringWithFormat:@"%d в %@", [GlobalData globalData].loggedInUser.month_score, MONTHS_IN[[GlobalData globalData].currentMonth - 1]];

    int yOffset = 0;
    int idx = 0;
    for (PuzzleSetProxy * puzzleSet in [GlobalData globalData].monthSets) {
        if (puzzleSet.type.intValue == PUZZLESET_FREE && puzzleSet.score == 0 && puzzleSet.minScore == 0)
        {
            continue;
        }
        PuzzleSetView * puzzleSetView = [PuzzleSetView puzzleSetCompleteViewWithData:puzzleSet];
        if (idx == 0)
        {
            puzzleSetView.imgDelimeter.hidden = YES;
        }
        puzzleSetView.frame = CGRectMake(floorf((puzzlesView.frame.size.width - puzzleSetView.frame.size.width) / 2), yOffset, puzzleSetView.frame.size.width, puzzleSetView.frame.size.height);
        yOffset += puzzleSetView.frame.size.height;
        [puzzlesView addSubview:puzzleSetView];
        ++idx;
    }
    puzzlesView.frame = CGRectMake(0, 0, puzzlesView.frame.size.width, yOffset);
    
    updateInProgress = [NSMutableDictionary new];
    invitedFriends = 0;

    [self addFramedView:puzzlesView];
    [self addFramedView:invitesView];
    [self addFramedView:rateView];
    [self addFramedView:shareView];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    if ([GlobalData globalData].loggedInUser.vkProvider != nil)
    {
        [self updateInvited:@"vkontakte"];
    }
    if  ([GlobalData globalData].loggedInUser.fbProvider != nil)
    {
        [self updateInvited:@"facebook"];
    }
    [[GlobalData globalData] loadMe];
    [self updateRate];
    [self updateShare];
    /*
    if ([GlobalData globalData].loggedInUser.ratedThisMonth.boolValue)
    {
        [lblRateScore setText:[NSString stringWithFormat:@"%d", [GlobalData globalData].scoreForRate]];
    }
    */
}

-(void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    // TODO :: remove all friend views
}

- (IBAction)handleInviteClick:(id)sender
{
    UINavigationController * navController = self.navigationController;
    [navController popViewControllerAnimated:NO];
    [navController pushViewController:[InviteViewController new] animated:YES];
}

-(void)updateInvited:(NSString *)providerName
{
    if ([updateInProgress objectForKey:providerName] != nil) {
        return;
    }
    [self showActivityIndicator];
    [updateInProgress setObject:[NSNumber numberWithBool:YES] forKey:providerName];
    
    NSDictionary * params = @{@"session_key": [GlobalData globalData].sessionKey
                              , @"provider_name": providerName};
    [[APIClient sharedClient] getPath:[NSString stringWithFormat:@"%@/invited_friends_this_month", providerName] parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
        [self hideActivityIndicator];
        [updateInProgress removeObjectForKey:providerName];
        NSLog(@"updateInvited %@ complete: %@", providerName, [[NSString alloc] initWithData:operation.responseData encoding:NSUTF8StringEncoding]);
        if (operation.response.statusCode == 200)
        {
            float yOffset = invitesView.frame.size.height - ([AppDelegate currentDelegate].isIPad ? 110 : 75);
            SBJsonParser * parser = [SBJsonParser new];
            NSArray * friendsData = [parser objectWithData:operation.responseData];
            for (NSDictionary * friendData in friendsData)
            {
                ScoreInviteCellView * userView = [[[NSBundle mainBundle] loadNibNamed:@"ScoreInviteCellView" owner:self options:nil] objectAtIndex:0];
                userView.lblName.text = [NSString stringWithFormat:@"%@ %@", [friendData objectForKey:@"first_name"], [friendData objectForKey:@"last_name"]];
                userView.lblScore.text = [NSString stringWithFormat:@"%d", [GlobalData globalData].scoreForFriend];
                [userView.imgAvatar loadImageFromURL:[NSURL URLWithString:[friendData objectForKey:@"userpic"]]];
                userView.frame = CGRectMake(0, yOffset, userView.frame.size.width, userView.frame.size.height);
                [invitesView insertSubview:userView atIndex:0];
                yOffset += userView.frame.size.height;
                invitedFriends++;
            }
            
            [self resizeView:invitesView newHeight:(yOffset + ([AppDelegate currentDelegate].isIPad ? 110 : 75)) animated:YES];
            [UIView animateWithDuration:0.3 animations:^{
                btnInvite.frame = CGRectMake(btnInvite.frame.origin.x, yOffset, btnInvite.frame.size.width, btnInvite.frame.size.height);
            }];
            lblInvitesFriendsCount.text = [NSString stringWithFormat:@"%d ", invitedFriends];
            lblInvitesFriendsCount.frame = CGRectMake(lblInvitesFriendsCount.frame.origin.x, lblInvitesFriendsCount.frame.origin.y, [lblInvitesFriendsCount.text sizeWithFont:lblInvitesFriendsCount.font].width, lblInvitesFriendsCount.frame.size.height);
            lblInvitesFriendsLabel.frame = CGRectMake(lblInvitesFriendsCount.frame.origin.x + lblInvitesFriendsCount.frame.size.width, lblInvitesFriendsLabel.frame.origin.y, lblInvitesFriendsLabel.frame.size.width, lblInvitesFriendsLabel.frame.size.height);
            lblInvitesScore.text = [NSString stringWithFormat:@"%d", invitedFriends * [GlobalData globalData].scoreForFriend];
        }
        else
        {
            SBJsonParser * parser = [SBJsonParser new];
            NSDictionary * data = [parser objectWithData:operation.responseData];
            NSString * message = data == nil ? operation.responseString : [data objectForKey:@"message"];
            NSLog(@"score for friends error: %@", message);
        }
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        [self hideActivityIndicator];
        [updateInProgress removeObjectForKey:providerName];
        NSLog(@"error: %@", error.description);
    }];
}

- (void)updateShare
{
    float newHeight = shareView.frame.size.height;
    while ([shareView viewWithTag:TAG_SHARE_CELL] != nil) {
        UIView * subview = [shareView viewWithTag:TAG_SHARE_CELL];
        newHeight -= subview.frame.size.height;
        [subview removeFromSuperview];
    }
    if (newHeight != shareView.frame.size.height)
    {
        CGRect frame = shareView.frame;
        frame.size = CGSizeMake(frame.size.width, newHeight);
        shareView.frame = frame;
    }
    
    UserData *user = [GlobalData globalData].loggedInUser;
    BOOL hasShared = (user.count_fb_shared + user.count_vk_shared != 0);
    if (hasShared)
    {
        __block float yOffset = shareView.frame.size.height;
        __block int sumScore = 0;
        void (^createShareViewCell)(PuzzleSetType type) = ^(PuzzleSetType type) {
            NSString * setTypeName = @"";
            int score = 0;
            UIImage * setTypeImage = nil;
            switch (type) {
                case PUZZLESET_BRILLIANT:
                    setTypeName = @"Бриллиантовый";
                    score = user.shared_brilliant_score;
                    setTypeImage = [UIImage imageNamed:@"puzzles_set_br"];
                    break;
                    
                case PUZZLESET_FREE:
                    setTypeName = @"Бесплатный";
                    score = user.shared_free_score;
                    setTypeImage = [UIImage imageNamed:@"puzzles_set_fr"];
                    break;
                    
                case PUZZLESET_GOLD:
                    setTypeName = @"Золотой";
                    score = user.shared_gold_score;
                    setTypeImage = [UIImage imageNamed:@"puzzles_set_au"];
                    break;
                    
                case PUZZLESET_SILVER:
                    setTypeName = @"Серебряный";
                    score = user.shared_silver1_score;
                    setTypeImage = [UIImage imageNamed:@"puzzles_set_ag"];
                    break;
                    
                case PUZZLESET_SILVER2:
                    setTypeName = @"2-й Серебряный";
                    score = user.shared_silver2_score;
                    setTypeImage = [UIImage imageNamed:@"puzzles_set_ag2"];
                    break;
                    
                default:
                    break;
            }
            if (score != 0)
            {
                ScoreShareCellView * shareCellView = [[[NSBundle mainBundle] loadNibNamed:@"ScoreShareCellView" owner:self options:nil] objectAtIndex:0];
                shareCellView.tag = TAG_SHARE_CELL;
                shareCellView.lblNetworkName.text = setTypeName;
                shareCellView.lblScore.text = [NSString stringWithFormat:@"%d", score];
                shareCellView.imgSetType.image = setTypeImage;
                shareCellView.frame = CGRectMake(0, yOffset, shareCellView.frame.size.width, shareCellView.frame.size.height);
                [shareView insertSubview:shareCellView atIndex:0];
                yOffset += shareCellView.frame.size.height;
                sumScore += score;
            }
        };
        
        for (int type = PUZZLESET_BRILLIANT; type <= PUZZLESET_FREE; ++type) {
            createShareViewCell(type);
        }
        
        [self resizeView:shareView newHeight:yOffset animated:YES];

        lblShareCount.text = [NSString stringWithFormat:@"%d ", user.count_fb_shared + user.count_vk_shared];
        lblShareCount.frame = CGRectMake(lblShareCount.frame.origin.x, lblShareCount.frame.origin.y, [lblShareCount.text sizeWithFont:lblShareCount.font].width, lblShareCount.frame.size.height);
        lblShareLabel.text = [NSString declesion:user.count_fb_shared + user.count_vk_shared oneString:@"победа отправлена" twoString:@"победы отправлено" fiveString:@"побед отправлено"];
        lblShareLabel.frame = CGRectMake(lblShareCount.frame.origin.x + lblShareCount.frame.size.width, lblShareLabel.frame.origin.y, lblShareLabel.frame.size.width, lblShareLabel.frame.size.height);
        lblShareScore.text = [NSString stringWithFormat:@"%d", sumScore];
        if (shareView.superview == nil)
        {
            [self addFramedView:shareView];
        }
    }
    else
    {
        if (shareView.superview != nil)
        {
            [self removeFramedView:shareView];
        }
    }
}

- (void)updateRate
{
    BOOL hasRated = [GlobalData globalData].loggedInUser.is_app_rated;
    if (hasRated)
    {
        [lblRateScore setText:[NSString stringWithFormat:@"%d", [GlobalData globalData].scoreForRate]];
        if (rateView.superview == nil)
        {
            [self addFramedView:rateView];
        }
    }
    else
    {
        if (rateView.superview != nil)
        {
            [self removeFramedView:rateView];
        }
    }
}

@end
