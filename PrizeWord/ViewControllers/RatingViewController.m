//
//  RatingViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import "RatingViewController.h"

@interface RatingViewController ()

@end

@implementation RatingViewController

- (void)viewDidLoad
{
    [super viewDidLoad];

    self.title = @"456-ой в рейтинге";

    [self addFramedView:ratingView];
}

- (void)viewDidUnload {
    ratingView = nil;
    [super viewDidUnload];
}

@end
