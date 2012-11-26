//
//  GameTileView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/25/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "GameTileView.h"
#import "EventManager.h"
#import "TileData.h"

@interface GameTileView ()

-(void)onTap;
-(void)initParts;

@end

@implementation GameTileView

- (id)initWithFrame:(CGRect)frame andData:(TileData *)data
{
    self = [super initWithFrame:frame];
    if (self) {
        background = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"tile_ready_to_input"]];
        tileData = data;
        [self initParts];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_TILE_CHANGE];
        [self addSubview:background];
        
        self.userInteractionEnabled = YES;
        UITapGestureRecognizer * tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTap)];
        [self addGestureRecognizer:tapRecognizer];
    }
    return self;
}

-(void)dealloc
{
    NSLog(@"TileView dealloc");
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_TILE_CHANGE];
    tileData = nil;
}

-(void)handleEvent:(Event *)event
{
    switch (event.type) {
        case EVENT_TILE_CHANGE:
        {
            TileData * newData = (TileData *)event.data;
            if (tileData.x == newData.x && tileData.y == newData.y)
            {
                // TODO :: play animation
                tileData = newData;
                [self initParts];
            }
            break;
        }
            
        default:
            break;
    }
}

-(void)initParts
{
    if (tileData.state == TILE_INACTIVE)
    {
        [background setImage:[UIImage imageNamed:@"tile_letter_fixed"]];
    }
    else {
        [background setImage:[UIImage imageNamed:@"tile_ready_to_input"]];
    }
}

-(void)onTap
{
    [[EventManager sharedManager] dispatchEventWithType:[Event eventWithType:EVENT_TILE_TAP andData:tileData]];
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
