//
//  NewsCell.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/7/13.
//
//

#import "NewsCell.h"
#import "DataManager.h"
#import "NewsPageControl.h"
#import "AppDelegate.h"

@interface NewsCell ()
{
    __weak IBOutlet UILabel *newsLbl1;
    __weak IBOutlet UILabel *newsLbl2;
    __weak IBOutlet UILabel *newsLbl3;
    __weak IBOutlet NewsPageControl *newsPaginator;
    __weak IBOutlet UIScrollView *newsScrollView;
    __weak IBOutlet UIActivityIndicatorView *activityIndicator;
    BOOL handleScrollEvent;
    BOOL isInitialized;
}

- (IBAction)handleNewsPaginatorChange:(id)sender;

@end

@implementation NewsCell

+ (float)height
{
    return [AppDelegate currentDelegate].isIPad ? 136 : 110;
}

-(id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self != nil)
    {
        isInitialized = NO;
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:NO animated:NO];
}

- (void)setup
{
    if (!isInitialized)
    {
        handleScrollEvent = YES;
        self.clipsToBounds = YES;
        [activityIndicator startAnimating];
        activityIndicator.hidden = NO;
        [[DataManager sharedManager] fetchNewsWithCompletion:^(NSArray *data, NSError *error) {
            isInitialized = YES;
            dispatch_async(dispatch_get_main_queue(), ^{
                [activityIndicator stopAnimating];
                activityIndicator.hidden = YES;
                if (data != nil && data.count > 0)
                {
                    newsPaginator.numberOfPages = data.count;
                    if (data.count >= 1)
                    {
                        newsLbl1.text = [data objectAtIndex:0];
                    }
                    if (data.count >= 2)
                    {
                        newsLbl2.text = [data objectAtIndex:1];
                    }
                    if (data.count >= 3)
                    {
                        newsLbl3.text = [data objectAtIndex:2];
                    }
                    [newsScrollView setContentSize:CGSizeMake(newsPaginator.numberOfPages * newsScrollView.frame.size.width, newsScrollView.frame.size.height)];
                }
                else
                {
                    [self.btnClose sendActionsForControlEvents:UIControlEventTouchUpInside];
                }
            });
        }];
    }
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    if (handleScrollEvent)
    {
        int page = (scrollView.contentOffset.x / scrollView.bounds.size.width + 0.5);
        if (page >= newsPaginator.numberOfPages)
            page = newsPaginator.numberOfPages - 1;
        newsPaginator.currentPage = page;
    }
}

- (IBAction)handleNewsPaginatorChange:(id)sender
{
    [newsScrollView setContentOffset:CGPointMake(newsPaginator.currentPage * newsScrollView.frame.size.width, 0) animated:YES];
    handleScrollEvent = NO;
    double delayInSeconds = 0.2;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        handleScrollEvent = YES;
    });
}

@end
