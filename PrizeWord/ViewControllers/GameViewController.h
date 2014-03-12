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
#import "PrizeWordViewController.h"
#import <StoreKit/SKProductsRequest.h>

@class GameFieldView;
@class GameField;
@class FlipNumberView;
@class PuzzleProxy;
@class FISound;

@interface GameViewController : PrizeWordViewController<EventListenerDelegate, UITextFieldDelegate, UIAlertViewDelegate, SKProductsRequestDelegate>
{
    GameField * gameField;
    UITextField * textField;
    IBOutlet GameFieldView * gameFieldView;
    IBOutlet PrizeWordButton * btnPause;
    IBOutlet PrizeWordButton * btnPlay;
    IBOutlet PrizeWordButton * btnHint;
    IBOutlet UILabel * lblTime;
    IBOutlet UIView * viewTime;
    IBOutlet UIView *pauseOverlay;
    IBOutlet UIImageView *pauseImgProgressbar;
    IBOutlet PrizeWordSwitchView *pauseSwtMusic;
    IBOutlet PrizeWordSwitchView *pauseSwtSound;
    IBOutlet UILabel *pauseTxtProgress;
    float pauseMaxProgress;

    UIView *finalOverlay;
    PuzzleProxy *puzzleData;
    IBOutlet UIView *finalOverlayOrdinary;
    IBOutlet UIView *finalOverlayRate;
    IBOutlet UIView *finalOverlayRateDone;
    IBOutlet UIView *finalOverlaySet;
    IBOutlet UIView *finalOverlaySetFBDone;
    IBOutlet UIView *finalOverlaySetVKDone;
    IBOutlet UIView *finalOverlaySetDone;
    
    UILabel *lblFinalBaseScore;
    UILabel *lblFinalTimeBonus;
    UILabel *lblFinalRateBonus;
    UILabel *lblFinalFacebookBonus;
    UILabel *lblFinalVkontakteBonus;
    UIImageView *imgFinalSetType;
    NSMutableArray * finalFlipNumbers;
    UIButton *btnFinalMenu;
    UIButton *btnFinalNext;
    UIButton *btnFinalContinue;

    UIBarButtonItem * playPauseItem;
    UIBarButtonItem * hintButtonItem;
    
    FISound * puzzleSolvedSound;
    NSArray * typeSounds;
    FISound * countingSound;
    FISound * secondaryDingSound;
}

-(id)initWithGameField:(GameField *)gameField;

@end
