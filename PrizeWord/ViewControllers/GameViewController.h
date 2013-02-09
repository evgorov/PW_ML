//
//  GameViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "EventListenerDelegate.h"
#import "PrizeWordSwitchView.h"

@class GameFieldView;
@class GameField;
@class FlipNumberView;
@class PuzzleData;
@interface GameViewController : UIViewController<EventListenerDelegate, UITextFieldDelegate, UIAlertViewDelegate>
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
    IBOutlet UIImageView *pauseImgProgressbar;
    IBOutlet PrizeWordSwitchView *pauseSwtMusic;
    IBOutlet PrizeWordSwitchView *pauseSwtSound;
    IBOutlet UILabel *pauseTxtProgress;
    float pauseMaxProgress;

    IBOutlet UIView *finalOverlay;
    IBOutlet UIView *finalShareView;
    IBOutlet UILabel *lblFinalBaseScore;
    IBOutlet UILabel *lblFinalTimeBonus;
    IBOutlet FlipNumberView *finalFlipNumber4;
    IBOutlet FlipNumberView *finalFlipNumber3;
    IBOutlet FlipNumberView *finalFlipNumber2;
    IBOutlet FlipNumberView *finalFlipNumber1;
    IBOutlet FlipNumberView *finalFlipNumber0;
    NSArray * finalFlipNumbers;
    PuzzleData * puzzleData;

    UIBarButtonItem * playPauseItem;
    UIBarButtonItem * hintButtonItem;
}

-(id)initWithGameField:(GameField *)gameField;

@end
