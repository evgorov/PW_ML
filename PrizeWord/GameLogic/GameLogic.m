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
            [self initGameFieldWithType:type];
            [[AppDelegate currentDelegate].rootViewController hideMenuAnimated:YES];
            [[AppDelegate currentDelegate].navController pushViewController:[[GameViewController alloc] initWithGameField:currentGameField] animated:YES];
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
        
        puzzle = (PuzzleData *)[NSEntityDescription insertNewObjectForEntityForName:@"Puzzle" inManagedObjectContext:managedObjectContext];
        [puzzle setWidth:[NSNumber numberWithUnsignedInt:9]];
        [puzzle setHeight:[NSNumber numberWithUnsignedInt:12]];
        [puzzle setIssuedAt:[NSDate date]];
        [puzzle setBase_score:[NSNumber numberWithUnsignedInt:1000]];
        [puzzle setName:@"Сканворд 1"];
        [puzzle setPuzzle_id:@"puzzle_1"];
        [puzzle setTime_given:[NSNumber numberWithUnsignedInt:10000]];
        [puzzleSet addPuzzlesObject:puzzle];
        
        QuestionData * question;

        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ом"];
        [question setAnswer_positionAsString:@"south-east:right"];
        [question setQuestion_text:@"Немецкий\nфизик"];
        [question setColumn:[NSNumber numberWithUnsignedInt:0]];
        [question setRow:[NSNumber numberWithUnsignedInt:0]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"он"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Буква\nкирилицы"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:0]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"смотр"];
        [question setAnswer_positionAsString:@"west:bottom"];
        [question setQuestion_text:@"Показ ре-\nзультатов\nдеятель-\nности"];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [question setRow:[NSNumber numberWithUnsignedInt:0]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"мало"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Франц.\nписатель\n(\"без\nсемьи\")"];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [question setRow:[NSNumber numberWithUnsignedInt:0]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ан"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Отечест-\nвенный\nсамолёт"];
        [question setColumn:[NSNumber numberWithUnsignedInt:5]];
        [question setRow:[NSNumber numberWithUnsignedInt:0]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"раб"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Невольник"];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [question setRow:[NSNumber numberWithUnsignedInt:0]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ушное"];
        [question setAnswer_positionAsString:@"west:bottom"];
        [question setQuestion_text:@"Блюдо из\nмяса"];
        [question setColumn:[NSNumber numberWithUnsignedInt:8]];
        [question setRow:[NSNumber numberWithUnsignedInt:0]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"дно"];
        [question setAnswer_positionAsString:@"south:right"];
        [question setQuestion_text:@"Двойное\n..."];
        [question setColumn:[NSNumber numberWithUnsignedInt:0]];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"марш"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Музы-\nкальный\nжанр"];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"данте"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Созда-\nтель итал.\nлит. языка"];
        [question setColumn:[NSNumber numberWithUnsignedInt:8]];
        [question setRow:[NSNumber numberWithUnsignedInt:1]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ананд"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Междун.\nгросс-\nмейстер\nиз Индии"];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [question setRow:[NSNumber numberWithUnsignedInt:2]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"декоратор"];
        [question setAnswer_positionAsString:@"north-west:bottom"];
        [question setQuestion_text:@"Театраль-\nный ху-\nдажник"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:3]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ляп"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Промах"];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [question setRow:[NSNumber numberWithUnsignedInt:3]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"боа"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Крупная\nзмея"];
        [question setColumn:[NSNumber numberWithUnsignedInt:5]];
        [question setRow:[NSNumber numberWithUnsignedInt:3]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ела"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Четырёх-\nвёсель-\nная ло-\nдка (стар.)"];
        [question setColumn:[NSNumber numberWithUnsignedInt:5]];
        [question setRow:[NSNumber numberWithUnsignedInt:4]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ен"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Бог-\nдемиург\nв мифах\nкоми"];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [question setRow:[NSNumber numberWithUnsignedInt:4]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"карло"];
        [question setAnswer_positionAsString:@"north-west:right"];
        [question setQuestion_text:@"Персонаж\n\"Золото-\nго клю-\nчика\""];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:5]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"аттила"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Предво-\nдитель\nгуннов"];
        [question setColumn:[NSNumber numberWithUnsignedInt:2]];
        [question setRow:[NSNumber numberWithUnsignedInt:5]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"плашка"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Инстру-\nмент для\nнарезания\nрезьбы"];
        [question setColumn:[NSNumber numberWithUnsignedInt:4]];
        [question setRow:[NSNumber numberWithUnsignedInt:5]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ив"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"... Монтан"];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [question setRow:[NSNumber numberWithUnsignedInt:5]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"крюк"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Лишнее\nрасстоя-\nние в пути"];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [question setRow:[NSNumber numberWithUnsignedInt:5]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"апплике"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Накладное\nсеребро"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:6]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"лавр"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Вечнозе-\nленое\nдерево"];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [question setRow:[NSNumber numberWithUnsignedInt:7]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"заза"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Опера\nРужжеро\nЛеонка-\nвалло"];
        [question setColumn:[NSNumber numberWithUnsignedInt:8]];
        [question setRow:[NSNumber numberWithUnsignedInt:7]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"аут"];
        [question setAnswer_positionAsString:@"north-west:right"];
        [question setQuestion_text:@"Выход\nмяча за\nпределы\nполя"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:8]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"тук"];
        [question setAnswer_positionAsString:@"south:bottom"];
        [question setQuestion_text:@"Жир, сало\n(стар.)"];
        [question setColumn:[NSNumber numberWithUnsignedInt:5]];
        [question setRow:[NSNumber numberWithUnsignedInt:8]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"юз"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Разработ-\nчик уголь-\nного мик-\nрофона"];
        [question setColumn:[NSNumber numberWithUnsignedInt:6]];
        [question setRow:[NSNumber numberWithUnsignedInt:8]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"тха"];
        [question setAnswer_positionAsString:@"north-east:right"];
        [question setQuestion_text:@"Верхов-\nный бог\nу адыгов"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"штука"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Проделка,\nвыдумка"];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [question setRow:[NSNumber numberWithUnsignedInt:9]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"кур"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Подземный\nмир у\nшумеров"];
        [question setColumn:[NSNumber numberWithUnsignedInt:3]];
        [question setRow:[NSNumber numberWithUnsignedInt:10]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"ура"];
        [question setAnswer_positionAsString:@"north-west:bottom"];
        [question setQuestion_text:@"Боевой\nклич\nрусских\nвоинов"];
        [question setColumn:[NSNumber numberWithUnsignedInt:7]];
        [question setRow:[NSNumber numberWithUnsignedInt:10]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"рол"];
        [question setAnswer_positionAsString:@"north:right"];
        [question setQuestion_text:@"Рулон"];
        [question setColumn:[NSNumber numberWithUnsignedInt:0]];
        [question setRow:[NSNumber numberWithUnsignedInt:11]];
        [puzzle addQuestionsObject:question];
        
        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"атакама"];
        [question setAnswer_positionAsString:@"east:right"];
        [question setQuestion_text:@"Пустыня в\nЧили"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:11]];
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
        puzzle = [puzzleSet.puzzles anyObject];
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
