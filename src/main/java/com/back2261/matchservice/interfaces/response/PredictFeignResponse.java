package com.back2261.matchservice.interfaces.response;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class PredictFeignResponse {

    private String user_id;
    private List<String> sim_users;
}
