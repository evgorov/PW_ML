//
//  EventManager.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "EventManager.h"

@implementation EventManager

+ (EventManager *) sharedManager
{
    static EventManager *_sharedManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedManager = [[EventManager alloc] init];
    });
    return _sharedManager;
}

@end
