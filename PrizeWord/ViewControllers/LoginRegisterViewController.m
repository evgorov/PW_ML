//
//  LoginRegisterViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/6/12.
//
//

#import "LoginRegisterViewController.h"

@interface LoginRegisterViewController ()

@end

@implementation LoginRegisterViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    scrollView.contentSize = imgBackground.frame.size;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewDidUnload {
    tfName = nil;
    tfSurname = nil;
    tfEmail = nil;
    tfPassword = nil;
    tfPasswordRepeat = nil;
    tfCity = nil;
    datePicker = nil;
    imgBackground = nil;
    scrollView = nil;
    [super viewDidUnload];
}
- (IBAction)handleDateSelect:(id)sender {
}
@end
