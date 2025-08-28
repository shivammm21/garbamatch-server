package com.garba.garba.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserStatsResponse {
    Long totalUsers;
    Long trialPlanUsers;
    Long basicPlanUsers;
    Long premiumPlanUsers;
}