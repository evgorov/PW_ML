//
//  GameLogic.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "GameLogic.h"
#import "AppDelegate.h"
#import "GameViewController.h"
#import "EventManager.h"
#import "GameField.h"
#import "PuzzleData.h"
#import "QuestionData.h"
#import "PuzzleSetData.h"
#import "PrizeWordNavigationController.h"
#import "RootViewController.h"

@interface GameLogic (private)

-(void)initGameFieldWithType:(LetterType)type;
-(void)handleTimer:(id)userInfo;

@end

@implementation GameLogic

@synthesize gameTime = _gameTime;

-(id)init
{
    self = [super init];
    if (self)
    {
        currentGameField = nil;
        gameTimer = [NSTimer scheduledTimerWithTimeInterval:(1/4.0) target:self selector:@selector(handleTimer:) userInfo:nil repeats:YES];
        _gameTime = 0;
        gameState = GAMESTATE_NOT_STARTED;
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_START];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_PAUSE];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_RESUME];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_COMPLETE];
    }
    return self;
}

-(void)dealloc
{
    [gameTimer invalidate];
    gameTimer = nil;
}

+(GameLogic *)sharedLogic
{
    static GameLogic * _sharedLogic = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedLogic = [[GameLogic alloc] init];
    });
    return _sharedLogic;
}

-(GameField *)gameField
{
    return currentGameField;
}

-(void)handleEvent:(Event *)event
{
    switch (event.type)
    {
        case EVENT_GAME_REQUEST_START:
        {
            LetterType type = (LetterType)([(NSNumber *)event.data intValue]);
            [[AppDelegate currentDelegate].rootViewController hideMenuAnimated:YES];
            if ([[AppDelegate currentDelegate].navController.topViewController isKindOfClass:[GameViewController class]])
            {
                [[AppDelegate currentDelegate].navController popViewControllerAnimated:NO];
                [self initGameFieldWithType:type];
                [[AppDelegate currentDelegate].navController pushViewController:[[GameViewController alloc] initWithGameField:currentGameField] animated:NO];
            }
            else
            {
                [self initGameFieldWithType:type];
                [[AppDelegate currentDelegate].navController pushViewController:[[GameViewController alloc] initWithGameField:currentGameField] animated:YES];
            }
            gameState = GAMESTATE_PLAYING;
            _gameTime = 0;
        }
            break;

        case EVENT_GAME_REQUEST_PAUSE:
            gameState = GAMESTATE_PAUSED;
            break;

        case EVENT_GAME_REQUEST_RESUME:
            gameState = GAMESTATE_PLAYING;
            break;

        case EVENT_GAME_REQUEST_COMPLETE:
            gameState = GAMESTATE_FINISHED;
            break;

        default:
            break;
    }
}

