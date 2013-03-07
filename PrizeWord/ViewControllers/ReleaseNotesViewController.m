//
//  ReleaseNotesViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/3/12.
//
//

#import "ReleaseNotesViewController.h"

@interface ReleaseNotesViewController ()

@end

@implementation ReleaseNotesViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    NSString * path = [[NSBundle mainBundle] pathForResource:@"ReleaseNotes" ofType:@"txt"];
    NSError * error;
    NSString * releaseNotes = [NSString stringWithContentsOfFile:path encoding:NSUTF8StringEncoding error:&error];
    
    textView.text = releaseNotes;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)handleMainMenuClick:(id)sender
{
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)viewDidUnload {
    textView = nil;
    [super viewDidUnload];
}
@end
