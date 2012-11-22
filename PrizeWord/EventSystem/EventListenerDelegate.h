//
//  EventHandlerDelegate.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#ifndef PrizeWord_EventListenerDelegate_h
#define PrizeWord_EventListenerDelegate_h

#import "Event.h"

@protocol EventListenerDelegate <NSObject>

-(void)handleEvent:(Event *) event;

@end

#endif
