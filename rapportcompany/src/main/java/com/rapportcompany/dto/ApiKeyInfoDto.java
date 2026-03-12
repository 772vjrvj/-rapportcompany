// package com.mproject.app.dto;

package com.rapportcompany.dto;

import lombok.Data;

@Data
public class ApiKeyInfoDto {

    private String serverId;
    private String serverUrl;
    private String serverApiKey;
    private String useYn;
    private String createDt;
}