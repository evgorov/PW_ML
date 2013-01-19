//
//  QuestionData.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/28/12.
//
//

#import "QuestionData.h"
#import "PuzzleData.h"


@implementation QuestionData

@dynamic column;
@dynamic row;
@dynamic answer_position;
@dynamic solved;
@dynamic question_text;
@dynamic answer;
@dynamic puzzle;

-(void)setAnswer_positionAsUint:(uint)answer_positionAsUint
{
    self.answer_position = [NSNumber numberWithUnsignedInt:answer_positionAsUint];
}

-(uint)answer_positionAsUint
{
    return [self.answer_position unsignedIntValue];
}

-(void)setColumnAsUint:(uint)columnAsUint
{
    self.column = [NSNumber numberWithUnsignedInt:columnAsUint];
}

-(uint)columnAsUint
{
    return [self.column unsignedIntValue];
}

-(void)setRowAsUint:(uint)rowAsUint
{
    self.row = [NSNumber numberWithUnsignedInt:rowAsUint];
}

-(uint)rowAsUint
{
    return [self.row unsignedIntValue];
}

-(NSString *)answer_positionAsString
{
    NSString * positionString = @"";
    uint position = self.answer_positionAsUint;
    if ((position & kAnswerPositionNorth) != 0)
        positionString = @"north";
    if ((position & kAnswerPositionSouth) != 0)
        positionString = @"south";
    if ((position & kAnswerPositionWest) != 0)
    {
        if (positionString.length == 0)
        {
            positionString = @"west";
        }
        else
        {
            positionString = [positionString stringByAppendingString:@"-west"];
        }
    }
    if ((position & kAnswerPositionEast) != 0)
    {
        if (positionString.length == 0)
        {
            positionString = @"east";
        }
        else
        {
            positionString = [positionString stringByAppendingString:@"-east"];
        }
    }
    if ((position & kAnswerPositionTop) != 0)
        positionString = [positionString stringByAppendingString:@":top"];
    if ((position & kAnswerPositionBottom) != 0)
        positionString = [positionString stringByAppendingString:@":bottom"];
    if ((position & kAnswerPositionLeft) != 0)
        positionString = [positionString stringByAppendingString:@":left"];
    if ((position & kAnswerPositionRight) != 0)
        positionString = [positionString stringByAppendingString:@":right"];
    return positionString;
}

-(void)setAnswer_positionAsString:(NSString *)answer_positionAsString
{
    uint position = 0;
    if ([answer_positionAsString rangeOfString:@"north"].location != NSNotFound)
        position |= kAnswerPositionNorth;
    if ([answer_positionAsString rangeOfString:@"south"].location != NSNotFound)
        position |= kAnswerPositionSouth;
    if ([answer_positionAsString rangeOfString:@"east"].location != NSNotFound)
        position |= kAnswerPositionEast;
    if ([answer_positionAsString rangeOfString:@"west"].location != NSNotFound)
        position |= kAnswerPositionWest;
    if ([answer_positionAsString rangeOfString:@"top"].location != NSNotFound)
        position |= kAnswerPositionTop;
    if ([answer_positionAsString rangeOfString:@"bottom"].location != NSNotFound)
        position |= kAnswerPositionBottom;
    if ([answer_positionAsString rangeOfString:@"left"].location != NSNotFound)
        position |= kAnswerPositionLeft;
    if ([answer_positionAsString rangeOfString:@"right"].location != NSNotFound)
        position |= kAnswerPositionRight;
    self.answer_positionAsUint = position;
}

@end
