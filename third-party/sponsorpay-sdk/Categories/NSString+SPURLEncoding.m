//
//  NSString+Escaping.m
//  SponsoPay iOS SDK
//
//  Created by David Davila on 8/9/12.
//  Copyright (c) 2012 David Davila. All rights reserved.
//

#import "NSString+SPURLEncoding.h"
#import "LoadableCategory.h"

MAKE_CATEGORIES_LOADABLE(NSString_SPURLEncoding)

@implementation NSString (SPURLEncoding)

- (NSString *)SPURLEncodedString
{
    return (NSString *) CFBridgingRelease(CFURLCreateStringByAddingPercentEscapes(NULL, (CFStringRef)[self mutableCopy], NULL, CFSTR("￼=,!$&'()*+;@?\n\"<>#\t :/"),kCFStringEncodingUTF8));
}

- (NSString *)SPURLDecodedString
{
	return [[self stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding] stringByReplacingOccurrencesOfString:@"+" withString:@" "];
}

@end
