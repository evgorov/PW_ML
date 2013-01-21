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

@interface LoginEnterViewController (private)

-(void)handleEnterComplete:(NSHTTPURLResponse *)response receivedData:(NSData *)receivedData;
-(void)handleEnterFailed:(NSError *)error;

@end

@implementation LoginEnterViewController

-(void)viewDidLoad
{
    [super viewDidLoad];
    self.title = NSLocalizedString(@"TITLE_ENTER", nil);
}

- (void)viewDidUnload {
    txtEmail = nil;
    txtPassword = nil;
    [super viewDidUnload];
}

- (IBAction)handleEnterClick:(id)sender
{
    [self showActivityIndicator];
    APIRequest * request = [APIRequest postRequest:@"login" successCallback:^(NSHTTPURLResponse *response, NSData * receivedData) {
        [self handleEnterComplete:response receivedData:receivedData];
    } failCallback:^(NSError *error) {
        [self handleEnterFailed:error];
    }];
    
    NSDateFormatter * dateFormatter = [NSDateFormatter new];
    [dateFormatter setDateFormat:@"yyyy-MM-dd"];
    
    [request.params setObject:txtEmail.text forKey:@"email"];
    [request.params setObject:txtPassword.text forKey:@"password"];
    [request runSilent];
}


-(void)handleEnterComplete:(NSHTTPURLResponse *)response receivedData:(NSData *)receivedData
{
    [self hideActivityIndicator];
    if (response.statusCode == 200)
    {
        NSLog(@"login complete! %@", [NSString stringWithUTF8String:receivedData.bytes]);
        NSDictionary * json = [[SBJsonParser new] objectWithData:receivedData];
        [GlobalData globalData].sessionKey = [json objectForKey:@"session_key"];;
        [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:[json objectForKey:@"me"]];
        UINavigationController * navController = self.navigationController;
        [navController popViewControllerAnimated:NO];
        [navController pushViewController:[PuzzlesViewController new] animated:YES];
    }
    else
    {
        NSLog(@"login failed! %d %@", response.statusCode, [NSString stringWithUTF8String:receivedData.bytes]);
        UIAlertView * alert = [[UIAlertView alloc] initWithTitle:[NSString stringWithFormat:@"%d", response.statusCode] message:[NSString stringWithUTF8String:receivedData.bytes] delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
        alert.tag = 1;
        [alert show];
    }
}

-(void)handleEnterFailed:(NSError *)error
{
    [self hideActivityIndicator];
    NSLog(@"login failed! %@", error.description);
    UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка" message:@"Ошибка соединения с сервером. Попробуйте ещё раз." delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
    alert.tag = 0;
    [alert show];
}

- (IBAction)handleForgetClick:(id)sender
{
    [self.navigationController pushViewController:[LoginRemindViewController new] animated:YES];
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    if (textField == txtEmail)
    {
        [textField resignFirstResponder];
        [txtPassword becomeFirstResponder];
        return NO;
    }
    else if (textField == txtPassword)
    {
        [textField resignFirstResponder];
        [self handleEnterClick:self];
    }
    return YES;
}

-(void)alertView:(UIAlertView *)alertView willDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (buttonIndex != alertView.cancelButtonIndex)
    {
        [self handleEnterClick:nil];
    }
}

@end
