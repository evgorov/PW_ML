//
//  RatingViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import "RatingViewController.h"
#import "APIRequest.h"
#import "UserData.h"
#import "GlobalData.h"
#import "SBJson.h"
#import "RatingCell.h"

int ROW_HEIGHT = 83;

@interface RatingViewController ()

@end

@implementation RatingViewController

- (void)viewDidLoad
{
    [super viewDidLoad];

    ratingView.frame = CGRectMake(0, 0, ratingView.frame.size.width, 30);
    ratingView.delegate = self;
    ratingView.dataSource = self;
    [self addFramedView:ratingView];
    
    users = [NSMutableArray new];
}

- (void)viewDidUnload {
    ratingView = nil;
    users = nil;
    [super viewDidUnload];
}

-(void)viewWillAppear:(BOOL)animated
{
    self.title = [NSString stringWithFormat:@"%d-ой в рейтинге", [GlobalData globalData].loggedInUser.position];
    
    [super viewWillAppear:animated];

    APIRequest * request = [APIRequest getRequest:@"users" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData)
    {
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * data = [parser objectWithData:receivedData];
        NSArray * usersData = [data objectForKey:@"users"];
        for (NSDictionary * userData in usersData)
        {
            [users addObject:[UserData userDataWithDictionary:userData]];
        }
        
        [self resizeView:ratingView newHeight:(ROW_HEIGHT * users.count) animated:YES];
        
        [ratingView reloadData];
    } failCallback:^(NSError *error)
    {
        NSLog(@"users error: %@", error.description);
    }];
    [request.params setValue:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [request.params setValue:@"0" forKey:@"from"];
    [request.params setValue:@"100" forKey:@"limit"];
    [request runSilent];
}

#pragma mark UITableViewDelegate, UITableViewDataSource

-(NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return users.count;
}

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return ROW_HEIGHT;
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    RatingCell * cell = (RatingCell *)[tableView dequeueReusableCellWithIdentifier:@"rating_cell"];
    if (cell == nil)
    {
        cell = (RatingCell *)[[[NSBundle mainBundle] loadNibNamed:@"RatingCell" owner:self options:nil] objectAtIndex:0];
    }
    else
    {
        NSLog(@"reuse success");
    }
    UserData * user = [users objectAtIndex:indexPath.row];
    cell.lblName.text = [NSString stringWithFormat:@"%@\n%@", user.last_name, user.first_name];
    cell.lblPosition.text = [NSString stringWithFormat:@"%d", user.position];
    cell.lblScore.text = [NSString stringWithFormat:@"%d", user.month_score];
    cell.lblSolved.text = [NSString stringWithFormat:@"%d ", user.solved];
    cell.lblSolvedLabel.frame = CGRectMake(cell.lblSolved.frame.origin.x + [cell.lblSolved.text sizeWithFont:cell.lblSolved.font].width, cell.lblSolvedLabel.frame.origin.y, cell.lblSolvedLabel.frame.size.width, cell.lblSolvedLabel.frame.size.height);
    cell.imgMoveUp.hidden = YES;
    cell.imgMoveDown.hidden = YES;
    cell.imgMoveNone.hidden = YES;
    if (user.dynamics > 0)
    {
        cell.imgMoveUp.hidden = NO;
    }
    else if (user.dynamics < 0)
    {
        cell.imgMoveDown.hidden = NO;
    }
    else
    {
        cell.imgMoveNone.hidden = NO;
    }
    return cell;
}

@end
