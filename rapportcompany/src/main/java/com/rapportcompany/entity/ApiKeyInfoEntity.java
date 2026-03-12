// package com.mproject.app.entity;

package com.rapportcompany.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "API_KEY_INFO")
public class ApiKeyInfoEntity {

    @Id
    @Column(name = "SERVER_ID", length = 50, nullable = false)
    private String serverId;

    @Column(name = "SERVER_URL", length = 255, nullable = false)
    private String serverUrl;

    @Column(name = "SERVER_API_KEY", length = 255, nullable = false)
    private String serverApiKey;

    @Column(name = "USE_YN", length = 1, nullable = false)
    private String useYn;

    @Column(name = "CREATE_DT", length = 19, nullable = false)
    private String createDt;
}