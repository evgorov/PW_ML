//
//  GameViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "GameViewController.h"
#import "GameTileView.h"

@interface GameViewController ()

@end

@implementation GameViewController

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
    tiles = [[NSMutableArray alloc] initWithCapacity:100];
    for (int i = 0; i != 100; ++i) {
        GameTileView * tile = [[GameTileView alloc] initWithFrame:CGRectMake(63 * (i % 10), 62 * (i / 10), 63, 62)];
        [tiles addObject:tile];
        [scrollView addSubview:tile];
    }
    scrollView.contentSize = CGSizeMake(630, 620);
    scrollView.bounces = NO;
}

- (void)viewDidUnload
{
    for (GameTileView * tile in tiles) {
        [tile removeFromSuperview];
    }
    [tiles removeAllObjects];
    tiles = nil;
    
    scrollView = nil;
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

@end
