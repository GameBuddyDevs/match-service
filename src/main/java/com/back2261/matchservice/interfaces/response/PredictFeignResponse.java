package com.back2261.matchservice.interfaces.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PredictFeignResponse {

    private String user_id;
    private List<String> sim_users;
}