-(void)initGameFieldWithType:(LetterType)type
{
    NSManagedObjectContext * managedObjectContext = [AppDelegate currentDelegate].managedObjectContext;
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *puzzleSetEntity = [NSEntityDescription entityForName:@"PuzzleSet" inManagedObjectContext:managedObjectContext];
    
    [request setEntity:puzzleSetEntity];
    [request setFetchLimit:1];
    [request setPredicate:[NSPredicate predicateWithFormat:@"type = %d", type]];
    
    NSError *error = nil;
    NSArray *puzzleSets = [managedObjectContext executeFetchRequest:request error:&error];
    
    PuzzleData * puzzle;
    if (puzzleSets == nil || puzzleSets.count == 0)
    {
        PuzzleSetData * puzzleSet = (PuzzleSetData *)[NSEntityDescription insertNewObjectForEntityForName:@"PuzzleSet" inManagedObjectContext:managedObjectContext];
        [puzzleSet setSet_id:[NSString stringWithFormat:@"set_%d", type]];
        [puzzleSet setType:[NSNumber numberWithInt:type]];
        [puzzleSet setBought:[NSNumber numberWithBool:YES]];
        [puzzleSet setName:@"Set"];
        
        QuestionData * question;
        
        puzzle = (PuzzleData *)[NSEntityDescription insertNewObjectForEntityForName:@"Puzzle" inManagedObjectContext:managedObjectContext];
        [puzzle setWidth:[NSNumber numberWithUnsignedInt:14]];
        [puzzle setHeight:[NSNumber numberWithUnsignedInt:20]];
        [puzzle setIssuedAt:[NSDate date]];
        [puzzle setBase_score:[NSNumber numberWithUnsignedInt:1000]];
        [puzzle setName:@"Сканворд 1"];
        [puzzle setTime_given:[NSNumber numberWithUnsignedInt:10000]];
        [puzzleSet addPuzzlesObject:puzzle];

        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"математик"];
        [question setAnswer_positionAsString:@"west:bottom"];
        [question setQuestion_text:@"Пифагор\nили Лоба-\nчевский"];
        [question setRow:[NSNumber numberWithUnsignedInt:0]];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"дикарь"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Курортник без путёвки"];
        [question setRow:[NSNumber numberWithUnsignedInt:0]];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"трал"];
        [question setAnswer_positionAsString:@"south-west:right"];
        [question setQuestion_text:@"\"Сачёк\" для ловли мин"];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"толпа"];
        [question setAnswer_positionAsString:@"north:right"];
        [question setQuestion_text:@"Сборище народу"];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [question setColumn:[NSNumber numberWithUnsignedInt:2]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"область"];
        [question setAnswer_positionAsString:@"north-west:bottom"];
        [question setQuestion_text:@"Сфера деятельности"];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"папироса"];
        [question setAnswer_positionAsString:@"north-west:bottom"];
        [question setQuestion_text:@"Беломорина в пепельнице"];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"длина"];
        [question setAnswer_positionAsString:@"north-east:bottom"];
        [question setQuestion_text:@"Размер вдоль"];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"лава"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Извержения вулкана"];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [question setColumn:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"клад"];
        [question setAnswer_positionAsString:@"north-west:bottom"];
        [question setQuestion_text:@"Зарытые сокровища"];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [question setColumn:[NSNumber numberWithUnsignedInt:11]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"рагу"];
        [question setAnswer_positionAsString:@"north-west:bottom"];
        [question setQuestion_text:@"Тущёные овощи"];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [question setColumn:[NSNumber numberWithUnsignedInt:13]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"перила"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Поручни лестницы"];
        [question setRow:[NSNumber numberWithUnsignedInt:2]];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"книга"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Учебник"];
        [question setRow:[NSNumber numberWithUnsignedInt:2]];
        [question setColumn:[NSNumber numberWithUnsignedInt:11]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"стать"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Молодецкая выправка"];
        [question setRow:[NSNumber numberWithUnsignedInt:2]];
        [question setColumn:[NSNumber numberWithUnsignedInt:13]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"марс"];
        [question setAnswer_positionAsString:@"south-west:right"];
        [question setQuestion_text:@"Багровая планета"];
        [question setRow:[NSNumber numberWithUnsignedInt:3]];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"али"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Боксёр Мухамед ..."];
        [question setRow:[NSNumber numberWithUnsignedInt:3]];
        [question setColumn:[NSNumber numberWithUnsignedInt:2]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"крем"];
        [question setAnswer_positionAsString:@"south-east:bottom"];
        [question setQuestion_text:@"Начинка эклера"];
        [question setRow:[NSNumber numberWithUnsignedInt:3]];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"надкус"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"След зубов на съестном"];
        [question setRow:[NSNumber numberWithUnsignedInt:3]];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"рукав"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Пожарный шланг"];
        [question setRow:[NSNumber numberWithUnsignedInt:4]];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ладоши"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Хлопайте в ... вы (песен.)"];
        [question setRow:[NSNumber numberWithUnsignedInt:4]];
        [question setColumn:[NSNumber numberWithUnsignedInt:10]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"сантим"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"1/100 франка"];
        [question setRow:[NSNumber numberWithUnsignedInt:4]];
        [question setColumn:[NSNumber numberWithUnsignedInt:12]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"топь"];
        [question setAnswer_positionAsString:@"south-west:right"];
        [question setQuestion_text:@"Вязкое место на болоте"];
        [question setRow:[NSNumber numberWithUnsignedInt:5]];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"попугай"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"И какаду, и жако"];
        [question setRow:[NSNumber numberWithUnsignedInt:5]];
        [question setColumn:[NSNumber numberWithUnsignedInt:2]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"свет"];
        [question setAnswer_positionAsString:@"south-east:right"];
        [question setQuestion_text:@"Лучистая энергия"];
        [question setRow:[NSNumber numberWithUnsignedInt:5]];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"врач"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Доктор в клинике"];
        [question setRow:[NSNumber numberWithUnsignedInt:5]];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"табак"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Зелье курильщика"];
        [question setRow:[NSNumber numberWithUnsignedInt:5]];
        [question setColumn:[NSNumber numberWithUnsignedInt:8]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"каток"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Ледовый стадион"];
        [question setRow:[NSNumber numberWithUnsignedInt:6]];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"алиса"];
        [question setAnswer_positionAsString:@"north:right"];
        [question setQuestion_text:@"Сообщница Базилио"];
        [question setRow:[NSNumber numberWithUnsignedInt:6]];
        [question setColumn:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"копна"];
        [question setAnswer_positionAsString:@"south-west:right"];
        [question setQuestion_text:@"Стог сена"];
        [question setRow:[NSNumber numberWithUnsignedInt:7]];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"карма"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Судьба индуса"];
        [question setRow:[NSNumber numberWithUnsignedInt:7]];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"агат"];
        [question setAnswer_positionAsString:@"north-east:right"];
        [question setQuestion_text:@"Камень для Тельцов"];
        [question setRow:[NSNumber numberWithUnsignedInt:7]];
        [question setColumn:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"омега"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Альфа и ..."];
        [question setRow:[NSNumber numberWithUnsignedInt:8]];
        [question setColumn:[NSNumber numberWithUnsignedInt:5]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"каркас"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"технический скелет"];
        [question setRow:[NSNumber numberWithUnsignedInt:8]];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"дань"];
        [question setAnswer_positionAsString:@"north-east:right"];
        [question setQuestion_text:@"Отдать ... уважения"];
        [question setRow:[NSNumber numberWithUnsignedInt:8]];
        [question setColumn:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"победа"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Разгром врага"];
        [question setRow:[NSNumber numberWithUnsignedInt:8]];
        [question setColumn:[NSNumber numberWithUnsignedInt:11]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"царь"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Титул Ивана Грозного"];
        [question setRow:[NSNumber numberWithUnsignedInt:8]];
        [question setColumn:[NSNumber numberWithUnsignedInt:13]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"пиршество"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Обильное угощение"];
        [question setRow:[NSNumber numberWithUnsignedInt:9]];
        [question setColumn:[NSNumber numberWithUnsignedInt:0]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"погром"];
        [question setAnswer_positionAsString:@"south-west:right"];
        [question setQuestion_text:@"Хаос после обыска"];
        [question setRow:[NSNumber numberWithUnsignedInt:9]];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"точка"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"В конце предложения"];
        [question setRow:[NSNumber numberWithUnsignedInt:9]];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"шпиц"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Комнатная собачка"];
        [question setRow:[NSNumber numberWithUnsignedInt:9]];
        [question setColumn:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"аксиома"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Её доказывать не надо"];
        [question setRow:[NSNumber numberWithUnsignedInt:10]];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"рейс"];
        [question setAnswer_positionAsString:@"south-west:right"];
        [question setQuestion_text:@"Постоянный маршрут"];
        [question setRow:[NSNumber numberWithUnsignedInt:11]];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"кедр"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"хвойный с шишками"];
        [question setRow:[NSNumber numberWithUnsignedInt:11]];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"скрип"];
        [question setAnswer_positionAsString:@"north-east:bottom"];
        [question setQuestion_text:@"Диалог старых половиц"];
        [question setRow:[NSNumber numberWithUnsignedInt:11]];
        [question setColumn:[NSNumber numberWithUnsignedInt:8]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"турникет"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Пропускник в метро"];
        [question setRow:[NSNumber numberWithUnsignedInt:11]];
        [question setColumn:[NSNumber numberWithUnsignedInt:10]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"чан"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Большой бак"];
        [question setRow:[NSNumber numberWithUnsignedInt:11]];
        [question setColumn:[NSNumber numberWithUnsignedInt:12]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"сводница"];
        [question setAnswer_positionAsString:@"west:bottom"];
        [question setQuestion_text:@"Посредница в амурных делах"];
        [question setRow:[NSNumber numberWithUnsignedInt:12]];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"картечь"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Крупная дробь охотника"];
        [question setRow:[NSNumber numberWithUnsignedInt:12]];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"евро"];
        [question setAnswer_positionAsString:@"south-west:right"];
        [question setQuestion_text:@"Валюта стран ЕС"];
        [question setRow:[NSNumber numberWithUnsignedInt:13]];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"влага"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"сырость воздуха"];
        [question setRow:[NSNumber numberWithUnsignedInt:13]];
        [question setColumn:[NSNumber numberWithUnsignedInt:2]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"иуда"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Предавший Христа"];
        [question setRow:[NSNumber numberWithUnsignedInt:13]];
        [question setColumn:[NSNumber numberWithUnsignedInt:8]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"опаска"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Осторожность (разг.)"];
        [question setRow:[NSNumber numberWithUnsignedInt:13]];
        [question setColumn:[NSNumber numberWithUnsignedInt:13]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"дека"];
        [question setAnswer_positionAsString:@"south-west:right"];
        [question setQuestion_text:@"Резонатор балалайки"];
        [question setRow:[NSNumber numberWithUnsignedInt:14]];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"коала"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Сумчатый Топтыгин"];
        [question setRow:[NSNumber numberWithUnsignedInt:14]];
        [question setColumn:[NSNumber numberWithUnsignedInt:5]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ампир"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Стиль в архитектуре"];
        [question setRow:[NSNumber numberWithUnsignedInt:14]];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [puzzle addQuestionsObject:question];
        
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"трон"];
        [question setAnswer_positionAsString:@"south-west:right"];
        [question setQuestion_text:@"Кресло монарха"];
        [question setRow:[NSNumber numberWithUnsignedInt:15]];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"опер"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Сыскарь (разг.)"];
        [question setRow:[NSNumber numberWithUnsignedInt:15]];
        [question setColumn:[NSNumber numberWithUnsignedInt:2]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"сопрано"];
        [question setAnswer_positionAsString:@"north:right"];
        [question setQuestion_text:@"Высокий женский голос"];
        [question setRow:[NSNumber numberWithUnsignedInt:15]];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"остров"];
        [question setAnswer_positionAsString:@"north-west:bottom"];
        [question setQuestion_text:@"Сахалин, Таити, Ямайка"];
        [question setRow:[NSNumber numberWithUnsignedInt:15]];
        [question setColumn:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"руда"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Попрода, богатая металлом"];
        [question setRow:[NSNumber numberWithUnsignedInt:15]];
        [question setColumn:[NSNumber numberWithUnsignedInt:11]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"грог"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Горячий алкоголь"];
        [question setRow:[NSNumber numberWithUnsignedInt:15]];
        [question setColumn:[NSNumber numberWithUnsignedInt:12]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"омут"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Впадина на дне реки"];
        [question setRow:[NSNumber numberWithUnsignedInt:16]];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ирга"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Крымский куст-медонос"];
        [question setRow:[NSNumber numberWithUnsignedInt:16]];
        [question setColumn:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"пикап"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"\"Каблучок\" на колёсах"];
        [question setRow:[NSNumber numberWithUnsignedInt:17]];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ракурс"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Угол зрения"];
        [question setRow:[NSNumber numberWithUnsignedInt:17]];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"лицо"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Бросить перчатку в ..."];
        [question setRow:[NSNumber numberWithUnsignedInt:18]];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"едок"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Клиент столовой"];
        [question setRow:[NSNumber numberWithUnsignedInt:18]];
        [question setColumn:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"отец"];
        [question setAnswer_positionAsString:@"north:right"];
        [question setQuestion_text:@"Батюшка"];
        [question setRow:[NSNumber numberWithUnsignedInt:19]];
        [question setColumn:[NSNumber numberWithUnsignedInt:0]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"радар"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Локатор гаишника"];
        [question setRow:[NSNumber numberWithUnsignedInt:19]];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ватага"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Артель рыбаков"];
        [question setRow:[NSNumber numberWithUnsignedInt:19]];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [puzzle addQuestionsObject:question];
        

        puzzle = (PuzzleData *)[NSEntityDescription insertNewObjectForEntityForName:@"Puzzle" inManagedObjectContext:managedObjectContext];
        [puzzle setWidth:[NSNumber numberWithUnsignedInt:14]];
        [puzzle setHeight:[NSNumber numberWithUnsignedInt:20]];
        [puzzle setIssuedAt:[NSDate date]];
        [puzzle setBase_score:[NSNumber numberWithUnsignedInt:1000]];
        [puzzle setName:@"Сканворд 2"];
        [puzzle setTime_given:[NSNumber numberWithUnsignedInt:10000]];
        [puzzleSet addPuzzlesObject:puzzle];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"авизо"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Посыльный вексель"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:0]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"мюзикл"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"\"Кошки\" с Бродвея"];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [question setRow:[NSNumber numberWithUnsignedInt:0]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"сатирикон"];
        [question setAnswer_positionAsString:@"north-west:bottom"];
        [question setQuestion_text:@"Театр К.Райкина"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"антимир"];
        [question setAnswer_positionAsString:@"north-west:bottom"];
        [question setQuestion_text:@"Чёрная дыра"];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"маляр"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Мастер ведра и кисти"];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"змий"];
        [question setAnswer_positionAsString:@"north-west:bottom"];
        [question setQuestion_text:@"Зелёный из бутылки водки"];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"саго"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Пальмовая крупа"];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"мамка"];
        [question setAnswer_positionAsString:@"north-west:bottom"];
        [question setQuestion_text:@"Кормилица (стар.)"];
        [question setColumn:[NSNumber numberWithUnsignedInt:9]];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"зазноба"];
        [question setAnswer_positionAsString:@"north-west:bottom"];
        [question setQuestion_text:@"Любимая в высоком терему"];
        [question setColumn:[NSNumber numberWithUnsignedInt:11]];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"клиника"];
        [question setAnswer_positionAsString:@"north-west:bottom"];
        [question setQuestion_text:@"Лечебный стационар"];
        [question setColumn:[NSNumber numberWithUnsignedInt:13]];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"смазчик"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Обслу- живающий подвижной состав"];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [question setRow:[NSNumber numberWithUnsignedInt:2]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"татами"];
        [question setAnswer_positionAsString:@"north-west:right"];
        [question setQuestion_text:@"Циновка под дзю-доистом"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:3]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"аймак"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Род у тюрков"];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [question setRow:[NSNumber numberWithUnsignedInt:3]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"запал"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Детонатор"];
        [question setColumn:[NSNumber numberWithUnsignedInt:9]];
        [question setRow:[NSNumber numberWithUnsignedInt:3]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"вальс"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Венский танец"];
        [question setColumn:[NSNumber numberWithUnsignedInt:11]];
        [question setRow:[NSNumber numberWithUnsignedInt:3]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"кашне"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Шейный платок"];
        [question setColumn:[NSNumber numberWithUnsignedInt:13]];
        [question setRow:[NSNumber numberWithUnsignedInt:3]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"реликт"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Пережиток древних эпох"];
        [question setColumn:[NSNumber numberWithUnsignedInt:5]];
        [question setRow:[NSNumber numberWithUnsignedInt:4]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"газовик"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Добытчик летучего топлива"];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [question setRow:[NSNumber numberWithUnsignedInt:4]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ромул"];
        [question setAnswer_positionAsString:@"north-west:right"];
        [question setQuestion_text:@"Основал с Ремом Рим"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:5]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ярмо"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Воловий хомут"];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [question setRow:[NSNumber numberWithUnsignedInt:5]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"абака"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Маниль- ская пенька"];
        [question setColumn:[NSNumber numberWithUnsignedInt:8]];
        [question setRow:[NSNumber numberWithUnsignedInt:5]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"авиатор"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Что пилот, что его механик"];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [question setRow:[NSNumber numberWithUnsignedInt:6]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"шпик"];
        [question setAnswer_positionAsString:@"south-east:bottom"];
        [question setQuestion_text:@"Жирная солонина"];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [question setRow:[NSNumber numberWithUnsignedInt:6]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"палаш"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Сабля с прямым лезвием"];
        [question setColumn:[NSNumber numberWithUnsignedInt:8]];
        [question setRow:[NSNumber numberWithUnsignedInt:6]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"кураре"];
        [question setAnswer_positionAsString:@"north-west:right"];
        [question setQuestion_text:@"Боевой яд туземцев"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:7]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"аванс"];
        [question setAnswer_positionAsString:@"north-east:bottom"];
        [question setQuestion_text:@"Лучшая часть зарплаты"];
        [question setColumn:[NSNumber numberWithUnsignedInt:2]];
        [question setRow:[NSNumber numberWithUnsignedInt:7]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"лапша"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Плоские макароны"];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [question setRow:[NSNumber numberWithUnsignedInt:7]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"икота"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Итог переедания"];
        [question setColumn:[NSNumber numberWithUnsignedInt:10]];
        [question setRow:[NSNumber numberWithUnsignedInt:7]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"скука"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Тоска ничего- неделания"];
        [question setColumn:[NSNumber numberWithUnsignedInt:12]];
        [question setRow:[NSNumber numberWithUnsignedInt:7]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"плиссе"];
        [question setAnswer_positionAsString:@"east:left"];
        [question setQuestion_text:@"Юбка в складочку"];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [question setRow:[NSNumber numberWithUnsignedInt:8]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"кукурузник"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Самолёт или прозвище Н.Хрущёва"];
        [question setColumn:[NSNumber numberWithUnsignedInt:0]];
        [question setRow:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"негатив"];
        [question setAnswer_positionAsString:@"north-west:right"];
        [question setQuestion_text:@"Позитив наоборот"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"абрис"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Очертание предмета"];
        [question setColumn:[NSNumber numberWithUnsignedInt:2]];
        [question setRow:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"киви"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Крыжовник с яйцо"];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [question setRow:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"виски"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Щотландская водка"];
        [question setColumn:[NSNumber numberWithUnsignedInt:9]];
        [question setRow:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"разор"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Обнищание (разг.)"];
        [question setColumn:[NSNumber numberWithUnsignedInt:11]];
        [question setRow:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"манок"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Свисток- зазывала"];
        [question setColumn:[NSNumber numberWithUnsignedInt:13]];
        [question setRow:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"кворум"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Полно- мочное собрание"];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [question setRow:[NSNumber numberWithUnsignedInt:10]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"красота"];
        [question setAnswer_positionAsString:@"nort-west:right"];
        [question setQuestion_text:@"\"Лепота!\""];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:11]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"орава"];
        [question setAnswer_positionAsString:@"north-east:bottom"];
        [question setQuestion_text:@"Кричащая толпа"];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [question setRow:[NSNumber numberWithUnsignedInt:11]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"метафора"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Образное сравнение"];
        [question setColumn:[NSNumber numberWithUnsignedInt:5]];
        [question setRow:[NSNumber numberWithUnsignedInt:11]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"нега"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Блажен-\nство в теле"];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [question setRow:[NSNumber numberWithUnsignedInt:11]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"итака"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Остров Одиссея"];
        [question setColumn:[NSNumber numberWithUnsignedInt:8]];
        [question setRow:[NSNumber numberWithUnsignedInt:11]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"сазан"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Карповая рыба"];
        [question setColumn:[NSNumber numberWithUnsignedInt:8]];
        [question setRow:[NSNumber numberWithUnsignedInt:12]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"кардамон"];
        [question setAnswer_positionAsString:@"north-west:right"];
        [question setQuestion_text:@"Пряность в кулинарии"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:13]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"вереск"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"\"Брат\" багульника"];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [question setRow:[NSNumber numberWithUnsignedInt:13]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"гранат"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Плод с багровой шрапнелью"];
        [question setColumn:[NSNumber numberWithUnsignedInt:10]];
        [question setRow:[NSNumber numberWithUnsignedInt:13]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"оборот"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Виток спутника"];
        [question setColumn:[NSNumber numberWithUnsignedInt:12]];
        [question setRow:[NSNumber numberWithUnsignedInt:13]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"пепел"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Пыль пожарища"];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [question setRow:[NSNumber numberWithUnsignedInt:14]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"игрок"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Завсег- датай казино"];
        [question setColumn:[NSNumber numberWithUnsignedInt:8]];
        [question setRow:[NSNumber numberWithUnsignedInt:14]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"раскат"];
        [question setAnswer_positionAsString:@"north-west:right"];
        [question setQuestion_text:@"Грохот грома"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:15]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"какапо"];
        [question setAnswer_positionAsString:@"north-east:bottom"];
        [question setQuestion_text:@"Совиный попугай"];
        [question setColumn:[NSNumber numberWithUnsignedInt:2]];
        [question setRow:[NSNumber numberWithUnsignedInt:15]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"апаш"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Воротник нара-\nспашку"];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [question setRow:[NSNumber numberWithUnsignedInt:15]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"шасси"];
        [question setAnswer_positionAsString:@"west:bottom"];
        [question setQuestion_text:@"Ноги лайнера"];
        [question setColumn:[NSNumber numberWithUnsignedInt:9]];
        [question setRow:[NSNumber numberWithUnsignedInt:15]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"лета"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Река забвения (мифол.)"];
        [question setColumn:[NSNumber numberWithUnsignedInt:11]];
        [question setRow:[NSNumber numberWithUnsignedInt:15]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"гуру"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Учитель у сикхов"];
        [question setColumn:[NSNumber numberWithUnsignedInt:13]];
        [question setRow:[NSNumber numberWithUnsignedInt:15]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ниша"];
        [question setAnswer_positionAsString:@"south-west:right"];
        [question setQuestion_text:@"Впадина в стене"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:16]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"кафе"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Общепит полд зонтиками"];
        [question setColumn:[NSNumber numberWithUnsignedInt:2]];
        [question setRow:[NSNumber numberWithUnsignedInt:16]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"аналог"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Точная копия изделия"];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [question setRow:[NSNumber numberWithUnsignedInt:16]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"опус"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Сочине- ние ком- позитора"];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [question setRow:[NSNumber numberWithUnsignedInt:17]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"неру"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Первый премьер Индии"];
        [question setColumn:[NSNumber numberWithUnsignedInt:9]];
        [question setRow:[NSNumber numberWithUnsignedInt:17]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"кино"];
        [question setAnswer_positionAsString:@"south-west:right"];
        [question setQuestion_text:@"Искусство для проката"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:18]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"пюре"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Картошка всмятку"];
        [question setColumn:[NSNumber numberWithUnsignedInt:2]];
        [question setRow:[NSNumber numberWithUnsignedInt:18]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"статор"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Стояк при роторе"];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [question setRow:[NSNumber numberWithUnsignedInt:18]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ални"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Сплав для магнитов"];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [question setRow:[NSNumber numberWithUnsignedInt:19]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"тату"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Наолка на груди"];
        [question setColumn:[NSNumber numberWithUnsignedInt:9]];
        [question setRow:[NSNumber numberWithUnsignedInt:19]];
        [puzzle addQuestionsObject:question];
        
        [managedObjectContext save:&error];
        if (error != nil)
        {
            NSLog(@"%@", error.localizedDescription);
        }
    }
    else
    {
        PuzzleSetData * puzzleSet = [puzzleSets objectAtIndex:0];
        NSEnumerator * en = [puzzleSet.puzzles objectEnumerator];
        int r = rand() % 100;
        NSLog(@"rand: %d", r);
        if (r > 49)
        {
            [en nextObject];
        }
        puzzle = [en nextObject];
    }
    
    currentGameField = [[GameField alloc] initWithData:puzzle];
}

-(void)handleTimer:(id)userInfo
{
    if (gameState == GAMESTATE_PLAYING)
    {
        _gameTime += gameTimer.timeInterval;
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_TIME_CHANGED]];
    }
}

@end
