//
//  NewsPageControl.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 3/20/13.
//
//

#import "NewsPageControl.h"

@interface NewsPageControl (private)

-(void)setupPages;

@end

@implementation NewsPageControl

const float DISTANCE = 16;

-(void)setNumberOfPages:(NSInteger)numberOfPages
{
    [super setNumberOfPages:numberOfPages];
    [self setupPages];
}

-(void)setCurrentPage:(NSInteger)currentPage
{
    [super setCurrentPage:currentPage];

    [self setupPages];
}

-(void)updateCurrentPageDisplay
{
    [super updateCurrentPageDisplay];
    [self setupPages];
}

-(void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
    [super touchesEnded:touches withEvent:event];
    [self setupPages];
}

-(void)setupPages
{
    while (self.subviews.count > 0)
    {
        [[self.subviews objectAtIndex:self.subviews.count - 1] removeFromSuperview];
    }
    while (self.subviews.count < self.numberOfPages)
    {
        [self addSubview:[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"puzzles_news_page.png"] highlightedImage:[UIImage imageNamed:@"puzzles_news_page_current.png"]]];
    }
    
    int i = 0;
    for (UIImageView * subview in self.subviews)
    {
        subview.highlighted = (i == self.currentPage);
        subview.frame = CGRectMake(self.frame.size.width / 2 - subview.frame.size.width / 2 - DISTANCE * (self.numberOfPages - 1) / 2 + i * DISTANCE, self.frame.size.height / 2 - subview.frame.size.height / 2, subview.frame.size.width, subview.frame.size.height);
        ++i;
    }
}

@end
