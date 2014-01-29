//
//  QuestionProxy.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/27/14.
//
//

#import "QuestionProxy.h"
#import "DataContext.h"

@implementation QuestionProxy

DATAPROXY_FORWARDING_GET(QuestionProxy *,uint,columnAsUint)
DATAPROXY_FORWARDING_GET(QuestionProxy *,uint,rowAsUint)
DATAPROXY_FORWARDING_GET(QuestionProxy *,NSString *,question_text)
DATAPROXY_FORWARDING_GET(QuestionProxy *,NSString *,answer)
DATAPROXY_FORWARDING_GET(QuestionProxy *,uint,answer_positionAsUint)
DATAPROXY_FORWARDING_GET(QuestionProxy *,NSNumber *,solved)
DATAPROXY_FORWARDING_SET(QuestionProxy *,NSNumber *,setSolved)

@end
