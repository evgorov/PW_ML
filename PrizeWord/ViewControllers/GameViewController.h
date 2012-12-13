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
    GameField * gameField;
    UITextField * textField;
    IBOutlet GameFieldView * gameFieldView;
    IBOutlet UIButton * btnPause;
    IBOutlet UIButton * btnPlay;
    IBOutlet UIButton * btnHint;
    IBOutlet UILabel * lblTime;
    IBOutlet UIView * viewTime;
    IBOutlet UIView *pauseOverlay;
    IBOutlet UISwitch *pauseSwtMusic;
    IBOutlet UISwitch *pauseSwtSound;
    IBOutlet UIImageView *pauseImgProgressbar;
    IBOutlet UILabel *pauseTxtProgress;
    float pauseMaxProgress;
    
    UIBarButtonItem * playPauseItem;
    UIBarButtonItem * hintButtonItem;
}

-(id)initWithGameField:(GameField *)gameField;

@end
