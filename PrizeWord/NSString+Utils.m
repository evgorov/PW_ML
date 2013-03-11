//
//  NSString_Utils.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 3/6/13.
//
//

#include "NSString+Utils.h"

@implementation NSString (Utils)

+(NSString *)digitString:(int)value
{
    if (value == 0)
    {
        return @"0";
    }
    int absValue = value > 0 ? value : (value * -1);
    NSString * result = @"";
    while (absValue >= 1000)
    {
        if (result.length > 0)
        {
            result = [NSString stringWithFormat:@"%03d %@", absValue % 1000, result];
        }
        else
        {
            result = [NSString stringWithFormat:@"%03d", absValue % 1000];
        }
        absValue /= 1000;
        value /= 1000;
    }
    if (result.length > 0)
    {
        result = [NSString stringWithFormat:@"%d %@", value, result];
    }
    else
    {
        result = [NSString stringWithFormat:@"%d", value];
    }
    return result;
}

+(NSString *)declesion:(int)number oneString:(NSString *)one twoString:(NSString *)two fiveString:(NSString *)five
{
    number = number < 0 ? (number * -1) : number;
    number %= 100;
    if (number >= 5 && number <= 20)
    {
        return five;
    }
    number %= 10;
    if (number == 1)
    {
        return one;
    }
    if (number >= 2 && number <= 4)
    {
        return two;
    }
    return five;
}

@end
