//
//  LoginRegisterViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/6/12.
//
//

#import "LoginRegisterViewController.h"
#import "PuzzlesViewController.h"

@interface LoginRegisterViewController ()

-(void)handleDateChanged:(id)sender;
-(void)handleKeyboardWillShow:(NSNotification *)aNotification;
-(void)handleKeyboardWillHide:(NSNotification *)aNotification;

@end

@implementation LoginRegisterViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.title = NSLocalizedString(@"TITLE_REGISTRATION", nil);
    
    scrollView.contentSize = imgBackground.frame.size;
    [datePicker addTarget:self action:@selector(handleDateChanged:) forControlEvents:UIControlEventValueChanged];
    datePicker.date = [NSDate new];
    [self handleDateChanged:self];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    activeResponder = nil;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [activeResponder resignFirstResponder];
    activeResponder = nil;
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillHideNotification object:nil];
}

- (void)viewDidUnload {
    [datePicker removeTarget:self action:@selector(handleDateChanged:) forControlEvents:UIControlEventValueChanged];
    
    tfName = nil;
    tfSurname = nil;
    tfEmail = nil;
    tfPassword = nil;
    tfPasswordRepeat = nil;
    tfCity = nil;
    datePicker = nil;
    imgBackground = nil;
    scrollView = nil;
    btnBirthday = nil;
    
    datePickerView = nil;
    [super viewDidUnload];
}

- (IBAction)handleAvaClick:(id)sender
{
}

- (IBAction)handleBirthdayClick:(id)sender
{
    if (datePickerView.hidden)
    {
        CGRect datePickerFrame = datePickerView.frame;

        datePickerView.hidden = NO;
        datePickerFrame.origin.y = self.view.frame.size.height - datePickerFrame.size.height;
        [UIView animateWithDuration:0.3 animations:^{
            datePickerView.frame = datePickerFrame;
            scrollView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height - datePickerView.frame.size.height);
        } completion:^(BOOL finished) {
            [scrollView scrollRectToVisible:btnBirthday.frame animated:YES];
        }];
        
        if (activeResponder != nil)
        {
            [activeResponder resignFirstResponder];
            activeResponder = nil;
        }
    }
}

- (IBAction)handleDatePickerDoneClick:(id)sender
{
    if (!datePickerView.hidden)
    {
        CGRect datePickerFrame = datePickerView.frame;
        datePickerFrame.origin.y = self.view.frame.size.height;

        [UIView animateWithDuration:0.3 animations:^{
            if (activeResponder == nil)
            {
                scrollView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
            }
            datePickerView.frame = datePickerFrame;
        } completion:^(BOOL finished) {
            datePickerView.hidden = YES;
        }];
    }
}

- (IBAction)handleRegisterClick:(UIButton *)sender
{
    [activeResponder resignFirstResponder];
    [self handleDatePickerDoneClick:self];
    [self showActivityIndicator];
    [NSTimer scheduledTimerWithTimeInterval:2 target:self selector:@selector(gotoRoot:) userInfo:nil repeats:NO];
}

-(void)handleDateChanged:(id)sender
{
    NSString * dateString = [NSDateFormatter localizedStringFromDate:datePicker.date dateStyle:NSDateFormatterLongStyle timeStyle:NSDateFormatterNoStyle];
    
    [btnBirthday setTitle:dateString forState:UIControlStateNormal];
    [scrollView scrollRectToVisible:btnBirthday.frame animated:YES];
}

-(BOOL)textFieldShouldBeginEditing:(UITextField *)textField
{
    activeResponder = textField;
    if (!datePickerView.hidden)
    {
        [self handleDatePickerDoneClick:self];
    }
    return YES;
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    activeResponder = nil;
    
    if (textField == tfName)
    {
        activeResponder = tfSurname;
    }
    else if (textField == tfSurname)
    {
        activeResponder = tfEmail;
    }
    else if (textField == tfEmail)
    {
        activeResponder = tfPassword;
    }
    else if (textField == tfPassword)
    {
        activeResponder = tfPasswordRepeat;
    }
    else if (textField == tfPasswordRepeat)
    {
        [self handleBirthdayClick:self];
    }
    else if (textField == tfCity)
    {
    }
    if (activeResponder != nil)
    {
        [activeResponder becomeFirstResponder];
//        [scrollView scrollRectToVisible:activeResponder.frame animated:YES];
        return NO;
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
    if (datePickerView.hidden)
    {
        NSDictionary * userInfo = aNotification.userInfo;

        UIViewAnimationCurve animationCurve = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] intValue];
        double animationDuration = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];

        [UIView setAnimationCurve:animationCurve];
        [UIView animateWithDuration:animationDuration animations:^{
            scrollView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
        }];
    }
}

-(void)gotoRoot:(id)sender
{
    [self hideActivityIndicator];
    UINavigationController * navController = self.navigationController;
    [navController popViewControllerAnimated:NO];
    [navController pushViewController:[PuzzlesViewController new] animated:YES];
}

@end
