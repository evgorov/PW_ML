//
//  PuzzleSetPackProxy.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/27/14.
//
//

#import "PuzzleSetPackProxy.h"

@implementation PuzzleSetPackProxy

DATAPROXY_FORWARDING_GET(PuzzleSetPackData *, NSNumber *, month)
DATAPROXY_FORWARDING_GET(PuzzleSetPackData *, NSNumber *, year)
DATAPROXY_FORWARDING_GET(PuzzleSetPackData *, NSString *, user_id)
DATAPROXY_FORWARDING_GET(PuzzleSetPackData *, NSString *, etag)
DATAPROXY_FORWARDING_SET(PuzzleSetPackData *, NSString *, setEtag)

@end
