//
//  PuzzlesViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/15/12.
//
//

#import "PuzzlesViewController.h"

@interface PuzzlesViewController ()

@end

@implementation PuzzlesViewController

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
    
    UIImage * border = [UIImage imageNamed:@"frame_border"];
    if ([border respondsToSelector:@selector(resizableImageWithCapInsets:)])
    {
        border = [border resizableImageWithCapInsets:UIEdgeInsetsMake(border.size.height / 2 - 1, border.size.width / 2 - 1, border.size.height / 2, border.size.width / 2)];
    }
    else
    {
        border = [border stretchableImageWithLeftCapWidth:(border.size.width / 2 - 1) topCapHeight:(border.size.height / 2 - 1)];
    }
    
    contentView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_dark_tile.jpg"]];
    currentPuzzlesView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_sand_tile.jpg"]];
    currentPuzzlesBorder.image = border;
    scrollView.contentSize = contentView.frame.size;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewDidUnload {
    scrollView = nil;
    contentView = nil;
    newsView = nil;
    currentPuzzlesView = nil;
    currentPuzzlesBorder = nil;
    [super viewDidUnload];
}
- (IBAction)handleNewsCloseClick:(id)sender {
}
@end
