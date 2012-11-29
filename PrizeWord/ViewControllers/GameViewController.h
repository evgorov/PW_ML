//
//  GameViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "EventListenerDelegate.h"

@class GameFieldView;
@class GameField;

@interface GameViewController : UIViewController<EventListenerDelegate, UITextFieldDelegate>
{
    GameFieldView * gameFieldView;
    GameField * gameField;
    UITextField * textField;
}

-(id)initWithGameField:(GameField *)gameField;

@end
