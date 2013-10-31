//
//  LoginEnterViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/9/12.
//
//

#import "LoginEnterViewController.h"
#import "PuzzlesViewController.h"
#import "LoginRemindViewController.h"
#import "APIRequest.h"
#import "SBJson.h"
#import "GlobalData.h"
#import "UserData.h"
#import "AppDelegate.h"

@interface LoginEnterViewController (private)

-(void)handleKeyboardWillShow:(NSNotification *)aNotification;
-(void)handleKeyboardWillHide:(NSNotification *)aNotification;

-(void)handleEnterComplete:(NSHTTPURLResponse *)response receivedData:(NSData *)receivedData;
-(void)handleEnterFailed:(NSError *)error;

-(void)handleBackgroundTap:(id)sender;

@end

@implementation LoginEnterViewController

-(void)viewDidLoad
{
    [super viewDidLoad];
    self.title = NSLocalizedString(@"TITLE_ENTER", nil);
    scrollView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_dark_tile.jpg"]];
    /*
    if ([[UIDevice currentDevice].systemVersion compare:@"7.0" options:NSNumericSearch] != NSOrderedAscending)
    {
        scrollView.contentInset = UIEdgeInsetsMake([AppDelegate currentDelegate].isIPad ? 68 : 57, 0, 0, 0);
        scrollView.scrollIndicatorInsets = scrollView.contentInset;
    }
    */
    scrollView.autoresizesSubviews = NO;
    scrollView.bounces = NO;
    scrollView.contentSize = self.view.frame.size;
    scrollViewDefaultHeight = scrollView.bounds.size.height;
    UITapGestureRecognizer * tapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleBackgroundTap:)];
    tapGestureRecognizer.numberOfTapsRequired = 1;
    tapGestureRecognizer.numberOfTouchesRequired = 1;
    tapGestureRecognizer.delegate = self;
    [scrollView addGestureRecognizer:tapGestureRecognizer];
}

- (void)viewDidUnload {
    txtEmail = nil;
    txtPassword = nil;
    scrollView = nil;
    [super viewDidUnload];
}

-(void)viewWillAppear:(BOOL)animated
{
    NSLog(@"enter viewWillAppear");
    [super viewWillAppear:animated];
    activeResponder = nil;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    
}

-(void)viewWillDisappear:(BOOL)animated
{
    NSLog(@"enter viewWillDisappear");
    [super viewWillDisappear:animated];
    if (activeResponder != nil)
    {
        [activeResponder resignFirstResponder];
    }
    activeResponder = nil;
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillHideNotification object:nil];
}

- (IBAction)handleEnterClick:(id)sender
{
    if (txtEmail.text == nil || txtEmail.text.length == 0)
    {
        [[[UIAlertView alloc] initWithTitle:@"Ошибка" message:@"Вы не ввели e-mail. Введите и попробуйте еще раз." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] show];
        return;
    }
    if (txtPassword.text == nil || txtPassword.text.length == 0)
    {
        [[[UIAlertView alloc] initWithTitle:@"Ошибка" message:@"Вы не ввели пароль. Введите и попробуйте еще раз." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] show];
        return;
    }

    
    [self showActivityIndicator];
    APIRequest * request = [APIRequest postRequest:@"login" successCallback:^(NSHTTPURLResponse *response, NSData * receivedData) {
        [self handleEnterComplete:response receivedData:receivedData];
    } failCallback:^(NSError *error) {
        [self hideActivityIndicator];
    }];
    
    NSDateFormatter * dateFormatter = [NSDateFormatter new];
    [dateFormatter setDateFormat:@"yyyy-MM-dd"];
    
    [request.params setObject:txtEmail.text forKey:@"email"];
    [request.params setObject:txtPassword.text forKey:@"password"];
    [request runUsingCache:NO silentMode:NO];
}


-(void)handleEnterComplete:(NSHTTPURLResponse *)response receivedData:(NSData *)receivedData
{
    [self hideActivityIndicator];
    NSLog(@"login complete! %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
    [[GlobalData globalData] parseDateFromResponse:response];
    NSDictionary * json = [[SBJsonParser new] objectWithData:receivedData];
    [GlobalData globalData].sessionKey = [json objectForKey:@"session_key"];;
    [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:[json objectForKey:@"me"]];
    UINavigationController * navController = self.navigationController;
    [navController popViewControllerAnimated:NO];
    [navController pushViewController:[PuzzlesViewController new] animated:YES];
}


- (IBAction)handleForgetClick:(id)sender
{
    [self.navigationController pushViewController:[LoginRemindViewController new] animated:YES];
}

-(BOOL)textFieldShouldBeginEditing:(UITextField *)textField
{
    activeResponder = textField;
    return YES;
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    if (textField == txtEmail)
    {
        [textField resignFirstResponder];
        activeResponder = txtPassword;
        [txtPassword becomeFirstResponder];
        return NO;
    }
    else if (textField == txtPassword)
    {
        [textField resignFirstResponder];
        activeResponder = nil;
        [self handleEnterClick:self];
    }
    return YES;
}

-(void)handleKeyboardWillShow:(NSNotification *)aNotification
{
    NSDictionary * userInfo = aNotification.userInfo;
    CGRect endFrame = [(NSValue *)[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    
    UIViewAnimationCurve animationCurve = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] intValue];
    double animationDuration = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];
    
    [UIView setAnimationCurve:animationCurve];
    [UIView animateWithDuration:animationDuration animations:^{
        scrollView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height - endFrame.size.height);
    } completion:^(BOOL finished) {
        [scrollView scrollRectToVisible:activeResponder.frame animated:YES];
    }];
}

-(void)handleKeyboardWillHide:(NSNotification *)aNotification
{
        NSDictionary * userInfo = aNotification.userInfo;
        
        UIViewAnimationCurve animationCurve = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] intValue];
        double animationDuration = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];
        
        [UIView setAnimationCurve:animationCurve];
        [UIView animateWithDuration:animationDuration animations:^{
            scrollView.frame = CGRectMake(0, (self.view.frame.size.height - scrollViewDefaultHeight) / 2, self.view.bounds.size.width, scrollViewDefaultHeight);
        }];
    activeResponder = nil;
}

-(BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldReceiveTouch:(UITouch *)touch
{
    if ([touch.view isKindOfClass:[UIButton class]])
    {
        return NO;
    }
    return YES;
}

-(void)handleBackgroundTap:(id)sender
{
    if (activeResponder != nil)
    {
        [activeResponder resignFirstResponder];
    }
}


@end
