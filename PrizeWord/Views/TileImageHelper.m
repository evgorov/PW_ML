//
//  TileImageHelper.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/2/12.
//
//

#import "TileImageHelper.h"

@interface TileImageHelper (private)

-(void)prepareLetters:(UIImage * __strong *)letters fromAtlas:(UIImage *)atlas;

@end

@implementation TileImageHelper

static TileImageHelper * _sharedHelper = nil;

+(void)initHelper
{
    if (_sharedHelper == nil) {
        _sharedHelper = [[TileImageHelper alloc] init];
    }
}

+(void)uninitHelper
{
    _sharedHelper = nil;
}

+(TileImageHelper *)sharedHelper
{
    return _sharedHelper;
}

-(id)init
{
    NSLog(@"TileImageHelper init");
    self = [super init];
    if (self) {
        [self prepareLetters:brilliantLetters fromAtlas:[UIImage imageNamed:@"tile_letters_correct_brilliant"]];
        [self prepareLetters:goldLetters fromAtlas:[UIImage imageNamed:@"tile_letters_correct_gold"]];
        [self prepareLetters:silverLetters fromAtlas:[UIImage imageNamed:@"tile_letters_correct_silver"]];
        [self prepareLetters:freeLetters fromAtlas:[UIImage imageNamed:@"tile_letters_correct_free"]];
        [self prepareLetters:inputLetters fromAtlas:[UIImage imageNamed:@"tile_letters_input"]];
        [self prepareLetters:wrongLetters fromAtlas:[UIImage imageNamed:@"tile_letters_wrong"]];
    }
    return self;
}

-(void)dealloc
{
    NSLog(@"TileImageHelper dealloc");
}

-(void)prepareLetters:(UIImage * __strong *)letters fromAtlas:(UIImage *)atlas;
{
    CGImageRef atlasImage = atlas.CGImage;
    int atlasWidth = CGImageGetWidth(atlasImage);
    int atlasHeight = CGImageGetHeight(atlasImage);
    int tileWidth = atlasWidth / 7;
    int tileHeight = atlasHeight / 5;
    
    for (int j = 0; j < 5; ++j) {
        for (int i = 0; i < 7; ++i) {
            letters[7 * j + i] = [UIImage imageWithCGImage: CGImageCreateWithImageInRect(atlasImage, CGRectMake(i * tileWidth, j * tileHeight, tileWidth, tileHeight))];
        }
    }
}

-(UIImage *)letterForType:(LetterType)type andIndex:(uint)index
{
    switch (type) {
        case LETTER_BRILLIANT:
            return brilliantLetters[index];
            break;
            
        case LETTER_GOLD:
            return goldLetters[index];
            break;
            
        case LETTER_SILVER:
            return silverLetters[index];
            break;
            
        case LETTER_FREE:
            return freeLetters[index];
            break;
            
        case LETTER_INPUT:
            return inputLetters[index];
            break;
            
        case LETTER_WRONG:
            return wrongLetters[index];
            break;
            
        default:
            break;
    }
    return nil;
}

-(UIImage *)correctQuestionForType:(LetterType)type
{
    switch (type) {
        case LETTER_BRILLIANT:
            return [UIImage imageNamed:@"tile_question_correct_brilliant.png"];
            
        case LETTER_GOLD:
            return [UIImage imageNamed:@"tile_question_correct_gold.png"];
            
        case LETTER_SILVER:
            return [UIImage imageNamed:@"tile_question_correct_silver.png"];
            
        case LETTER_FREE:
            return [UIImage imageNamed:@"tile_question_correct_free.png"];
            
        default:
            break;
    }
    return nil;
}

@end
