//
//  GameViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <UIKit/UIKit.h>

@class GameFieldView;
@class GameField;

@interface GameViewController : UIViewController
{
    GameFieldView * gameFieldView;
    GameField * gameField;
}

-(id)initWithGameField:(GameField *)gameField;

@end
