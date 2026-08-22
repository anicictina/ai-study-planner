package com.anicictina.backend.user.dto;

import com.anicictina.backend.user.PreferredTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {

    private PreferredTime preferredStudyTime;
}
