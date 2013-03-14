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
#import "AppDelegate.h"

int ROW_HEIGHT = 83;
int FOOTER_HEIGHT = 24;
int HEADER_HEIGHT = 24;

@interface RatingViewController ()

@end

@implementation RatingViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if ([AppDelegate currentDelegate].isIPad)
    {
        ROW_HEIGHT = 97;
        FOOTER_HEIGHT = 35;
        HEADER_HEIGHT = 35;
    }

    ratingView.frame = CGRectMake((self.view.frame.size.width - ratingView.frame.size.width) / 2, 0, ratingView.frame.size.width, 0);
    ratingView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_sand_tile.jpg"]];
    ratingView.clipsToBounds = NO;
    ratingView.delegate = self;
    ratingView.dataSource = self;
    [self.view addSubview:ratingView];
    
    users = [NSMutableArray new];

    self.title = [NSString stringWithFormat:@"%d-й в рейтинге", [GlobalData globalData].loggedInUser.position];
}

- (void)viewDidUnload {
    ratingView = nil;
    users = nil;
    [super viewDidUnload];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [[GlobalData globalData] loadMe];

    APIRequest * request = [APIRequest getRequest:@"users" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData)
    {
        [users removeAllObjects];
//        NSLog(@"rating: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * data = [parser objectWithData:receivedData];
        NSArray * usersData = [data objectForKey:@"users"];
        for (NSDictionary * userData in usersData)
        {
            [users addObject:[UserData userDataWithDictionary:userData]];
        }
        int height = (ROW_HEIGHT * users.count) + HEADER_HEIGHT + FOOTER_HEIGHT;
        if (height > self.view.frame.size.height) {
            height = self.view.frame.size.height;
        }
        [UIView animateWithDuration:0.3 animations:^{
            ratingView.frame = CGRectMake(ratingView.frame.origin.x, ratingView.frame.origin.y, ratingView.frame.size.width, height);
        }];
        
        [ratingView reloadData];
        [ratingView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:[GlobalData globalData].loggedInUser.position inSection:0] atScrollPosition:UITableViewScrollPositionMiddle animated:YES];
        [self hideActivityIndicator];
    } failCallback:^(NSError *error)
    {
        NSLog(@"users error: %@", error.description);
        [self hideActivityIndicator];
    }];
    [request.params setValue:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [request.params setValue:@"0" forKey:@"from"];
    [request.params setValue:@"100" forKey:@"limit"];
    [request runSilent];
    [self showActivityIndicator];
}

-(void)viewDidAppear:(BOOL)animated
{
    NSLog(@"viewDidAppear");
}

-(void)viewWillDisappear:(BOOL)animated
{
    NSLog(@"viewWillDisappear");
}

-(void)viewDidDisappear:(BOOL)animated
{
    NSLog(@"viewDidDisappear");
}

#pragma mark UITableViewDelegate, UITableViewDataSource

-(NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return users.count + 2;
}

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row == 0)
    {
        return HEADER_HEIGHT;
    }
    else if (indexPath.row > users.count)
    {
        return FOOTER_HEIGHT;
    }
    return ROW_HEIGHT;
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row == 0 || indexPath.row > users.count)
    {
        UITableViewCell * cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"hfrc"];
        UIImageView * bg = nil;
        if (indexPath.row == 0)
        {
            bg = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"rating_header.png"]];
        }
        else
        {
            bg = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"rating_footer.png"]];
        }
        NSLog(@"%f %f %f", ratingView.frame.size.width, bg.frame.size.width, ratingView.frame.size.width - bg.frame.size.width);
        bg.frame = CGRectMake((ratingView.frame.size.width - bg.frame.size.width) / 2, 0, bg.frame.size.width, bg.frame.size.height);
        [cell addSubview:bg];
        cell.clipsToBounds = NO;
        return cell;
    }
    
    RatingCell * cell = (RatingCell *)[tableView dequeueReusableCellWithIdentifier:@"rating_cell"];
    if (cell == nil)
    {
        cell = (RatingCell *)[[[NSBundle mainBundle] loadNibNamed:@"RatingCell" owner:self options:nil] objectAtIndex:0];
    }
    
    if (indexPath.row == 1)
    {
        cell.imgBorder.image = [UIImage imageNamed:@"rating_cell_border_first.png"];
    }
    else if (indexPath.row == users.count)
    {
        cell.imgBorder.image = [UIImage imageNamed:@"rating_cell_border_last.png"];
    }
    else
    {
        cell.imgBorder.image = [UIImage imageNamed:@"rating_cell_border.png"];
    }
    
    UserData * user = [users objectAtIndex:indexPath.row - 1];
    cell.lblName.text = user.first_name;
    cell.lblSurname.text = user.last_name;
    cell.lblPosition.text = [NSString stringWithFormat:@"%d", user.position];
    cell.lblScore.text = [NSString stringWithFormat:@"%d", user.month_score];
    cell.lblSolved.text = [NSString stringWithFormat:@"%d ", user.solved];
    cell.lblSolvedLabel.frame = CGRectMake(cell.lblSolved.frame.origin.x + [cell.lblSolved.text sizeWithFont:cell.lblSolved.font].width, cell.lblSolvedLabel.frame.origin.y, cell.lblSolvedLabel.frame.size.width, cell.lblSolvedLabel.frame.size.height);
    cell.imgMoveUp.hidden = YES;
    cell.imgMoveDown.hidden = YES;
    cell.imgMoveNone.hidden = YES;
    cell.lblPosition.hidden = YES;
    [cell.imgPhoto clear];
    cell.imgPhoto.image = user.userpic;
    if (user.userpic_url != nil)
    {
        [cell.imgPhoto loadImageFromURL:[NSURL URLWithString:user.userpic_url]];
    }
    if (indexPath.row == 1)
    {
        cell.imgPlaceBg.image = [UIImage imageNamed:@"rating_cell_first_place.png"];
    }
    else if (indexPath.row == 2)
    {
        cell.imgPlaceBg.image = [UIImage imageNamed:@"rating_cell_second_place.png"];
    }
    else if (indexPath.row == 3)
    {
        cell.imgPlaceBg.image = [UIImage imageNamed:@"rating_cell_third_place.png"];
    }
    else
    {
        cell.imgPlaceBg.image = [UIImage imageNamed:@"rating_cell_other_place_bg.png"];
        cell.lblPosition.hidden = NO;
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
    }
    
    if ([user.user_id compare:[GlobalData globalData].loggedInUser.user_id] == NSOrderedSame)
    {
        cell.imgBackground.image = [UIImage imageNamed:@"rating_cell_me_bg.png"];
    }
    else
    {
        cell.imgBackground.image = [UIImage imageNamed:@"rating_cell_bg.png"];
    }

    return cell;
}

@end
